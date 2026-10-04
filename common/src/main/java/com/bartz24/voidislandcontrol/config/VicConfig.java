package com.bartz24.voidislandcontrol.config;

import com.bartz24.voidislandcontrol.References;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.world.level.Level;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JSON config at config/voidislandcontrol.json. Same knobs as the 1.12.2 remaster.
 * 1.20.1 has no item metadata, so starting items are {@code namespace:path*count}
 * or {@code namespace:path{snbt}*count}.
 */
public final class VicConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static WorldGenSettings worldGenSettings = new WorldGenSettings();
    public static IslandSettings islandSettings = new IslandSettings();
    public static CommandSettings commandSettings = new CommandSettings();

    private VicConfig() {
    }

    public static Path configDir;

    public static void load(Path dir) {
        configDir = dir;
        Path file = configDir.resolve(References.MODID + ".json");
        try {
            Files.createDirectories(configDir);
            if (Files.exists(file)) {
                try (Reader reader = Files.newBufferedReader(file)) {
                    Root root = GSON.fromJson(reader, Root.class);
                    if (root != null) {
                        if (root.worldGenSettings != null) worldGenSettings = root.worldGenSettings;
                        if (root.islandSettings != null) islandSettings = root.islandSettings;
                        if (root.commandSettings != null) commandSettings = root.commandSettings;
                    }
                }
            }
            save(configDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Void Island Control config", e);
        }
        if (islandSettings.startingItems == null) islandSettings.startingItems = new String[0];
        if (islandSettings.customIslands == null) islandSettings.customIslands = new String[0];
        if (commandSettings.worldLoadCommands == null) commandSettings.worldLoadCommands = new String[0];
        if (commandSettings.visitSettings == null) commandSettings.visitSettings = new VisitSettings();
        if (islandSettings.grassSettings == null) islandSettings.grassSettings = new GrassIslandSettings();
        if (islandSettings.sandSettings == null) islandSettings.sandSettings = new SandIslandSettings();
        if (islandSettings.snowSettings == null) islandSettings.snowSettings = new SnowIslandSettings();
        if (islandSettings.woodSettings == null) islandSettings.woodSettings = new WoodIslandSettings();
        if (islandSettings.gogSettings == null) islandSettings.gogSettings = new GoGSettings();
        if (commandSettings.commandBlockPos == null) commandSettings.commandBlockPos = new CommandBlockPos();
    }

    public static void save(Path configDir) throws IOException {
        Path file = configDir.resolve(References.MODID + ".json");
        Root root = new Root();
        root.worldGenSettings = worldGenSettings;
        root.islandSettings = islandSettings;
        root.commandSettings = commandSettings;
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(root, writer);
        }
    }

    public static final class Root {
        public WorldGenSettings worldGenSettings = new WorldGenSettings();
        public IslandSettings islandSettings = new IslandSettings();
        public CommandSettings commandSettings = new CommandSettings();
    }

    public static final class WorldGenSettings {
        public boolean netherVoid = true;
        public boolean netherVoidStructures = true;
        public boolean endVoid = true;
        public boolean endVoidStructures = true;
        /** Kept for pack configs. 1.20.1 world type is the voidislandcontrol:void preset. */
        public String worldGenType = "VOID";
        public String worldGenSpecialParameters = "";
        /** Biome id used by the void preset comment. Runtime generator uses the preset biome source. */
        public String worldBiome = "minecraft:plains";
        public int cloudLevel = 32;
        public int horizonLevel = 40;
        /** Base dimension for island management. overworld=0, nether=-1, end=1. */
        public int baseDimension = 0;
    }

    public static final class IslandSettings {
        public String islandMainSpawnType = "bedrock";
        public String islandSpawnType = "random";
        public int islandDistance = 1000;
        public int protectionBuildRange = 500;
        public boolean spawnProtection = true;
        public boolean islandProtection = true;
        public int islandSize = 3;
        public boolean spawnChest = false;
        public boolean oneChunk = false;
        public String[] startingItems = new String[0];
        public String islandBiome = "";
        public int islandBiomeRange = 0;
        public int islandYLevel = 88;
        public String bottomBlockType = "BEDROCK";
        public boolean autoCreate = false;
        public boolean autoCreateServersOnly = false;
        public boolean allowIslandCreation = true;
        public boolean resetInventory = true;
        public String[] customIslands = new String[0];
        public boolean forceSpawn = false;
        public int buffTimer = 1200;
        public boolean handleRespawn = true;
        public boolean islandLockdown = false;
        public int islandLockdownRange = 500;
        public GrassIslandSettings grassSettings = new GrassIslandSettings();
        public SandIslandSettings sandSettings = new SandIslandSettings();
        public SnowIslandSettings snowSettings = new SnowIslandSettings();
        public WoodIslandSettings woodSettings = new WoodIslandSettings();
        public GoGSettings gogSettings = new GoGSettings();
    }

    public static final class GrassIslandSettings {
        public boolean enableGrassIsland = true;
        public boolean spawnTree = true;
        public String grassBlockType = "GRASS";
    }

    public static final class SandIslandSettings {
        public boolean enableSandIsland = true;
        public boolean spawnCactus = true;
        public String sandBlockType = "RED";
    }

    public static final class SnowIslandSettings {
        public boolean enableSnowIsland = true;
        public boolean spawnPumpkins = true;
        public boolean spawnIgloo = false;
    }

    public static final class WoodIslandSettings {
        public boolean enableWoodIsland = true;
        public boolean spawnWater = true;
        public boolean spawnString = true;
        public String woodBlockType = "DARKOAK";
    }

    public static final class GoGSettings {
        public boolean enableGoGIsland = true;
    }

    public static final class CommandSettings {
        public String commandName = "island";
        public boolean oneChunkCommandAllowed = false;
        public CommandBlockPos commandBlockPos = new CommandBlockPos();
        public String commandBlockType = "NONE";
        public boolean commandBlockAuto = false;
        public String commandBlockCommand = "";
        public String commandBlockFacing = "UP";
        public boolean allowVisitCommand = true;
        public boolean allowHomeCommand = true;
        public String[] worldLoadCommands = new String[0];
        public VisitSettings visitSettings = new VisitSettings();
    }

    public static final class CommandBlockPos {
        public int x = 0;
        public int y = 0;
        public int z = 0;
    }

    public static final class VisitSettings {
        public boolean allowBlockInteract = false;
        public boolean allowItemUse = false;
        public boolean allowEntityInteract = false;
        public boolean allowAttack = false;
        public boolean allowPickup = false;
        public boolean allowBlockHarvest = false;
        public boolean allowBlockPlace = false;
        public boolean allowPerPlayerOverrides = false;
    }

    public static net.minecraft.resources.ResourceKey<Level> baseLevel() {
        return switch (worldGenSettings.baseDimension) {
            case -1 -> Level.NETHER;
            case 1 -> Level.END;
            default -> Level.OVERWORLD;
        };
    }
}
