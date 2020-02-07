package me.confuser.banmanager.common.commands;

import com.google.common.net.InetAddresses;
import com.google.gson.stream.JsonReader;
import inet.ipaddr.IPAddress;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.storage.conversion.AdvancedBan;
import me.confuser.banmanager.common.storage.conversion.SimpleWarnings;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.UUID;

public class ImportCommand extends CommonCommand {
  private boolean importInProgress = false;
  private static HashSet<String> validConverters = new HashSet<String>(){{
    add("player");
    add("players");
    add("ip");
    add("ips");
    add("simplewarnings");
    add("advancedban");
  }};

  public ImportCommand(BanManagerPlugin plugin) {
    super(plugin, "bmimport", false);
  }

  @Override
  public boolean onCommand(CommonSender sender, CommandParser parser) {
    if (parser.args.length < 1) {
      return false;
    }

    if (!validConverters.contains(parser.args[0].toLowerCase())) return false;

    if (importInProgress) {
      sender.sendMessage(Message.getString("import.error.inProgress"));
      return true;
    }

    getPlugin().getScheduler().runAsync(() -> {
      String finishedMessage = "";

      if (parser.args[0].startsWith("player")) {
        sender.sendMessage(Message.getString("import.player.started"));
        finishedMessage = Message.getString("import.player.finished");

        importPlayers();
      } else if (parser.args[0].startsWith("ip")) {
        sender.sendMessage(Message.getString("import.ip.started"));
        finishedMessage = Message.getString("import.ip.finished");

        importIps();
      } else if (parser.args[0].startsWith("simplew")) {
        new SimpleWarnings(getPlugin());
      } else if (parser.args[0].startsWith("advancedb")) {
        if (parser.args.length < 5) {
          sender.sendMessage("/bmimport advancedban localhost 3306 databaseName username password");
          return;
        }

        sender.sendMessage(Message.getString("import.advancedban.started"));
        finishedMessage = Message.getString("import.advancedban.finished");

        new AdvancedBan(getPlugin(), parser.args);
      }

      if (sender != null) {
        sender.sendMessage(finishedMessage);
      }
    });

    return true;
  }

  private void importPlayers() {
    importInProgress = true;

    getPlugin().getLogger().info(Message.getString("import.player.started"));

    try {
      JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream("banned-players.json"), StandardCharsets.UTF_8.newDecoder()));
      reader.beginArray();

      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

      while (reader.hasNext()) {
        reader.beginObject();

        UUID uuid = null;
        String name = null;
        Long created = null;
        PlayerData actor = null;
        Long expires = null;
        String reason = null;

        while (reader.hasNext()) {
          switch (reader.nextName()) {
            case "uuid":
              uuid = UUID.fromString(reader.nextString());
              break;
            case "name":
              name = reader.nextString();
              break;
            case "created":
              try {
                created = dateFormat.parse(reader.nextString()).getTime() / 1000L;
              } catch (ParseException e) {
                e.printStackTrace();

                continue;
              }
              break;
            case "source":
              String sourceName = reader.nextString();

              if (!isValidSource(sourceName)) {
                actor = getPlugin().getPlayerStorage().getConsole();
              } else {
                actor = getPlugin().getPlayerStorage().retrieve(sourceName, false);

                if (actor == null) {
                  actor = getPlugin().getPlayerStorage().getConsole();
                }
              }
              break;
            case "expires":
              String expiresStr = reader.nextString();

              if (expiresStr.equals("forever")) {
                expires = 0L;
              } else {
                try {
                  expires = dateFormat.parse(expiresStr).getTime() / 1000L;
                } catch (ParseException e) {
                  e.printStackTrace();

                  continue;
                }
              }
              break;
            case "reason":
              reason = reader.nextString();
              break;
          }
        }

        reader.endObject();

        if (uuid == null || name == null || created == null || actor == null || expires == null || reason == null) {
          continue;
        }

        if (!isValidSource(name)) {
          getPlugin().getLogger().warning("Invalid name " + name + " skipping its import");
          continue;
        }

        if (getPlugin().getPlayerBanStorage().isBanned(uuid)) {
          continue;
        }

        PlayerData player = getPlugin().getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid));

        if (player == null) {
          player = new PlayerData(uuid, name);

          getPlugin().getPlayerStorage().create(player);
        }

        PlayerBanData ban = new PlayerBanData(player, actor, reason, expires, created);
        try {
          getPlugin().getPlayerBanStorage().create(ban);
        } catch (SQLException e) {
          e.printStackTrace();
          continue;
        }
      }

      reader.endArray();

      reader.close();
    } catch (IOException | SQLException e) {
      e.printStackTrace();
    }

    importInProgress = false;
    getPlugin().getLogger().info(Message.getString("import.player.finished"));
  }

  private void importIps() {
    importInProgress = true;

    getPlugin().getLogger().info(Message.getString("import.ip.started"));

    try {
      JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream("banned-ips.json"), Charset.forName("UTF-8").newDecoder()));
      reader.beginArray();

      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

      while (reader.hasNext()) {
        reader.beginObject();

        String ipStr = null;
        Long created = null;
        PlayerData actor = null;
        Long expires = null;
        String reason = null;

        while (reader.hasNext()) {
          switch (reader.nextName()) {
            case "ip":
              ipStr = reader.nextString();
              break;
            case "created":
              try {
                created = dateFormat.parse(reader.nextString()).getTime() / 1000L;
              } catch (ParseException e) {
                e.printStackTrace();

                continue;
              }
              break;
            case "source":
              String sourceName = reader.nextString();

              if (!isValidSource(sourceName)) {
                actor = getPlugin().getPlayerStorage().getConsole();
              } else {
                actor = getPlugin().getPlayerStorage().retrieve(sourceName, false);

                if (actor == null) {
                  actor = getPlugin().getPlayerStorage().getConsole();
                }
              }
              break;
            case "expires":
              String expiresStr = reader.nextString();

              if (expiresStr.equals("forever")) {
                expires = 0L;
              } else {
                try {
                  created = dateFormat.parse(reader.nextString()).getTime() / 1000L;
                } catch (ParseException e) {
                  e.printStackTrace();

                  continue;
                }
              }
              break;
            case "reason":
              reason = reader.nextString();
              break;
          }
        }

        reader.endObject();

        if (ipStr == null || created == null || actor == null || expires == null || reason == null) {
          continue;
        }

        if (!InetAddresses.isInetAddress(ipStr)) {
          continue;
        }

        IPAddress ip = getIp(ipStr);

        if (getPlugin().getIpBanStorage().isBanned(ip)) {
          continue;
        }

        IpBanData ban = new IpBanData(ip, actor, reason, expires, created);
        try {
          getPlugin().getIpBanStorage().create(ban);
        } catch (SQLException e) {
          e.printStackTrace();
          continue;
        }
      }

      reader.endArray();

      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    importInProgress = false;

    getPlugin().getLogger().info(Message.getString("import.ip.finished"));
  }

  private boolean isValidSource(String name) {
    return !name.equals("CONSOLE") && !name.equals("(UNKNOWN)") && name.length() <= 16;
  }
}
