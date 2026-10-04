package com.bartz24.voidislandcontrol.forge;

import com.bartz24.voidislandcontrol.platform.StructureNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.io.InputStream;

public class ForgeStructureNbt implements StructureNbt {
    @Override
    public CompoundTag read(InputStream in) throws IOException {
        return NbtIo.readCompressed(in);
    }
}
