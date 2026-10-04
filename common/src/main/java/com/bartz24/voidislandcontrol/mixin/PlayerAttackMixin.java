package com.bartz24.voidislandcontrol.mixin;

import com.bartz24.voidislandcontrol.logic.Protection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerAttackMixin {
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void vic$attack(Entity target, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player && Protection.denyAttack(player, target)) {
            ci.cancel();
        }
    }
}
