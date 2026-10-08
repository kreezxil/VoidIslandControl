package com.bartz24.voidislandcontrol.mixin;

import com.bartz24.voidislandcontrol.logic.Protection;
import com.bartz24.voidislandcontrol.logic.SpawnHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void vic$tick(CallbackInfo ci) {
        SpawnHandler.tick((ServerPlayer) (Object) this);
    }
}

@Mixin(ServerPlayerGameMode.class)
class ServerPlayerGameModeMixin {
    @Shadow
    protected ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void vic$destroy(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!Protection.denyBreak(player, pos)) return;
        cir.setReturnValue(false);
        if (player.level() instanceof ServerLevel level) {
            var state = level.getBlockState(pos);
            player.connection.send(new ClientboundBlockUpdatePacket(pos, state));
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }
}
