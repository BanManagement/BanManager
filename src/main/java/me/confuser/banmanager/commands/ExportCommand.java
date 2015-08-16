package me.confuser.banmanager.commands;

import com.google.gson.stream.JsonWriter;
import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.apache.commons.lang.time.FastDateFormat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.*;
import java.nio.charset.Charset;

public class ExportCommand extends BukkitCommand<BanManager> {

  private static final FastDateFormat BANNED_JSON_TIME_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd_HH-mm-ss");

  private boolean inProgress = false;

  public ExportCommand() {
    super("bmexport");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, final String[] args) {
    if (args.length != 1) return false;

    if (inProgress) {
      sender.sendMessage(Message.getString("export.error.inProgress"));
      return true;
    }

    if (!args[0].startsWith("play") && !args[0].startsWith("ip")) return false;

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        Message finishedMessage = null;
        String fileName;

        if (args[0].startsWith("play")) {
          sender.sendMessage(Message.getString("export.player.started"));
          finishedMessage = Message.get("export.player.finished");
          fileName = "banned-players-" + BANNED_JSON_TIME_FORMAT.format(System.currentTimeMillis()) + ".json";

          finishedMessage.set("file", fileName);

          try {
            exportPlayers(fileName);
          } catch (IOException e) {
            sender.sendMessage(Message.get("sender.error.exception").toString());
            e.printStackTrace();
            return;
          }
        } else if (args[0].startsWith("ip")) {
          sender.sendMessage(Message.getString("export.ip.started"));
          finishedMessage = Message.get("export.player.finished");

          fileName = "banned-ips-" + BANNED_JSON_TIME_FORMAT.format(System.currentTimeMillis()) + ".json";

          finishedMessage.set("file", fileName);

          try {
            exportIps(fileName);
          } catch (IOException e) {
            sender.sendMessage(Message.get("sender.error.exception").toString());
            e.printStackTrace();
            return;
          }
        } else {
          return;
        }

        finishedMessage.sendTo(sender);
      }
    });

    return true;
  }

  private static final FastDateFormat EXPORT_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss Z");

  private void exportIps(String fileName) throws IOException {
    File file = new File(plugin.getDataFolder(), fileName);

    if (!file.exists()) {
      file.createNewFile();
    } else {
      throw new IOException("File already exists");
    }

    JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8").newEncoder()));

    jsonWriter.beginArray();
    jsonWriter.setIndent("  ");

    CloseableIterator<IpBanData> itr = plugin.getIpBanStorage().iterator();

    while (itr.hasNext()) {
      IpBanData next = itr.next();

      jsonWriter.beginObject();

      jsonWriter.name("ip").value(IPUtils.toString(next.getIp()));
      jsonWriter.name("created").value(EXPORT_FORMAT.format(next.getCreated() * 1000L));
      jsonWriter.name("source").value(next.getActor().getName());
      jsonWriter.name("expires");

      if (next.getExpires() == 0) {
        jsonWriter.value("forever");
      } else {
        jsonWriter.value(EXPORT_FORMAT.format(next.getExpires() * 1000L));
      }

      jsonWriter.name("reason").value(next.getReason());

      jsonWriter.endObject();
    }

    itr.closeQuietly();

    jsonWriter.endArray();
    jsonWriter.close();
  }

  private void exportPlayers(String fileName) throws IOException {
    File file = new File(plugin.getDataFolder(), fileName);

    if (!file.exists()) {
      file.createNewFile();
    } else {
      throw new IOException("File already exists");
    }

    JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8").newEncoder()));
    jsonWriter.beginArray();
    jsonWriter.setIndent("  ");

    CloseableIterator<PlayerBanData> itr = plugin.getPlayerBanStorage().iterator();

    while (itr.hasNext()) {
      PlayerBanData next = itr.next();

      jsonWriter.beginObject();

      jsonWriter.name("uuid").value(next.getPlayer().getUUID().toString());
      jsonWriter.name("name").value(next.getPlayer().getName());
      jsonWriter.name("created").value(EXPORT_FORMAT.format(next.getCreated() * 1000L));
      jsonWriter.name("source").value(next.getActor().getName());
      jsonWriter.name("expires");

      if (next.getExpires() == 0) {
        jsonWriter.value("forever");
      } else {
        jsonWriter.value(EXPORT_FORMAT.format(next.getExpires() * 1000L));
      }

      jsonWriter.name("reason").value(next.getReason());

      jsonWriter.endObject();
    }

    itr.closeQuietly();

    jsonWriter.endArray();
    jsonWriter.close();
  }
}
