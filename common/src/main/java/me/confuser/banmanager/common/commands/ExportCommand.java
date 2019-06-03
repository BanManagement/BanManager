package me.confuser.banmanager.common.commands;

import com.google.gson.stream.JsonWriter;
import com.j256.ormlite.dao.CloseableIterator;
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
import me.confuser.banmanager.util.IPUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

public class ExportCommand extends SingleCommand {

  private static final FastDateFormat BANNED_JSON_TIME_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd_HH-mm-ss");

  private boolean inProgress = false;

  public ExportCommand(LocaleManager locale) {
    super(CommandSpec.BMEXPORT.localize(locale), "bmactivity", CommandPermission.BMEXPORT, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {
    if (args.size() != 1) return CommandResult.INVALID_ARGS;

    if (inProgress) {
      Message.EXPORT_ERROR_INPROGRESS.send(sender);
      return CommandResult.SUCCESS;
    }

    if (!args.get(0).startsWith("play") && !args.get(0).startsWith("ip")) return CommandResult.INVALID_ARGS;

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      Message finishedMessage;
      String fileName;

      if (args.get(0).startsWith("play")) {
        Message.EXPORT_PLAYER_STARTED.send(sender);
        finishedMessage = Message.EXPORT_PLAYER_FINISHED;
        fileName = "banned-players-" + BANNED_JSON_TIME_FORMAT.format(System.currentTimeMillis()) + ".json";

        try {
          exportPlayers(plugin, fileName);
        } catch (IOException e) {
          Message.SENDER_ERROR_EXCEPTION.send(sender);
          e.printStackTrace();
          return;
        }
      } else if (args.get(0).startsWith("ip")) {
        Message.EXPORT_IP_STARTED.send(sender);
        finishedMessage = Message.EXPORT_IP_FINISHED;
        fileName = "banned-ips-" + BANNED_JSON_TIME_FORMAT.format(System.currentTimeMillis()) + ".json";

        try {
          exportIps(plugin, fileName);
        } catch (IOException e) {
          Message.SENDER_ERROR_EXCEPTION.send(sender);
          e.printStackTrace();
          return;
        }
      } else {
        return;
      }

      finishedMessage.send(sender, "file", fileName);
    });

    return CommandResult.SUCCESS;
  }

  private static final FastDateFormat EXPORT_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss Z");

  private void exportIps(BanManagerPlugin plugin, String fileName) throws IOException {
    File file = new File(plugin.getBootstrap().getDataDirectory().toFile(), fileName);

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

  private void exportPlayers(BanManagerPlugin plugin, String fileName) throws IOException {
    File file = new File(plugin.getBootstrap().getDataDirectory().toFile(), fileName);

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
