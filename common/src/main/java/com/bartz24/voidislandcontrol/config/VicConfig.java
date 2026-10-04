package com.bartz24.voidislandcontrol.config;

import com.bartz24.voidislandcontrol.References;
import net.minecraft.world.level.Level;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TOML config at config/voidislandcontrol.toml. Same file for Forge, NeoForge, and Fabric.
 * 1.20.1 has no item metadata, so starting items are namespace:path*count
 * or namespace:path{snbt}*count.
 */
public final class VicConfig {
    public static WorldGenSettings worldGenSettings = new WorldGenSettings();
    public static IslandSettings islandSettings = new IslandSettings();
    public static CommandSettings commandSettings = new CommandSettings();

    private VicConfig() {
    }

    public static Path configDir;

    public static void load(Path dir) {
        configDir = dir;
        Path file = configDir.resolve(References.MODID + ".toml");
        try {
            Files.createDirectories(configDir);
            if (Files.exists(file)) {
                apply(parse(Files.readString(file)));
            }
            fillNulls();
            save(configDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Void Island Control config", e);
        }
    }

    public static void save(Path configDir) throws IOException {
        Path file = configDir.resolve(References.MODID + ".toml");
        try (Writer writer = Files.newBufferedWriter(file)) {
            writer.write(documented());
        }
    }

    private static void fillNulls() {
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

    private static Map<String, String> parse(String raw) {
        Map<String, String> values = new HashMap<>();
        String section = "";
        for (String line : raw.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                section = trimmed.substring(1, trimmed.length() - 1).trim();
                continue;
            }
            int eq = trimmed.indexOf('=');
            if (eq < 0) continue;
            String key = trimmed.substring(0, eq).trim();
            String value = trimmed.substring(eq + 1).trim();
            values.put(section.isEmpty() ? key : section + "." + key, value);
        }
        return values;
    }

    private static void apply(Map<String, String> v) {
        WorldGenSettings w = worldGenSettings;
        w.netherVoid = bool(v, "worldGenSettings.netherVoid", w.netherVoid);
        w.netherVoidStructures = bool(v, "worldGenSettings.netherVoidStructures", w.netherVoidStructures);
        w.endVoid = bool(v, "worldGenSettings.endVoid", w.endVoid);
        w.endVoidStructures = bool(v, "worldGenSettings.endVoidStructures", w.endVoidStructures);
        w.worldGenType = str(v, "worldGenSettings.worldGenType", w.worldGenType);
        w.worldGenSpecialParameters = str(v, "worldGenSettings.worldGenSpecialParameters", w.worldGenSpecialParameters);
        w.worldBiome = str(v, "worldGenSettings.worldBiome", w.worldBiome);
        w.cloudLevel = num(v, "worldGenSettings.cloudLevel", w.cloudLevel);
        w.horizonLevel = num(v, "worldGenSettings.horizonLevel", w.horizonLevel);
        w.baseDimension = num(v, "worldGenSettings.baseDimension", w.baseDimension);

        IslandSettings s = islandSettings;
        s.islandMainSpawnType = str(v, "islandSettings.islandMainSpawnType", s.islandMainSpawnType);
        s.islandSpawnType = str(v, "islandSettings.islandSpawnType", s.islandSpawnType);
        s.islandDistance = num(v, "islandSettings.islandDistance", s.islandDistance);
        s.protectionBuildRange = num(v, "islandSettings.protectionBuildRange", s.protectionBuildRange);
        s.spawnProtection = bool(v, "islandSettings.spawnProtection", s.spawnProtection);
        s.islandProtection = bool(v, "islandSettings.islandProtection", s.islandProtection);
        s.islandSize = num(v, "islandSettings.islandSize", s.islandSize);
        s.spawnChest = bool(v, "islandSettings.spawnChest", s.spawnChest);
        s.oneChunk = bool(v, "islandSettings.oneChunk", s.oneChunk);
        s.startingItems = arr(v, "islandSettings.startingItems", s.startingItems);
        s.islandBiome = str(v, "islandSettings.islandBiome", s.islandBiome);
        s.islandBiomeRange = num(v, "islandSettings.islandBiomeRange", s.islandBiomeRange);
        s.islandYLevel = num(v, "islandSettings.islandYLevel", s.islandYLevel);
        s.bottomBlockType = str(v, "islandSettings.bottomBlockType", s.bottomBlockType);
        s.autoCreate = bool(v, "islandSettings.autoCreate", s.autoCreate);
        s.autoCreateServersOnly = bool(v, "islandSettings.autoCreateServersOnly", s.autoCreateServersOnly);
        s.allowIslandCreation = bool(v, "islandSettings.allowIslandCreation", s.allowIslandCreation);
        s.resetInventory = bool(v, "islandSettings.resetInventory", s.resetInventory);
        s.customIslands = arr(v, "islandSettings.customIslands", s.customIslands);
        s.forceSpawn = bool(v, "islandSettings.forceSpawn", s.forceSpawn);
        s.buffTimer = num(v, "islandSettings.buffTimer", s.buffTimer);
        s.handleRespawn = bool(v, "islandSettings.handleRespawn", s.handleRespawn);
        s.islandLockdown = bool(v, "islandSettings.islandLockdown", s.islandLockdown);
        s.islandLockdownRange = num(v, "islandSettings.islandLockdownRange", s.islandLockdownRange);
        s.grassSettings.enableGrassIsland = bool(v, "islandSettings.grassSettings.enableGrassIsland", s.grassSettings.enableGrassIsland);
        s.grassSettings.spawnTree = bool(v, "islandSettings.grassSettings.spawnTree", s.grassSettings.spawnTree);
        s.grassSettings.grassBlockType = str(v, "islandSettings.grassSettings.grassBlockType", s.grassSettings.grassBlockType);
        s.sandSettings.enableSandIsland = bool(v, "islandSettings.sandSettings.enableSandIsland", s.sandSettings.enableSandIsland);
        s.sandSettings.spawnCactus = bool(v, "islandSettings.sandSettings.spawnCactus", s.sandSettings.spawnCactus);
        s.sandSettings.sandBlockType = str(v, "islandSettings.sandSettings.sandBlockType", s.sandSettings.sandBlockType);
        s.snowSettings.enableSnowIsland = bool(v, "islandSettings.snowSettings.enableSnowIsland", s.snowSettings.enableSnowIsland);
        s.snowSettings.spawnPumpkins = bool(v, "islandSettings.snowSettings.spawnPumpkins", s.snowSettings.spawnPumpkins);
        s.snowSettings.spawnIgloo = bool(v, "islandSettings.snowSettings.spawnIgloo", s.snowSettings.spawnIgloo);
        s.woodSettings.enableWoodIsland = bool(v, "islandSettings.woodSettings.enableWoodIsland", s.woodSettings.enableWoodIsland);
        s.woodSettings.spawnWater = bool(v, "islandSettings.woodSettings.spawnWater", s.woodSettings.spawnWater);
        s.woodSettings.spawnString = bool(v, "islandSettings.woodSettings.spawnString", s.woodSettings.spawnString);
        s.woodSettings.woodBlockType = str(v, "islandSettings.woodSettings.woodBlockType", s.woodSettings.woodBlockType);
        s.gogSettings.enableGoGIsland = bool(v, "islandSettings.gogSettings.enableGoGIsland", s.gogSettings.enableGoGIsland);

        CommandSettings c = commandSettings;
        c.commandName = str(v, "commandSettings.commandName", c.commandName);
        c.oneChunkCommandAllowed = bool(v, "commandSettings.oneChunkCommandAllowed", c.oneChunkCommandAllowed);
        c.commandBlockPos.x = num(v, "commandSettings.commandBlockPos.x", c.commandBlockPos.x);
        c.commandBlockPos.y = num(v, "commandSettings.commandBlockPos.y", c.commandBlockPos.y);
        c.commandBlockPos.z = num(v, "commandSettings.commandBlockPos.z", c.commandBlockPos.z);
        c.commandBlockType = str(v, "commandSettings.commandBlockType", c.commandBlockType);
        c.commandBlockAuto = bool(v, "commandSettings.commandBlockAuto", c.commandBlockAuto);
        c.commandBlockCommand = str(v, "commandSettings.commandBlockCommand", c.commandBlockCommand);
        c.commandBlockFacing = str(v, "commandSettings.commandBlockFacing", c.commandBlockFacing);
        c.allowVisitCommand = bool(v, "commandSettings.allowVisitCommand", c.allowVisitCommand);
        c.allowHomeCommand = bool(v, "commandSettings.allowHomeCommand", c.allowHomeCommand);
        c.worldLoadCommands = arr(v, "commandSettings.worldLoadCommands", c.worldLoadCommands);
        VisitSettings visit = c.visitSettings;
        visit.allowBlockInteract = bool(v, "commandSettings.visitSettings.allowBlockInteract", visit.allowBlockInteract);
        visit.allowItemUse = bool(v, "commandSettings.visitSettings.allowItemUse", visit.allowItemUse);
        visit.allowEntityInteract = bool(v, "commandSettings.visitSettings.allowEntityInteract", visit.allowEntityInteract);
        visit.allowAttack = bool(v, "commandSettings.visitSettings.allowAttack", visit.allowAttack);
        visit.allowPickup = bool(v, "commandSettings.visitSettings.allowPickup", visit.allowPickup);
        visit.allowBlockHarvest = bool(v, "commandSettings.visitSettings.allowBlockHarvest", visit.allowBlockHarvest);
        visit.allowBlockPlace = bool(v, "commandSettings.visitSettings.allowBlockPlace", visit.allowBlockPlace);
        visit.allowPerPlayerOverrides = bool(v, "commandSettings.visitSettings.allowPerPlayerOverrides", visit.allowPerPlayerOverrides);
    }

    private static boolean bool(Map<String, String> v, String key, boolean fallback) {
        String raw = v.get(key);
        return raw == null ? fallback : Boolean.parseBoolean(raw);
    }

    private static int num(Map<String, String> v, String key, int fallback) {
        String raw = v.get(key);
        if (raw == null) return fallback;
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String str(Map<String, String> v, String key, String fallback) {
        String raw = v.get(key);
        if (raw == null) return fallback;
        if (raw.length() >= 2 && raw.startsWith("\"") && raw.endsWith("\"")) {
            return raw.substring(1, raw.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return raw;
    }

    private static String[] arr(Map<String, String> v, String key, String[] fallback) {
        String raw = v.get(key);
        if (raw == null) return fallback;
        String body = raw.trim();
        if (body.startsWith("[")) body = body.substring(1);
        if (body.endsWith("]")) body = body.substring(0, body.length() - 1);
        if (body.isBlank()) return new String[0];
        List<String> items = new ArrayList<>();
        for (String part : body.split(",")) {
            String item = part.trim();
            if (item.length() >= 2 && item.startsWith("\"") && item.endsWith("\"")) {
                item = item.substring(1, item.length() - 1);
            }
            if (!item.isEmpty()) items.add(item);
        }
        return items.toArray(String[]::new);
    }

    private static String documented() {
        WorldGenSettings w = worldGenSettings;
        IslandSettings s = islandSettings;
        CommandSettings c = commandSettings;
        VisitSettings visit = c.visitSettings;
        return """
                # Void Island Control. Forge, NeoForge, and Fabric all use config/voidislandcontrol.toml
                # Lines starting with # are comments. Restart after edits. Delete this file to regenerate defaults.
                # Create the world with the Void Island preset, or set level-type=voidislandcontrol:void on a dedicated server.

                [worldGenSettings]
                # Void the nether. Fortresses still place when netherVoidStructures is true.
                netherVoid = %s
                netherVoidStructures = %s
                # Void the end. End cities still place when endVoidStructures is true.
                endVoid = %s
                endVoidStructures = %s
                # Kept from 1.12. The 1.20.1 world is the voidislandcontrol:void preset, not a world type.
                worldGenType = %s
                worldGenSpecialParameters = %s
                # Biome id. The preset biome source is what generation actually uses.
                worldBiome = %s
                cloudLevel = %d
                horizonLevel = %d
                # Island home dimension. 0 overworld, -1 nether, 1 end.
                baseDimension = %d

                [islandSettings]
                # Spawn pad: bedrock, grass, or the island type's own top block.
                islandMainSpawnType = %s
                # random, grid, or spiral placement of new islands.
                islandSpawnType = %s
                # Blocks between island centers.
                islandDistance = %d
                # Build and break radius around an island center. 0 disables range checks.
                protectionBuildRange = %d
                spawnProtection = %s
                islandProtection = %s
                # Pad width in blocks. 3 is a 3x3.
                islandSize = %d
                spawnChest = %s
                # New islands are one chunk. Also the /island onechunk admin toggle.
                oneChunk = %s
                # namespace:path*count or namespace:path{snbt}*count. No item metadata in 1.20.1.
                startingItems = %s
                islandBiome = %s
                islandBiomeRange = %d
                # Y of the island surface.
                islandYLevel = %d
                # BEDROCK or SECONDARYBLOCK for the layer under the pad.
                bottomBlockType = %s
                autoCreate = %s
                autoCreateServersOnly = %s
                allowIslandCreation = %s
                resetInventory = %s
                # Structure nbt names in config/voidislandcontrolstructures.
                customIslands = %s
                forceSpawn = %s
                # Spawn buff length in ticks. 1200 is 60 seconds.
                buffTimer = %d
                handleRespawn = %s
                islandLockdown = %s
                islandLockdownRange = %d

                [islandSettings.grassSettings]
                enableGrassIsland = %s
                spawnTree = %s
                # GRASS, DIRT, or COARSEDIRT.
                grassBlockType = %s

                [islandSettings.sandSettings]
                enableSandIsland = %s
                spawnCactus = %s
                # NORMAL or RED.
                sandBlockType = %s

                [islandSettings.snowSettings]
                enableSnowIsland = %s
                spawnPumpkins = %s
                spawnIgloo = %s

                [islandSettings.woodSettings]
                enableWoodIsland = %s
                spawnWater = %s
                spawnString = %s
                # OAK, SPRUCE, BIRCH, JUNGLE, ACACIA, or DARKOAK.
                woodBlockType = %s

                [islandSettings.gogSettings]
                # Garden of Glass pad. Uses Botania blocks when that mod is loaded, otherwise a pebble island.
                enableGoGIsland = %s

                [commandSettings]
                # Base command name. Default is /island.
                commandName = %s
                oneChunkCommandAllowed = %s
                # NONE, IMPULSE, REPEATING, or CHAIN. Placed on a new island when not NONE.
                commandBlockType = %s
                commandBlockAuto = %s
                commandBlockCommand = %s
                commandBlockFacing = %s
                allowVisitCommand = %s
                allowHomeCommand = %s
                # Commands run once when the world first loads. Use @p for the first player.
                worldLoadCommands = %s

                [commandSettings.commandBlockPos]
                # Offset from the island spawn for the command block.
                x = %d
                y = %d
                z = %d

                [commandSettings.visitSettings]
                # Defaults for visitors. Per-player overrides apply only when allowPerPlayerOverrides is true.
                allowBlockInteract = %s
                allowItemUse = %s
                allowEntityInteract = %s
                allowAttack = %s
                allowPickup = %s
                allowBlockHarvest = %s
                allowBlockPlace = %s
                allowPerPlayerOverrides = %s
                """.formatted(
                w.netherVoid, w.netherVoidStructures, w.endVoid, w.endVoidStructures,
                q(w.worldGenType), q(w.worldGenSpecialParameters), q(w.worldBiome),
                w.cloudLevel, w.horizonLevel, w.baseDimension,
                q(s.islandMainSpawnType), q(s.islandSpawnType), s.islandDistance, s.protectionBuildRange,
                s.spawnProtection, s.islandProtection, s.islandSize, s.spawnChest, s.oneChunk,
                arr(s.startingItems), q(s.islandBiome), s.islandBiomeRange, s.islandYLevel, q(s.bottomBlockType),
                s.autoCreate, s.autoCreateServersOnly, s.allowIslandCreation, s.resetInventory, arr(s.customIslands),
                s.forceSpawn, s.buffTimer, s.handleRespawn, s.islandLockdown, s.islandLockdownRange,
                s.grassSettings.enableGrassIsland, s.grassSettings.spawnTree, q(s.grassSettings.grassBlockType),
                s.sandSettings.enableSandIsland, s.sandSettings.spawnCactus, q(s.sandSettings.sandBlockType),
                s.snowSettings.enableSnowIsland, s.snowSettings.spawnPumpkins, s.snowSettings.spawnIgloo,
                s.woodSettings.enableWoodIsland, s.woodSettings.spawnWater, s.woodSettings.spawnString, q(s.woodSettings.woodBlockType),
                s.gogSettings.enableGoGIsland,
                q(c.commandName), c.oneChunkCommandAllowed,
                q(c.commandBlockType), c.commandBlockAuto, q(c.commandBlockCommand), q(c.commandBlockFacing),
                c.allowVisitCommand, c.allowHomeCommand, arr(c.worldLoadCommands),
                c.commandBlockPos.x, c.commandBlockPos.y, c.commandBlockPos.z,
                visit.allowBlockInteract, visit.allowItemUse, visit.allowEntityInteract, visit.allowAttack,
                visit.allowPickup, visit.allowBlockHarvest, visit.allowBlockPlace, visit.allowPerPlayerOverrides
        );
    }

    private static String q(String value) {
        return "\"" + (value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"")) + "\"";
    }

    private static String arr(String[] values) {
        if (values == null || values.length == 0) return "[]";
        StringBuilder out = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) out.append(", ");
            out.append(q(values[i]));
        }
        return out.append(']').toString();
    }

    public static final class WorldGenSettings {
        public boolean netherVoid = true;
        public boolean netherVoidStructures = true;
        public boolean endVoid = true;
        public boolean endVoidStructures = true;
        public String worldGenType = "VOID";
        public String worldGenSpecialParameters = "";
        public String worldBiome = "minecraft:plains";
        public int cloudLevel = 32;
        public int horizonLevel = 40;
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
        public boolean handleRespawn = true;
        public int buffTimer = 1200;
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
