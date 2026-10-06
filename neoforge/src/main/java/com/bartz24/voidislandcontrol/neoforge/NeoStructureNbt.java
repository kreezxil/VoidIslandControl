package com.bartz24.voidislandcontrol.neoforge;

import com.bartz24.voidislandcontrol.platform.StructureNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.io.InputStream;

public class NeoStructureNbt implements StructureNbt {
    @Override
    public CompoundTag read(InputStream in) throws IOException {
        // 1.20.1 has readCompressed(InputStream) only. The NbtAccounter overload is 1.20.2+.
        return NbtIo.readCompressed(in);
    }
}