package me.confuser.banmanager.fabric.mixin;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

@Mixin(ServerPlayNetworkHandler.class)
public class SignUpdateMixin {

  @Shadow
  public ServerPlayerEntity player;

  @Inject(method = "onSignUpdate", at = @At("HEAD"), cancellable = true)
  private void banmanager$blockMutedSignEdit(UpdateSignC2SPacket packet, List<?> signText, CallbackInfo ci) {
    BanManagerPlugin plugin = BanManagerPlugin.getInstance();
    if (plugin == null) return;

    // Check player mute
    if (plugin.getPlayerMuteStorage().isMuted(player.getUuid())
        && Permissions.check(player, "bm.block.muted.sign", true)) {
      ci.cancel();
      return;
    }

    // Check IP mute
    InetAddress address = ((InetSocketAddress) player.networkHandler.getConnectionAddress()).getAddress();
    if (plugin.getIpMuteStorage().isMuted(address)
        && Permissions.check(player, "bm.block.ipmuted.sign", true)) {
      ci.cancel();
    }
  }
}
