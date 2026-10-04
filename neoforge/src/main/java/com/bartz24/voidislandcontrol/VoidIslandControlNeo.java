package com.bartz24.voidislandcontrol;

import com.bartz24.voidislandcontrol.world.VoidChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.minecraft.core.registries.Registries;

@Mod(References.MODID)
public class VoidIslandControlNeo {
    public VoidIslandControlNeo(IEventBus modBus) {
        CommonInit.init(FMLPaths.CONFIGDIR.get());
        modBus.addListener(this::registerCodec);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void registerCodec(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.CHUNK_GENERATOR)) {
            event.register(Registries.CHUNK_GENERATOR, VoidChunkGenerator.ID, () -> VoidChunkGenerator.CODEC);
        }
    }

    private void registerCommands(RegisterCommandsEvent event) {
        CommonInit.registerCommands(event.getDispatcher());
    }
}
