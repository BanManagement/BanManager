package me.confuser.banmanager.commands;

import com.google.gson.stream.JsonWriter;
import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExportCommand extends BukkitCommand<BanManager> {

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
        String fileName = null;

        if (args[0].startsWith("play")) {
          sender.sendMessage(Message.getString("export.player.started"));
          finishedMessage = Message.get("export.player.finished");
          fileName = "banned-players-" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".json";

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

          fileName = "banned-ips-" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".json";

          finishedMessage.set("file", fileName);

          try {
            exportIps(fileName);
          } catch (IOException e) {
            sender.sendMessage(Message.get("sender.error.exception").toString());
            e.printStackTrace();
            return;
          }
        }

        if (sender != null) {
          finishedMessage.sendTo(sender);
        }
      }

    });

    return true;
  }

  private void exportIps(String fileName) throws IOException {
    File file = new File(plugin.getDataFolder(), fileName);

    if (!file.exists()) {
      file.createNewFile();
    } else {
      throw new IOException("File already exists");
    }

    FileWriter fileWriter = new FileWriter(file.getAbsolutePath());
    JsonWriter jsonWriter = new JsonWriter(fileWriter);

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    jsonWriter.beginArray();
    jsonWriter.setIndent("  ");

    CloseableIterator<IpBanData> itr = plugin.getIpBanStorage().iterator();

    while (itr.hasNext()) {
      IpBanData next = itr.next();

      jsonWriter.beginObject();

      jsonWriter.name("ip").value(IPUtils.toString(next.getIp()));
      jsonWriter.name("created").value(dateFormat.format(new Date(next.getCreated() * 1000L)));
      jsonWriter.name("source").value(next.getActor().getName());
      jsonWriter.name("expires");

      if (next.getExpires() == 0) {
        jsonWriter.value("forever");
      } else {
        jsonWriter.value(dateFormat.format(new Date(next.getExpires() * 1000L)));
      }

      jsonWriter.name("reason").value(next.getReason());

      jsonWriter.endObject();
    }

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

    FileWriter fileWriter = new FileWriter(file.getAbsolutePath());
    JsonWriter jsonWriter = new JsonWriter(fileWriter);

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");


    jsonWriter.beginArray();
    jsonWriter.setIndent("  ");

    CloseableIterator<PlayerBanData> itr = plugin.getPlayerBanStorage().iterator();

    while (itr.hasNext()) {
      PlayerBanData next = itr.next();

      jsonWriter.beginObject();

      jsonWriter.name("uuid").value(next.getPlayer().getUUID().toString());
      jsonWriter.name("name").value(next.getPlayer().getName());
      jsonWriter.name("created").value(dateFormat.format(new Date(next.getCreated() * 1000L)));
      jsonWriter.name("source").value(next.getActor().getName());
      jsonWriter.name("expires");

      if (next.getExpires() == 0) {
        jsonWriter.value("forever");
      } else {
        jsonWriter.value(dateFormat.format(new Date(next.getExpires() * 1000L)));
      }

      jsonWriter.name("reason").value(next.getReason());

      jsonWriter.endObject();
    }

    jsonWriter.endArray();
    jsonWriter.close();
  }
}
