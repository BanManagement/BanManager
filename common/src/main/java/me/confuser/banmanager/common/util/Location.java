package me.confuser.banmanager.common.util;

public class Location {

    private String world;
    private Double x;
    private Double y;
    private Double z;
    private Float yaw;
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
