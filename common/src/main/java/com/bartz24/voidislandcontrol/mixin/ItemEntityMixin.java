package com.bartz24.voidislandcontrol.mixin;

import com.bartz24.voidislandcontrol.logic.Protection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void vic$pickup(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer server && Protection.denyPickup(server, (ItemEntity) (Object) this)) {
            ci.cancel();
        }
    }
}
