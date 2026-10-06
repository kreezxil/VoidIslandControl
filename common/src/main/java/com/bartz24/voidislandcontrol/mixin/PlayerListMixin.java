package com.bartz24.voidislandcontrol.mixin;

import com.bartz24.voidislandcontrol.logic.SpawnHandler;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Inject(method = "respawn", at = @At("RETURN"))
    private void vic$respawn(ServerPlayer player, boolean keepInventory, CallbackInfoReturnable<ServerPlayer> cir) {
        if (cir.getReturnValue() != null) SpawnHandler.onRespawn(cir.getReturnValue());
    }
}
