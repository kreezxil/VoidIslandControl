package com.bartz24.voidislandcontrol;

import com.bartz24.voidislandcontrol.world.VoidChunkGenerator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class VoidIslandControlFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CommonInit.init(FabricLoader.getInstance().getConfigDir());
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, VoidChunkGenerator.ID, VoidChunkGenerator.CODEC);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                CommonInit.registerCommands(dispatcher));
    }
}
