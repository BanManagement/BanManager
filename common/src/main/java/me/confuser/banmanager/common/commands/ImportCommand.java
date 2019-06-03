package me.confuser.banmanager.common.commands;

import com.google.common.net.InetAddresses;
import com.google.gson.stream.JsonReader;
import me.confuser.banmanager.common.command.CommandResult;
import me.confuser.banmanager.common.command.abstraction.SingleCommand;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.command.CommandSpec;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.common.util.Predicates;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.storage.conversion.SimpleWarnings;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.banmanager.util.UUIDUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class ImportCommand extends SingleCommand {

  private boolean importInProgress = false;
  private static HashSet<String> validConverters = new HashSet<String>(){{
    add("player");
    add("players");
    add("ip");
    add("ips");
    add("simplewarnings");
  }};

  public ImportCommand(LocaleManager locale) {
    super(CommandSpec.BMIMPORT.localize(locale), "bmimport", CommandPermission.BMIMPORT, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {
    if (args.size() != 1) {
      return CommandResult.INVALID_ARGS;
    }

    if (!validConverters.contains(args.get(0).toLowerCase())) return CommandResult.INVALID_ARGS;

    if (importInProgress) {
      Message.IMPORT_ERROR_INPROGRESS.send(sender);
      return CommandResult.SUCCESS;
    }

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      Message finishedMessage = null;

      if (args.get(0).startsWith("player")) {
        Message.IMPORT_PLAYER_STARTED.send(sender);
        finishedMessage = Message.IMPORT_PLAYER_FINISHED;

        importPlayers(plugin);
      } else if (args.get(0).startsWith("ip")) {
        Message.IMPORT_IP_STARTED.send(sender);
        finishedMessage = Message.IMPORT_IP_FINISHED;

        importIps(plugin);
      } else if (args.get(0).startsWith("simplew")) {
        new SimpleWarnings();
      }

      if (sender != null && finishedMessage != null) {
        finishedMessage.send(sender);
      }
    });

    return CommandResult.SUCCESS;
  }

  private void importPlayers(BanManagerPlugin plugin) {
    importInProgress = true;

    plugin.getLogger().info(Message.IMPORT_PLAYER_STARTED.getMessage());

    try {
      JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream("banned-players.json"), Charset.forName("UTF-8").newDecoder()));
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
                actor = plugin.getPlayerStorage().getConsole();
              } else {
                actor = plugin.getPlayerStorage().retrieve(sourceName, false);

                if (actor == null) {
                  actor = plugin.getPlayerStorage().getConsole();
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
          plugin.getLogger().warn("Invalid name " + name + " skipping its import");
          continue;
        }

        if (plugin.getPlayerBanStorage().isBanned(uuid)) {
          continue;
        }

        PlayerData player = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid));

        if (player == null) {
          player = new PlayerData(uuid, name);

          plugin.getPlayerStorage().create(player);
        }

        PlayerBanData ban = new PlayerBanData(player, actor, reason, expires, created);
        try {
          plugin.getPlayerBanStorage().create(ban);
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
    plugin.getLogger().info(Message.IMPORT_PLAYER_FINISHED.getMessage());
  }

  private void importIps(BanManagerPlugin plugin) {
    importInProgress = true;

    plugin.getLogger().info(Message.IMPORT_IP_STARTED.getMessage());

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
                actor = plugin.getPlayerStorage().getConsole();
              } else {
                actor = plugin.getPlayerStorage().retrieve(sourceName, false);

                if (actor == null) {
                  actor = plugin.getPlayerStorage().getConsole();
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

        long ip = IPUtils.toLong(ipStr);

        if (plugin.getIpBanStorage().isBanned(ip)) {
          continue;
        }

        IpBanData ban = new IpBanData(ip, actor, reason, expires, created);
        try {
          plugin.getIpBanStorage().create(ban);
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

    plugin.getLogger().info(Message.IMPORT_IP_FINISHED.getMessage());
  }

  private boolean isValidSource(String name) {
    return !name.equals("CONSOLE") && !name.equals("(UNKNOWN)") && name.length() <= 16;
  }
}
