package com.bartz24.voidislandcontrol;

import com.bartz24.voidislandcontrol.logic.SpawnHandler;
import com.bartz24.voidislandcontrol.world.VoidChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.RegisterEvent;
import com.bartz24.voidislandcontrol.config.VicConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;

@Mod(References.MODID)
public class VoidIslandControlNeo {
    public VoidIslandControlNeo() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        CommonInit.init(FMLPaths.CONFIGDIR.get());
        modBus.addListener(this::registerCodec);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onLogin);
    }

    private void registerCodec(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.CHUNK_GENERATOR)) {
            event.register(Registries.CHUNK_GENERATOR, VoidChunkGenerator.ID, () -> VoidChunkGenerator.CODEC);
        }
    }

    private void registerCommands(RegisterCommandsEvent event) {
        CommonInit.registerCommands(event.getDispatcher());
    }

    private void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount != 1) return;
        if (!(player.serverLevel().getChunkSource().getGenerator() instanceof com.bartz24.voidislandcontrol.world.VoidChunkGenerator))
            return;
        int y = VicConfig.islandSettings.islandYLevel - 2;
        player.teleportTo(player.serverLevel(), 0.5, y, 0.5, player.getYRot(), player.getXRot());
    }

    private void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SpawnHandler.onJoin(player);
    }
}