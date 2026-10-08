package com.bartz24.voidislandcontrol.logic;

import com.bartz24.voidislandcontrol.world.VoidChunkGenerator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VicServerLog {
    private static final Logger LOG = LoggerFactory.getLogger("voidislandcontrol");

    private VicServerLog() {
    }

    public static void onStarted(MinecraftServer server) {
        ServerLevel level = server.overworld();
        var generator = level.getChunkSource().getGenerator();
        String kind = generator instanceof VoidChunkGenerator ? "voidislandcontrol:void" : generator.getClass().getName();
        LOG.info("Preparing start region for Void Island Control ({}) in dimension minecraft:overworld", kind);
    }
}
