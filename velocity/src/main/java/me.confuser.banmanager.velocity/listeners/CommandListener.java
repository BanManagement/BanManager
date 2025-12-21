package me.confuser.banmanager.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.listeners.CommonCommandListener;
import me.confuser.banmanager.velocity.Listener;

import java.util.Arrays;

public class CommandListener extends Listener {
    private final CommonCommandListener listener;
    private final BanManagerPlugin plugin;

    public CommandListener(BanManagerPlugin plugin) {
        this.plugin = plugin;
        this.listener = new CommonCommandListener(plugin);
    }

    @Subscribe
    public void onCommand(CommandExecuteEvent event) {
        if(event.getCommandSource() instanceof Player) {
            CommonPlayer commonPlayer = plugin.getServer().getPlayer(((Player) event.getCommandSource()).getUniqueId());
            String[] command = event.getCommand().split(" ", 7);

            if (listener.onCommand(commonPlayer, command[0], Arrays.copyOfRange(command, 1, command.length))) {
                event.setResult(CommandExecuteEvent.CommandResult.denied());
            }
        }
    }
}
