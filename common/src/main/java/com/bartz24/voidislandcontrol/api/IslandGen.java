package com.bartz24.voidislandcontrol.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public abstract class IslandGen {
    public final String identifier;
    public final BlockPos spawnOffset;

    public IslandGen(String identifier, BlockPos spawnOffset) {
        this.identifier = identifier;
        this.spawnOffset = spawnOffset;
    }

    public abstract void generate(ServerLevel level, BlockPos spawn);
}
