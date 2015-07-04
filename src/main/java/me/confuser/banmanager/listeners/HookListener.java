package me.confuser.banmanager.listeners;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.Hook;
import me.confuser.banmanager.configs.HooksConfig;
import me.confuser.banmanager.events.*;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.listeners.Listeners;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class HookListener extends Listeners<BanManager> {

  private HooksConfig config = plugin.getConfiguration().getHooksConfig();

  // TODO refactor to reduce duplicate code
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBan(final PlayerBanEvent event) {
    final Hook hook;

    if (event.getBan().getExpires() == 0) {
      hook = config.getHook("ban");
    } else {
      hook = config.getHook("tempban");
    }

    if (hook == null) return;

    if (hook.getPre().size() != 0) {
      plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

        @Override
        public void run() {
          for (String command : hook.getPre()) {
            String hookCommnad = command
                    .replace("[player]", event.getBan().getPlayer().getName())
                    .replace("[actor]", event.getBan().getActor().getName())
                    .replace("[reason]", event.getBan().getReason());

            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), hookCommnad);
          }
        }

      });
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBan(final PlayerBannedEvent event) {
    final Hook hook;

    if (event.getBan().getExpires() == 0) {
      hook = config.getHook("ban");
    } else {
      hook = config.getHook("tempban");
    }

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

        @Override
        public void run() {
          for (String command : hook.getPost()) {
            String hookCommnad = command
                    .replace("[player]", event.getBan().getPlayer().getName())
                    .replace("[actor]", event.getBan().getActor().getName())
                    .replace("[reason]", event.getBan().getReason());

            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), hookCommnad);
          }
        }

      });
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onMute(final PlayerMuteEvent event) {
    final Hook hook;

    if (event.getMute().getExpires() == 0) {
      hook = config.getHook("mute");
    } else {
      hook = config.getHook("tempmute");
    }

    if (hook == null) return;

    if (hook.getPre().size() != 0) {
      plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

        @Override
        public void run() {
          for (String command : hook.getPre()) {
            String hookCommnad = command
                    .replace("[player]", event.getMute().getPlayer().getName())
                    .replace("[actor]", event.getMute().getActor().getName())
                    .replace("[reason]", event.getMute().getReason());

            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), hookCommnad);
          }
        }

      });
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onMute(final PlayerMutedEvent event) {
    final Hook hook;

    if (event.getMute().getExpires() == 0) {
      hook = config.getHook("mute");
    } else {
      hook = config.getHook("tempmute");
    }

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

        @Override
        public void run() {
          for (String command : hook.getPost()) {
            String hookCommnad = command
                    .replace("[player]", event.getMute().getPlayer().getName())
                    .replace("[actor]", event.getMute().getActor().getName())
                    .replace("[reason]", event.getMute().getReason());

            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), hookCommnad);
          }
        }

      });
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBan(final IpBanEvent event) {
    final Hook hook;

    if (event.getBan().getExpires() == 0) {
      hook = config.getHook("ipban");
    } else {
      hook = config.getHook("tempipban");
    }

    if (hook == null) return;

    if (hook.getPre().size() != 0) {
      plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

        @Override
        public void run() {
          for (String command : hook.getPre()) {
            String hookCommnad = command
                    .replace("[ip]", IPUtils.toString(event.getBan().getIp()))
                    .replace("[actor]", event.getBan().getActor().getName())
                    .replace("[reason]", event.getBan().getReason());

            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), hookCommnad);
          }
        }

      });
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBan(final IpBannedEvent event) {
    final Hook hook;

    if (event.getBan().getExpires() == 0) {
      hook = config.getHook("ipban");
    } else {
      hook = config.getHook("tempipban");
    }

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

        @Override
        public void run() {
          for (String command : hook.getPost()) {
            String hookCommnad = command
                    .replace("[player]", IPUtils.toString(event.getBan().getIp()))
                    .replace("[actor]", event.getBan().getActor().getName())
                    .replace("[reason]", event.getBan().getReason());

            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), hookCommnad);
          }
        }

      });
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBan(final IpRangeBanEvent event) {
    final Hook hook;

    if (event.getBan().getExpires() == 0) {
      hook = config.getHook("iprangeban");
    } else {
      hook = config.getHook("temprangeipban");
    }

    if (hook == null) return;

    if (hook.getPre().size() != 0) {
      plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

        @Override
        public void run() {
          for (String command : hook.getPre()) {
            String hookCommnad = command
                    .replace("[from]", IPUtils.toString(event.getBan().getFromIp()))
                    .replace("[to]", IPUtils.toString(event.getBan().getToIp()))
                    .replace("[actor]", event.getBan().getActor().getName())
                    .replace("[reason]", event.getBan().getReason());

            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), hookCommnad);
          }
        }

      });
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBan(final IpRangeBannedEvent event) {
    final Hook hook;

    if (event.getBan().getExpires() == 0) {
      hook = config.getHook("iprangeban");
    } else {
      hook = config.getHook("temprangeipban");
    }

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

        @Override
        public void run() {
          for (String command : hook.getPost()) {
            String hookCommnad = command
                    .replace("[from]", IPUtils.toString(event.getBan().getFromIp()))
                    .replace("[to]", IPUtils.toString(event.getBan().getToIp()))
                    .replace("[actor]", event.getBan().getActor().getName())
                    .replace("[reason]", event.getBan().getReason());

            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), hookCommnad);
          }
        }

      });
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onWarn(final PlayerWarnEvent event) {
    final Hook hook = config.getHook("warn");

    if (hook == null) return;

    if (hook.getPre().size() != 0) {
      plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

        @Override
        public void run() {
          for (String command : hook.getPre()) {
            String hookCommnad = command
                    .replace("[player]", event.getWarning().getPlayer().getName())
                    .replace("[actor]", event.getWarning().getActor().getName())
                    .replace("[reason]", event.getWarning().getReason());

            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), hookCommnad);
          }
        }

      });
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onWarn(final PlayerWarnedEvent event) {
    final Hook hook = config.getHook("warn");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

        @Override
        public void run() {
          for (String command : hook.getPost()) {
            String hookCommnad = command
                    .replace("[player]", event.getWarning().getPlayer().getName())
                    .replace("[actor]", event.getWarning().getActor().getName())
                    .replace("[reason]", event.getWarning().getReason());

            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), hookCommnad);
          }
        }

      });
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onNote(final PlayerNoteCreatedEvent event) {
    final Hook hook = config.getHook("note");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

        @Override
        public void run() {
          for (String command : hook.getPost()) {
            String hookCommnad = command
                    .replace("[player]", event.getNote().getPlayer().getName())
                    .replace("[actor]", event.getNote().getActor().getName())
                    .replace("[message]", event.getNote().getMessage());

            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), hookCommnad);
          }
        }

      });
    }
  }
}
