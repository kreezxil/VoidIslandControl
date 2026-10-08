package com.bartz24.voidislandcontrol.data;

import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.api.IslandPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;
import java.nio.file.Path;

public class VicSavedData extends SavedData {
    public static final String NAME = "vic_data";
    private static boolean loaded;

    public static VicSavedData get(ServerLevel level) {
        loadFile(level);
        return new VicSavedData();
    }

    public static void mark(ServerLevel level) {
        if (level == null) return;
        saveFile(level);
    }

    public static void loadFile(ServerLevel level) {
        if (loaded || level == null) return;
        loaded = true;
        Path home = file(level);
        Path stray = level.getServer().getWorldPath(LevelResource.ROOT).resolve("vic_data.dat");
        Path source = Files.isRegularFile(home) ? home : stray;
        if (!Files.isRegularFile(source)) return;
        try {
            CompoundTag nbt = NbtIo.read(source.toFile());
            if (nbt == null) return;
            apply(nbt);
            if (source.equals(stray) && !IslandManager.currentIslands.isEmpty()) saveFile(level);
        } catch (Exception ignored) {
        }
    }

    public static void saveFile(ServerLevel level) {
        if (IslandManager.currentIslands.isEmpty()) return;
        try {
            Path home = file(level);
            Files.createDirectories(home.getParent());
            CompoundTag nbt = new CompoundTag();
            ListTag list = new ListTag();
            for (IslandPos pos : IslandManager.currentIslands) {
                if (pos.getX() == 0 && pos.getY() == 0 && pos.getPlayerUUIDs().isEmpty()) continue;
                list.add(pos.write());
            }
            if (list.isEmpty()) return;
            nbt.put("Positions", list);
            ListTag spawned = new ListTag();
            for (String name : IslandManager.spawnedPlayers) {
                CompoundTag tag = new CompoundTag();
                tag.putString("name", name);
                spawned.add(tag);
            }
            nbt.put("SpawnedPlayers", spawned);
            nbt.putBoolean("oneChunkWorld", IslandManager.worldOneChunk);
            nbt.putInt("initialDist", IslandManager.initialIslandDistance);
            nbt.putBoolean("worldLoaded", IslandManager.worldLoaded);
            NbtIo.write(nbt, home.toFile());
        } catch (Exception ignored) {
        }
    }

    private static void apply(CompoundTag nbt) {
        ListTag list = nbt.getList("Positions", Tag.TAG_COMPOUND);
        if (list.isEmpty() && !IslandManager.currentIslands.isEmpty()) return;
        IslandManager.currentIslands.clear();
        for (int i = 0; i < list.size(); i++) {
            IslandPos pos = IslandPos.read(list.getCompound(i));
            if (pos.getX() == 0 && pos.getY() == 0 && pos.getPlayerUUIDs().isEmpty()) continue;
            IslandManager.currentIslands.add(pos);
        }
        IslandManager.spawnedPlayers.clear();
        ListTag spawned = nbt.getList("SpawnedPlayers", Tag.TAG_COMPOUND);
        for (int i = 0; i < spawned.size(); i++) {
            IslandManager.spawnedPlayers.add(spawned.getCompound(i).getString("name"));
        }
        if (nbt.contains("oneChunkWorld")) IslandManager.worldOneChunk = nbt.getBoolean("oneChunkWorld");
        if (nbt.contains("initialDist")) IslandManager.initialIslandDistance = nbt.getInt("initialDist");
        if (nbt.contains("worldLoaded")) IslandManager.worldLoaded = nbt.getBoolean("worldLoaded");
    }

    private static Path file(ServerLevel level) {
        return level.getServer().getWorldPath(LevelResource.ROOT).resolve("data").resolve("vic_data.dat");
    }

    public VicSavedData() {
    }

    public static VicSavedData load(CompoundTag nbt) {
        return new VicSavedData();
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        return nbt;
    }
}
