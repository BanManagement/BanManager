package me.confuser.banmanager.fabric.listeners;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.listeners.CommonCommandListener;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;

public class CommandListener {

  private final CommonCommandListener listener;
  @SuppressWarnings("unused")
  private BanManagerPlugin plugin;

  public CommandListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonCommandListener(plugin);

    ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((message, sender, params) -> {
      if (!sender.isExecutedByPlayer()) {
        return true;
      }

      CommonPlayer player = plugin.getServer().getPlayer(sender.getPlayer().getUuid());
      String[] args = message.getSignedContent().split(" ", 6);
      String cmd = args[0].replace("/", "").toLowerCase();

      return listener.onCommand(player, cmd, args);
    });
  }
}
