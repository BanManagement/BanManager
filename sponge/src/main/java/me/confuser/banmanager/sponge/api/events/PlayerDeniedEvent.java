package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;

public class PlayerDeniedEvent extends CustomEvent {

    @Getter
    private PlayerData player;

    @Getter
    private Message message;

    public PlayerDeniedEvent(PlayerData player, Message message) {
        super();
        this.player = player;
        this.message = message;
    }
}




