package me.confuser.banmanager.common.util;

import lombok.Getter;

public class Location {

    @Getter
    private String world;
    @Getter
    private Double x;
    @Getter
    private Double y;
    @Getter
    private Double z;
    @Getter
    private Float yaw;
    @Getter
    private Float pitch;

    public Location(String world, Double x, Double y, Double z, Float yaw, Float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

}
