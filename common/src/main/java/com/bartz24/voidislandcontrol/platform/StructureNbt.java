package com.bartz24.voidislandcontrol.platform;

import net.minecraft.nbt.CompoundTag;

import java.io.IOException;
import java.io.InputStream;
import java.util.ServiceLoader;

public interface StructureNbt {
    CompoundTag read(InputStream in) throws IOException;

    StructureNbt INSTANCE = ServiceLoader.load(StructureNbt.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No StructureNbt service"));
}
