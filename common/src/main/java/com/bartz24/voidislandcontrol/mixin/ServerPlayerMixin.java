package com.bartz24.voidislandcontrol.mixin;

import com.bartz24.voidislandcontrol.logic.Protection;
import com.bartz24.voidislandcontrol.logic.SpawnHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
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
    private void vic$destroy(net.minecraft.core.BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (Protection.denyBreak(player, pos)) cir.setReturnValue(false);
    }


}
