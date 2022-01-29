package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.gson.stream.JsonWriter;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class ExportCommand extends CommonCommand {

  private static final String BANNED_JSON_TIME_FORMAT = "yyyy-MM-dd_HH-mm-ss";
  private static final String EXPORT_FORMAT = "yyyy-MM-dd HH:mm:ss Z";
  private boolean inProgress = false;

  public ExportCommand(BanManagerPlugin plugin) {
    super(plugin, "bmexport", false);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.args.length != 1) return false;

    if (inProgress) {
      sender.sendMessage(Message.getString("export.error.inProgress"));
      return true;
    }

    if (!parser.args[0].startsWith("play") && !parser.args[0].startsWith("ip")) return false;

    getPlugin().getScheduler().runAsync(() -> {
      Message finishedMessage;
      String fileName;

      if (parser.args[0].startsWith("play")) {
        sender.sendMessage(Message.getString("export.player.started"));
        finishedMessage = Message.get("export.player.finished");
        fileName = "banned-players-" + DateUtils.format(BANNED_JSON_TIME_FORMAT, System.currentTimeMillis() / 1000L) + ".json";

        finishedMessage.set("file", fileName);

        try {
          exportPlayers(fileName);
        } catch (IOException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }
      } else if (parser.args[0].startsWith("ip")) {
        sender.sendMessage(Message.getString("export.ip.started"));
        finishedMessage = Message.get("export.player.finished");

        fileName = "banned-ips-" + DateUtils.format(BANNED_JSON_TIME_FORMAT, System.currentTimeMillis() / 1000L) + ".json";

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
    });

    return true;
  }

  private void exportIps(String fileName) throws IOException {
    File file = new File(getPlugin().getDataFolder(), fileName);

    if (!file.exists()) {
      file.createNewFile();
    } else {
      throw new IOException("File already exists");
    }

    JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8")
        .newEncoder()));

    jsonWriter.beginArray();
    jsonWriter.setIndent("  ");

    CloseableIterator<IpBanData> itr = getPlugin().getIpBanStorage().iterator();

    while (itr.hasNext()) {
      IpBanData next = itr.next();

      jsonWriter.beginObject();

      jsonWriter.name("ip").value(next.getIp().toString());
      jsonWriter.name("created").value(DateUtils.format(EXPORT_FORMAT, next.getCreated()));
      jsonWriter.name("source").value(next.getActor().getName());
      jsonWriter.name("expires");

      if (next.getExpires() == 0) {
        jsonWriter.value("forever");
      } else {
        jsonWriter.value(DateUtils.format(EXPORT_FORMAT, next.getExpires()));
      }

      jsonWriter.name("reason").value(next.getReason());

      jsonWriter.endObject();
    }

    itr.closeQuietly();

    jsonWriter.endArray();
    jsonWriter.close();
  }

  private void exportPlayers(String fileName) throws IOException {
    File file = new File(getPlugin().getDataFolder(), fileName);

    if (!file.exists()) {
      file.createNewFile();
    } else {
      throw new IOException("File already exists");
    }

    JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8")
        .newEncoder()));
    jsonWriter.beginArray();
    jsonWriter.setIndent("  ");

    CloseableIterator<PlayerBanData> itr = getPlugin().getPlayerBanStorage().iterator();

    while (itr.hasNext()) {
      PlayerBanData next = itr.next();

      jsonWriter.beginObject();

      jsonWriter.name("uuid").value(next.getPlayer().getUUID().toString());
      jsonWriter.name("name").value(next.getPlayer().getName());
      jsonWriter.name("created").value(DateUtils.format(EXPORT_FORMAT, next.getCreated()));
      jsonWriter.name("source").value(next.getActor().getName());
      jsonWriter.name("expires");

      if (next.getExpires() == 0) {
        jsonWriter.value("forever");
      } else {
        jsonWriter.value(DateUtils.format(EXPORT_FORMAT, next.getExpires()));
      }

      jsonWriter.name("reason").value(next.getReason());

      jsonWriter.endObject();
    }

    itr.closeQuietly();

    jsonWriter.endArray();
    jsonWriter.close();
  }
}
