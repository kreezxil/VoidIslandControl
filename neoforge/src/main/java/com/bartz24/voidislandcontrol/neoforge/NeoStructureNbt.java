package com.bartz24.voidislandcontrol.neoforge;

import com.bartz24.voidislandcontrol.platform.StructureNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.io.InputStream;

public class NeoStructureNbt implements StructureNbt {
    @Override
    public CompoundTag read(InputStream in) throws IOException {
        return NbtIo.readCompressed(in, accounter());
    }

    private static NbtAccounter accounter() {
        try {
            return NbtAccounter.class.getConstructor(long.class, int.class).newInstance(0x20000000L, 512);
        } catch (ReflectiveOperationException ignored) {
            try {
                return NbtAccounter.class.getConstructor(long.class).newInstance(0x20000000L);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Cannot build NbtAccounter", e);
            }
        }
    }
}
