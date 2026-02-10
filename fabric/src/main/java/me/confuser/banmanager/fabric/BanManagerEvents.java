package me.confuser.banmanager.fabric;

import lombok.Getter;
import lombok.Setter;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.util.Message;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class BanManagerEvents {

  public static final Event<PlayerBanEvent> PLAYER_BAN_EVENT = EventFactory.createArrayBacked(PlayerBanEvent.class,
    (listeners) -> (banData, silent) -> {
      for (PlayerBanEvent listener : listeners) {
        if (listener.onPlayerBan(banData, silent)) {
          return true;
        }
      }
      return false;
    });

  public static final Event<PlayerBannedEvent> PLAYER_BANNED_EVENT = EventFactory.createArrayBacked(PlayerBannedEvent.class,
    (listeners) -> (banData, silent) -> {
      for (PlayerBannedEvent listener : listeners) {
        listener.onPlayerBanned(banData, silent);
      }
    });

  public static final Event<PlayerUnbanEvent> PLAYER_UNBAN_EVENT = EventFactory.createArrayBacked(PlayerUnbanEvent.class,
    (listeners) -> (banData, actor, reason, silent) -> {
      for (PlayerUnbanEvent listener : listeners) {
        listener.onPlayerUnban(banData, actor, reason, silent);
      }
    });

  public static final Event<IpBanEvent> IP_BAN_EVENT = EventFactory.createArrayBacked(IpBanEvent.class,
    (listeners) -> (banData, silent) -> {
      for (IpBanEvent listener : listeners) {
        if (listener.onIpBan(banData, silent)) {
          return true;
        }
      }
      return false;
    });

  public static final Event<IpBannedEvent> IP_BANNED_EVENT = EventFactory.createArrayBacked(IpBannedEvent.class,
    (listeners) -> (banData, silent) -> {
      for (IpBannedEvent listener : listeners) {
        listener.onIpBanned(banData, silent);
      }
    });

  public static final Event<IpUnbanEvent> IP_UNBAN_EVENT = EventFactory.createArrayBacked(IpUnbanEvent.class,
    (listeners) -> (banData, actor, reason, silent) -> {
      for (IpUnbanEvent listener : listeners) {
        listener.onIpUnban(banData, actor, reason, silent);
      }
    });

  public static final Event<IpMuteEvent> IP_MUTE_EVENT = EventFactory.createArrayBacked(IpMuteEvent.class,
    (listeners) -> (muteData, silent) -> {
      for (IpMuteEvent listener : listeners) {
        if (listener.onIpMute(muteData, silent)) {
          return true;
        }
      }
      return false;
    });

  public static final Event<IpMutedEvent> IP_MUTED_EVENT = EventFactory.createArrayBacked(IpMutedEvent.class,
    (listeners) -> (muteData, silent) -> {
      for (IpMutedEvent listener : listeners) {
        listener.onIpMuted(muteData, silent);
      }
    });

  public static final Event<IpUnmutedEvent> IP_UNMUTED_EVENT = EventFactory.createArrayBacked(IpUnmutedEvent.class,
    (listeners) -> (muteData, actor, reason, silent) -> {
      for (IpUnmutedEvent listener : listeners) {
        listener.onIpUnmuted(muteData, actor, reason, silent);
      }
    });

  public static final Event<PlayerKickedEvent> PLAYER_KICKED_EVENT = EventFactory.createArrayBacked(PlayerKickedEvent.class,
    (listeners) -> (kickData, silent) -> {
      for (PlayerKickedEvent listener : listeners) {
        listener.onPlayerKicked(kickData, silent);
      }
    });

  public static final Event<PlayerNoteCreatedEvent> PLAYER_NOTE_CREATED_EVENT = EventFactory.createArrayBacked(PlayerNoteCreatedEvent.class,
    (listeners) -> (noteData) -> {
      for (PlayerNoteCreatedEvent listener : listeners) {
        listener.onPlayerNoteCreated(noteData);
      }
    });

  public static final Event<PlayerReportEvent> PLAYER_REPORT_EVENT = EventFactory.createArrayBacked(PlayerReportEvent.class,
    (listeners) -> (reportData, silent) -> {
      for (PlayerReportEvent listener : listeners) {
        if (listener.onPlayerReport(reportData, silent)) {
          return true;
        }
      }
      return false;
    });

  public static final Event<PlayerReportedEvent> PLAYER_REPORTED_EVENT = EventFactory.createArrayBacked(PlayerReportedEvent.class,
    (listeners) -> (reportData, silent) -> {
      for (PlayerReportedEvent listener : listeners) {
        listener.onPlayerReported(reportData, silent);
      }
    });

  public static final Event<PlayerReportDeletedEvent> PLAYER_REPORT_DELETED_EVENT = EventFactory.createArrayBacked(PlayerReportDeletedEvent.class,
    (listeners) -> (reportData) -> {
      for (PlayerReportDeletedEvent listener : listeners) {
        listener.onPlayerReportDeleted(reportData);
      }
    });

  public static final Event<NameBanEvent> NAME_BAN_EVENT = EventFactory.createArrayBacked(NameBanEvent.class,
    (listeners) -> (banData, silent) -> {
      for (NameBanEvent listener : listeners) {
        if (listener.onNameBan(banData, silent)) {
          return true;
        }
      }
      return false;
    });

  public static final Event<NameBannedEvent> NAME_BANNED_EVENT = EventFactory.createArrayBacked(NameBannedEvent.class,
    (listeners) -> (banData, silent) -> {
      for (NameBannedEvent listener : listeners) {
        listener.onNameBanned(banData, silent);
      }
    });

  public static final Event<NameUnbanEvent> NAME_UNBAN_EVENT = EventFactory.createArrayBacked(NameUnbanEvent.class,
    (listeners) -> (banData, actor, reason, silent) -> {
      for (NameUnbanEvent listener : listeners) {
        listener.onNameUnban(banData, actor, reason, silent);
      }
    });

  public static final Event<PlayerWarnEvent> PLAYER_WARN_EVENT = EventFactory.createArrayBacked(PlayerWarnEvent.class,
    (listeners) -> (warnData, silent) -> {
      for (PlayerWarnEvent listener : listeners) {
        if (listener.onPlayerWarn(warnData, silent)) {
          return true;
        }
      }
      return false;
    });

  public static final Event<PlayerWarnedEvent> PLAYER_WARNED_EVENT = EventFactory.createArrayBacked(PlayerWarnedEvent.class,
    (listeners) -> (warnData, silent) -> {
      for (PlayerWarnedEvent listener : listeners) {
        listener.onPlayerWarned(warnData, silent);
      }
    });

  public static final Event<IpRangeBanEvent> IP_RANGE_BAN_EVENT = EventFactory.createArrayBacked(IpRangeBanEvent.class,
    (listeners) -> (banData, silent) -> {
      for (IpRangeBanEvent listener : listeners) {
        if (listener.onIpRangeBan(banData, silent)) {
          return true;
        }
      }
      return false;
    });

  public static final Event<IpRangeBannedEvent> IP_RANGE_BANNED_EVENT = EventFactory.createArrayBacked(IpRangeBannedEvent.class,
    (listeners) -> (banData, silent) -> {
      for (IpRangeBannedEvent listener : listeners) {
        listener.onIpRangeBanned(banData, silent);
      }
    });

  public static final Event<IpRangeUnbanEvent> IP_RANGE_UNBAN_EVENT = EventFactory.createArrayBacked(IpRangeUnbanEvent.class,
    (listeners) -> (banData, actor, reason, silent) -> {
      for (IpRangeUnbanEvent listener : listeners) {
        listener.onIpRangeUnban(banData, actor, reason, silent);
      }
    });

  public static final Event<PlayerMuteEvent> PLAYER_MUTE_EVENT = EventFactory.createArrayBacked(PlayerMuteEvent.class,
    (listeners) -> (muteData, silent) -> {
      for (PlayerMuteEvent listener : listeners) {
        if (listener.onPlayerMute(muteData, silent)) {
          return true;
        }
      }
      return false;
    });

  public static final Event<PlayerMutedEvent> PLAYER_MUTED_EVENT = EventFactory.createArrayBacked(PlayerMutedEvent.class,
    (listeners) -> (muteData, silent) -> {
      for (PlayerMutedEvent listener : listeners) {
        listener.onPlayerMuted(muteData, silent);
      }
    });

  public static final Event<PlayerUnmuteEvent> PLAYER_UNMUTE_EVENT = EventFactory.createArrayBacked(PlayerUnmuteEvent.class,
    (listeners) -> (muteData, actor, reason, silent) -> {
      for (PlayerUnmuteEvent listener : listeners) {
        listener.onPlayerUnmute(muteData, actor, reason, silent);
      }
    });

  public static final Event<PluginReloadedEvent> PLUGIN_RELOADED_EVENT = EventFactory.createArrayBacked(PluginReloadedEvent.class,
    (listeners) -> (actor) -> {
      for (PluginReloadedEvent listener : listeners) {
        listener.onPluginReloaded(actor);
      }
    });

  public static final Event<PlayerDeniedEvent> PLAYER_DENIED_EVENT = EventFactory.createArrayBacked(PlayerDeniedEvent.class,
    (listeners) -> (player, message) -> {
      for (PlayerDeniedEvent listener : listeners) {
        listener.onPlayerDenied(player, message);
      }
    });

  @FunctionalInterface
  public interface PlayerBanEvent {
    boolean onPlayerBan(PlayerBanData banData, SilentValue silent);
  }

  @FunctionalInterface
  public interface PlayerBannedEvent {
    void onPlayerBanned(PlayerBanData banData, boolean silent);
  }

  @FunctionalInterface
  public interface PlayerUnbanEvent {
    void onPlayerUnban(PlayerBanData banData, PlayerData actor, String reason, boolean silent);
  }

  @FunctionalInterface
  public interface IpBanEvent {
    boolean onIpBan(IpBanData banData, SilentValue silent);
  }

  @FunctionalInterface
  public interface IpBannedEvent {
    void onIpBanned(IpBanData banData, boolean silent);
  }

  @FunctionalInterface
  public interface IpUnbanEvent {
    void onIpUnban(IpBanData banData, PlayerData actor, String reason, boolean silent);
  }

  @FunctionalInterface
  public interface IpMuteEvent {
    boolean onIpMute(IpMuteData muteData, SilentValue silent);
  }

  @FunctionalInterface
  public interface IpMutedEvent {
    void onIpMuted(IpMuteData muteData, boolean silent);
  }

  @FunctionalInterface
  public interface IpUnmutedEvent {
    void onIpUnmuted(IpMuteData muteData, PlayerData actor, String reason, boolean silent);
  }

  @FunctionalInterface
  public interface PlayerKickedEvent {
    void onPlayerKicked(PlayerKickData kickData, boolean silent);
  }

  @FunctionalInterface
  public interface PlayerNoteCreatedEvent {
    void onPlayerNoteCreated(PlayerNoteData noteData);
  }

  @FunctionalInterface
  public interface PlayerReportEvent {
    boolean onPlayerReport(PlayerReportData reportData, SilentValue silent);
  }

  @FunctionalInterface
  public interface PlayerReportedEvent {
    void onPlayerReported(PlayerReportData reportData, boolean silent);
  }

  @FunctionalInterface
  public interface PlayerReportDeletedEvent {
    void onPlayerReportDeleted(PlayerReportData reportData);
  }

  @FunctionalInterface
  public interface NameBanEvent {
    boolean onNameBan(NameBanData banData, SilentValue silent);
  }

  @FunctionalInterface
  public interface NameBannedEvent {
    void onNameBanned(NameBanData banData, boolean silent);
  }

  @FunctionalInterface
  public interface NameUnbanEvent {
    void onNameUnban(NameBanData banData, PlayerData actor, String reason, boolean silent);
  }

  @FunctionalInterface
  public interface PlayerWarnEvent {
    boolean onPlayerWarn(PlayerWarnData warnData, SilentValue silent);
  }

  @FunctionalInterface
  public interface PlayerWarnedEvent {
    void onPlayerWarned(PlayerWarnData warnData, boolean silent);
  }

  @FunctionalInterface
  public interface IpRangeBanEvent {
    boolean onIpRangeBan(IpRangeBanData banData, SilentValue silent);
  }

  @FunctionalInterface
  public interface IpRangeBannedEvent {
    void onIpRangeBanned(IpRangeBanData banData, boolean silent);
  }

  @FunctionalInterface
  public interface IpRangeUnbanEvent {
    void onIpRangeUnban(IpRangeBanData banData, PlayerData actor, String reason, boolean silent);
  }

  @FunctionalInterface
  public interface PlayerMuteEvent {
    boolean onPlayerMute(PlayerMuteData muteData, SilentValue silent);
  }

  @FunctionalInterface
  public interface PlayerMutedEvent {
    void onPlayerMuted(PlayerMuteData muteData, boolean silent);
  }

  @FunctionalInterface
  public interface PlayerUnmuteEvent {
    void onPlayerUnmute(PlayerMuteData muteData, PlayerData actor, String reason, boolean silent);
  }

  @FunctionalInterface
  public interface PluginReloadedEvent {
    void onPluginReloaded(PlayerData actor);
  }

  @FunctionalInterface
  public interface PlayerDeniedEvent {
    void onPlayerDenied(PlayerData player, Message message);
  }

  public static class SilentValue {
    @Getter
    @Setter
    private boolean silent;

    public SilentValue(boolean silent) {
      this.silent = silent;
    }
  }
}
