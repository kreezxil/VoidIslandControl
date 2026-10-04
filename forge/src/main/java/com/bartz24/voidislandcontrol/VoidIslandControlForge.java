package com.bartz24.voidislandcontrol;

import com.bartz24.voidislandcontrol.world.VoidChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.RegisterEvent;

@Mod(References.MODID)
public class VoidIslandControlForge {
    public VoidIslandControlForge() {
        CommonInit.init(FMLPaths.CONFIGDIR.get());
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::registerCodec);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
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
