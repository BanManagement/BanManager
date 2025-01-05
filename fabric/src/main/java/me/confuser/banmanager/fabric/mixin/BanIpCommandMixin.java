package me.confuser.banmanager.fabric.mixin;

import net.minecraft.server.dedicated.command.BanIpCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BanIpCommand.class)
public class BanIpCommandMixin {
    @ModifyArg(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;literal(Ljava/lang/String;)Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;"), index = 0, require = 0)
    private static String banManager_renameCommand(String def) {
        return "minecraft:ban-ip";
    }
}
