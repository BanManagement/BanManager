package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import lombok.Setter;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.util.Message;

public class PlayerBannedEvent extends SilentEvent {

    @Getter
    private PlayerBanData ban;

    @Getter
    @Setter
    private Message kickMessage;

    public PlayerBannedEvent(PlayerBanData ban, boolean isSilent) {
        super(isSilent);
        this.ban = ban;
    }
}




