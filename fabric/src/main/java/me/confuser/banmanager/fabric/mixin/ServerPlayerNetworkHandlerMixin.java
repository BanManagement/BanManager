package me.confuser.banmanager.fabric.mixin;

//? if >=1.21 {
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;
//?} else {
/*import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
*///?}
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonCommandListener;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayerNetworkHandlerMixin {
  @Shadow
  public ServerPlayerEntity player;

  @Shadow
  public abstract ServerPlayerEntity getPlayer();

  //? if >=1.21 {
  @Inject(method = "handleCommandExecution", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/message/SignedCommandArguments$Impl;<init>(Ljava/util/Map;)V"), cancellable = true)
  private void banManager_checkCommand(ChatCommandSignedC2SPacket packet, LastSeenMessageList lastSeenMessages, CallbackInfo ci) {
    // Split the command
    String[] args = packet.command().split(" ", 6);
    // Get rid of the first /
    String cmd = args[0].replace("/", "").toLowerCase();

    if (new CommonCommandListener(BanManagerPlugin.getInstance()).onCommand(BanManagerPlugin.getInstance().getServer().getPlayer(player.getUuid()), cmd, args)) {
      ci.cancel();
    }
  }
  //?} else {
  /*@Inject(method = "onCommandExecution", at = @At("HEAD"), cancellable = true)
  private void banManager_checkCommand(CommandExecutionC2SPacket packet, CallbackInfo ci) {
    // Split the command
    String[] args = packet.command().split(" ", 6);
    // Get rid of the first /
    String cmd = args[0].replace("/", "").toLowerCase();

    if (new CommonCommandListener(BanManagerPlugin.getInstance()).onCommand(BanManagerPlugin.getInstance().getServer().getPlayer(player.getUuid()), cmd, args)) {
      ci.cancel();
    }
  }
  *///?}

}
