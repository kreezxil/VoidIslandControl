package com.bartz24.voidislandcontrol.data;

import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.api.IslandPos;
import com.bartz24.voidislandcontrol.config.VicConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Supplier;

public class VicSavedData extends SavedData {
    public static final String NAME = "vic_data";

    public static VicSavedData get(ServerLevel level) {
        Object storage = level.getDataStorage();
        Supplier<VicSavedData> constructor = VicSavedData::new;
        Function<CompoundTag, VicSavedData> loader = VicSavedData::load;
        try {
            for (Method method : storage.getClass().getMethods()) {
                if (!method.getName().equals("computeIfAbsent") || method.getParameterCount() != 2) continue;
                Class<?> factoryType = method.getParameterTypes()[0];
                if (factoryType == String.class) continue;
                Constructor<?> factoryCtor = factoryType.getConstructor(Supplier.class, Function.class);
                Object factory = factoryCtor.newInstance(constructor, loader);
                return (VicSavedData) method.invoke(storage, factory, NAME);
            }
            Method legacy = storage.getClass().getMethod("computeIfAbsent", Function.class, Supplier.class, String.class);
            return (VicSavedData) legacy.invoke(storage, loader, constructor, NAME);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot load vic_data", e);
        }
    }
    public VicSavedData() {
    }

    public static VicSavedData load(CompoundTag nbt) {
        VicSavedData data = new VicSavedData();
        IslandManager.currentIslands.clear();
        IslandManager.spawnedPlayers.clear();
        IslandManager.worldOneChunk = false;
        IslandManager.initialIslandDistance = VicConfig.islandSettings.islandDistance;
        ListTag list = nbt.getList("Positions", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            IslandManager.currentIslands.add(IslandPos.read(list.getCompound(i)));
        }
        ListTag spawned = nbt.getList("SpawnedPlayers", Tag.TAG_COMPOUND);
        for (int i = 0; i < spawned.size(); i++) {
            IslandManager.spawnedPlayers.add(spawned.getCompound(i).getString("name"));
        }
        if (nbt.contains("oneChunkWorld")) IslandManager.worldOneChunk = nbt.getBoolean("oneChunkWorld");
        if (nbt.contains("initialDist")) IslandManager.initialIslandDistance = nbt.getInt("initialDist");
        if (nbt.contains("worldLoaded")) IslandManager.worldLoaded = nbt.getBoolean("worldLoaded");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (IslandPos pos : IslandManager.currentIslands) list.add(pos.write());
        nbt.put("Positions", list);
        ListTag spawned = new ListTag();
        for (String name : IslandManager.spawnedPlayers) {
            CompoundTag tag = new CompoundTag();
            tag.putString("name", name);
            spawned.add(tag);
        }
        nbt.put("SpawnedPlayers", spawned);
        if (IslandManager.worldOneChunk) nbt.putBoolean("oneChunkWorld", true);
        nbt.putInt("initialDist", IslandManager.initialIslandDistance);
        nbt.putBoolean("worldLoaded", IslandManager.worldLoaded);
        return nbt;
    }

    public static void mark(ServerLevel level) {
        if (level != null) get(level).setDirty();
    }
}
