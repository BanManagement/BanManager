package me.confuser.banmanager.common;

@FunctionalInterface
public interface PlaceholderResolver {
    String resolve(CommonPlayer player, String message);
}
