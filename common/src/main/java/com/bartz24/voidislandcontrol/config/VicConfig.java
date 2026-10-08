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
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * TOML config at config/voidislandcontrol.toml. Same file for Forge, NeoForge, and Fabric.
 * 1.20.1 has no item metadata, so starting items are namespace:path*count
 * or namespace:path{snbt}*count.
 *
 * Add an option by adding one entry in entries(). Comment, section, key, and the field
 * sit on that one line. Do not add a second copy in a save or load method.
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
            Map<String, String> values = Files.exists(file) ? parse(Files.readString(file)) : Map.of();
            for (Entry entry : entries()) entry.read(values);
            save(configDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Void Island Control config", e);
        }
    }

    public static void save(Path configDir) throws IOException {
        Path file = configDir.resolve(References.MODID + ".toml");
        try (Writer writer = Files.newBufferedWriter(file)) {
            writer.write("# Void Island Control. Forge, NeoForge, and Fabric all use config/voidislandcontrol.toml\n");
            writer.write("# Lines starting with # are comments. Restart after edits. Delete this file to regenerate defaults.\n");
            writer.write("# Create the world with the Void Island preset, or set level-type=voidislandcontrol:void on a dedicated server.\n");
            String section = "";
            for (Entry entry : entries()) {
                if (!entry.section.equals(section)) {
                    section = entry.section;
                    writer.write("\n[" + section + "]\n");
                }
                if (entry.comment != null && !entry.comment.isBlank()) {
                    writer.write("# " + entry.comment + "\n");
                }
                writer.write(entry.key + " = " + entry.write() + "\n");
            }
        }
    }

    private static List<Entry> entries() {
        WorldGenSettings w = worldGenSettings;
        IslandSettings s = islandSettings;
        CommandSettings c = commandSettings;
        VisitSettings visit = c.visitSettings;
        List<Entry> list = new ArrayList<>();

        list.add(bool("worldGenSettings", "netherVoid", "Void the nether. Fortresses still place when netherVoidStructures is true.", () -> w.netherVoid, v -> w.netherVoid = v));
        list.add(bool("worldGenSettings", "netherVoidStructures", "", () -> w.netherVoidStructures, v -> w.netherVoidStructures = v));
        list.add(bool("worldGenSettings", "endVoid", "Void the end. End cities still place when endVoidStructures is true.", () -> w.endVoid, v -> w.endVoid = v));
        list.add(bool("worldGenSettings", "endVoidStructures", "", () -> w.endVoidStructures, v -> w.endVoidStructures = v));
        list.add(str("worldGenSettings", "worldGenType", "Kept from 1.12. The 1.20.1 world is the voidislandcontrol:void preset, not a world type.", () -> w.worldGenType, v -> w.worldGenType = v));
        list.add(str("worldGenSettings", "worldGenSpecialParameters", "", () -> w.worldGenSpecialParameters, v -> w.worldGenSpecialParameters = v));
        list.add(str("worldGenSettings", "worldBiome", "Biome id. The preset biome source is what generation actually uses.", () -> w.worldBiome, v -> w.worldBiome = v));
        list.add(num("worldGenSettings", "cloudLevel", "", () -> w.cloudLevel, v -> w.cloudLevel = v));
        list.add(num("worldGenSettings", "horizonLevel", "", () -> w.horizonLevel, v -> w.horizonLevel = v));
        list.add(num("worldGenSettings", "baseDimension", "Island home dimension. 0 overworld, -1 nether, 1 end.", () -> w.baseDimension, v -> w.baseDimension = v));

        list.add(str("islandSettings", "islandMainSpawnType", "Spawn pad: bedrock, grass, or an island type id.", () -> s.islandMainSpawnType, v -> s.islandMainSpawnType = v));
        list.add(str("islandSettings", "islandSpawnType", "Type used by /island create with no argument. random picks one.", () -> s.islandSpawnType, v -> s.islandSpawnType = v));
        list.add(num("islandSettings", "islandDistance", "Blocks between island centers.", () -> s.islandDistance, v -> s.islandDistance = v));
        list.add(num("islandSettings", "protectionBuildRange", "Build and break radius around an island center. 0 disables range checks.", () -> s.protectionBuildRange, v -> s.protectionBuildRange = v));
        list.add(bool("islandSettings", "spawnProtection", "", () -> s.spawnProtection, v -> s.spawnProtection = v));
        list.add(bool("islandSettings", "islandProtection", "", () -> s.islandProtection, v -> s.islandProtection = v));
        list.add(num("islandSettings", "islandSize", "Pad width in blocks. 3 is a 3x3.", () -> s.islandSize, v -> s.islandSize = v));
        list.add(bool("islandSettings", "spawnChest", "", () -> s.spawnChest, v -> s.spawnChest = v));
        list.add(bool("islandSettings", "oneChunk", "New islands are one chunk. Also the /island onechunk admin toggle.", () -> s.oneChunk, v -> s.oneChunk = v));
        list.add(arr("islandSettings", "startingItems", "namespace:path*count or namespace:path{snbt}*count. No item metadata in 1.20.1.", () -> s.startingItems, v -> s.startingItems = v));
        list.add(str("islandSettings", "islandBiome", "", () -> s.islandBiome, v -> s.islandBiome = v));
        list.add(num("islandSettings", "islandBiomeRange", "", () -> s.islandBiomeRange, v -> s.islandBiomeRange = v));
        list.add(num("islandSettings", "islandYLevel", "Y of the island surface.", () -> s.islandYLevel, v -> s.islandYLevel = v));
        list.add(str("islandSettings", "bottomBlockType", "BEDROCK or SECONDARYBLOCK for the layer under the pad.", () -> s.bottomBlockType, v -> s.bottomBlockType = v));
        list.add(bool("islandSettings", "autoCreate", "", () -> s.autoCreate, v -> s.autoCreate = v));
        list.add(bool("islandSettings", "autoCreateServersOnly", "", () -> s.autoCreateServersOnly, v -> s.autoCreateServersOnly = v));
        list.add(bool("islandSettings", "allowIslandCreation", "", () -> s.allowIslandCreation, v -> s.allowIslandCreation = v));
        list.add(bool("islandSettings", "resetInventory", "", () -> s.resetInventory, v -> s.resetInventory = v));
        list.add(arr("islandSettings", "customIslands", "Structure nbt names in config/voidislandcontrolstructures.", () -> s.customIslands, v -> s.customIslands = v));
        list.add(bool("islandSettings", "forceSpawn", "", () -> s.forceSpawn, v -> s.forceSpawn = v));
        list.add(num("islandSettings", "buffTimer", "Spawn buff length in ticks. 1200 is 60 seconds.", () -> s.buffTimer, v -> s.buffTimer = v));
        list.add(bool("islandSettings", "handleRespawn", "", () -> s.handleRespawn, v -> s.handleRespawn = v));
        list.add(bool("islandSettings", "islandLockdown", "", () -> s.islandLockdown, v -> s.islandLockdown = v));
        list.add(num("islandSettings", "islandLockdownRange", "", () -> s.islandLockdownRange, v -> s.islandLockdownRange = v));
        list.add(bool("islandSettings", "deleteIslandOnAbandonment", "Last member leaving. true clears the plot and frees the grid cell. false keeps both so the island can be recovered.", () -> s.deleteIslandOnAbandonment, v -> s.deleteIslandOnAbandonment = v));
        list.add(bool("islandSettings", "defaultVoidWorld", "Create World screen. true selects voidislandcontrol:void ahead of the vanilla default and Ex Deorum. false leaves the button alone.", () -> s.defaultVoidWorld, v -> s.defaultVoidWorld = v));

        list.add(bool("islandSettings.grassSettings", "enableGrassIsland", "", () -> s.grassSettings.enableGrassIsland, v -> s.grassSettings.enableGrassIsland = v));
        list.add(bool("islandSettings.grassSettings", "spawnTree", "", () -> s.grassSettings.spawnTree, v -> s.grassSettings.spawnTree = v));
        list.add(str("islandSettings.grassSettings", "grassBlockType", "GRASS, DIRT, or COARSEDIRT.", () -> s.grassSettings.grassBlockType, v -> s.grassSettings.grassBlockType = v));

        list.add(bool("islandSettings.sandSettings", "enableSandIsland", "", () -> s.sandSettings.enableSandIsland, v -> s.sandSettings.enableSandIsland = v));
        list.add(bool("islandSettings.sandSettings", "spawnCactus", "", () -> s.sandSettings.spawnCactus, v -> s.sandSettings.spawnCactus = v));
        list.add(str("islandSettings.sandSettings", "sandBlockType", "NORMAL or RED.", () -> s.sandSettings.sandBlockType, v -> s.sandSettings.sandBlockType = v));

        list.add(bool("islandSettings.snowSettings", "enableSnowIsland", "", () -> s.snowSettings.enableSnowIsland, v -> s.snowSettings.enableSnowIsland = v));
        list.add(bool("islandSettings.snowSettings", "spawnPumpkins", "", () -> s.snowSettings.spawnPumpkins, v -> s.snowSettings.spawnPumpkins = v));
        list.add(bool("islandSettings.snowSettings", "spawnIgloo", "", () -> s.snowSettings.spawnIgloo, v -> s.snowSettings.spawnIgloo = v));

        list.add(bool("islandSettings.woodSettings", "enableWoodIsland", "", () -> s.woodSettings.enableWoodIsland, v -> s.woodSettings.enableWoodIsland = v));
        list.add(bool("islandSettings.woodSettings", "spawnWater", "", () -> s.woodSettings.spawnWater, v -> s.woodSettings.spawnWater = v));
        list.add(bool("islandSettings.woodSettings", "spawnString", "", () -> s.woodSettings.spawnString, v -> s.woodSettings.spawnString = v));
        list.add(str("islandSettings.woodSettings", "woodBlockType", "OAK, SPRUCE, BIRCH, JUNGLE, ACACIA, or DARKOAK.", () -> s.woodSettings.woodBlockType, v -> s.woodSettings.woodBlockType = v));

        list.add(bool("islandSettings.gogSettings", "enableGoGIsland", "Garden of Glass pad. Uses Botania blocks when that mod is loaded, otherwise a pebble island.", () -> s.gogSettings.enableGoGIsland, v -> s.gogSettings.enableGoGIsland = v));

        list.add(str("commandSettings", "commandName", "Base command name. Default is /island.", () -> c.commandName, v -> c.commandName = v));
        list.add(bool("commandSettings", "oneChunkCommandAllowed", "", () -> c.oneChunkCommandAllowed, v -> c.oneChunkCommandAllowed = v));
        list.add(str("commandSettings", "commandBlockType", "NONE, IMPULSE, REPEATING, or CHAIN. Placed on a new island when not NONE.", () -> c.commandBlockType, v -> c.commandBlockType = v));
        list.add(bool("commandSettings", "commandBlockAuto", "", () -> c.commandBlockAuto, v -> c.commandBlockAuto = v));
        list.add(str("commandSettings", "commandBlockCommand", "", () -> c.commandBlockCommand, v -> c.commandBlockCommand = v));
        list.add(str("commandSettings", "commandBlockFacing", "", () -> c.commandBlockFacing, v -> c.commandBlockFacing = v));
        list.add(bool("commandSettings", "allowVisitCommand", "", () -> c.allowVisitCommand, v -> c.allowVisitCommand = v));
        list.add(bool("commandSettings", "allowHomeCommand", "", () -> c.allowHomeCommand, v -> c.allowHomeCommand = v));
        list.add(arr("commandSettings", "worldLoadCommands", "Commands run once when the world first loads. Use @p for the first player.", () -> c.worldLoadCommands, v -> c.worldLoadCommands = v));

        list.add(num("commandSettings.commandBlockPos", "x", "Offset from the island spawn for the command block.", () -> c.commandBlockPos.x, v -> c.commandBlockPos.x = v));
        list.add(num("commandSettings.commandBlockPos", "y", "", () -> c.commandBlockPos.y, v -> c.commandBlockPos.y = v));
        list.add(num("commandSettings.commandBlockPos", "z", "", () -> c.commandBlockPos.z, v -> c.commandBlockPos.z = v));

        list.add(bool("commandSettings.visitSettings", "allowBlockInteract", "Defaults for visitors. Per-player overrides apply only when allowPerPlayerOverrides is true.", () -> visit.allowBlockInteract, v -> visit.allowBlockInteract = v));
        list.add(bool("commandSettings.visitSettings", "allowItemUse", "", () -> visit.allowItemUse, v -> visit.allowItemUse = v));
        list.add(bool("commandSettings.visitSettings", "allowEntityInteract", "", () -> visit.allowEntityInteract, v -> visit.allowEntityInteract = v));
        list.add(bool("commandSettings.visitSettings", "allowAttack", "", () -> visit.allowAttack, v -> visit.allowAttack = v));
        list.add(bool("commandSettings.visitSettings", "allowPickup", "", () -> visit.allowPickup, v -> visit.allowPickup = v));
        list.add(bool("commandSettings.visitSettings", "allowBlockHarvest", "", () -> visit.allowBlockHarvest, v -> visit.allowBlockHarvest = v));
        list.add(bool("commandSettings.visitSettings", "allowBlockPlace", "", () -> visit.allowBlockPlace, v -> visit.allowBlockPlace = v));
        list.add(bool("commandSettings.visitSettings", "allowPerPlayerOverrides", "", () -> visit.allowPerPlayerOverrides, v -> visit.allowPerPlayerOverrides = v));
        return list;
    }

    private static Entry bool(String section, String key, String comment, Supplier<Boolean> get, Consumer<Boolean> set) {
        return new Entry(section, key, comment, () -> Boolean.toString(get.get()), raw -> set.accept(Boolean.parseBoolean(raw)));
    }

    private static Entry num(String section, String key, String comment, Supplier<Integer> get, Consumer<Integer> set) {
        return new Entry(section, key, comment, () -> Integer.toString(get.get()), raw -> {
            try {
                set.accept(Integer.parseInt(raw));
            } catch (NumberFormatException ignored) {
            }
        });
    }

    private static Entry str(String section, String key, String comment, Supplier<String> get, Consumer<String> set) {
        return new Entry(section, key, comment, () -> q(get.get()), raw -> set.accept(unquote(raw)));
    }

    private static Entry arr(String section, String key, String comment, Supplier<String[]> get, Consumer<String[]> set) {
        return new Entry(section, key, comment, () -> writeArr(get.get()), raw -> set.accept(readArr(raw)));
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

    private static String q(String value) {
        return "\"" + (value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"")) + "\"";
    }

    private static String unquote(String raw) {
        if (raw == null) return "";
        if (raw.length() >= 2 && raw.startsWith("\"") && raw.endsWith("\"")) {
            return raw.substring(1, raw.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return raw;
    }

    private static String writeArr(String[] values) {
        if (values == null || values.length == 0) return "[]";
        StringBuilder out = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) out.append(", ");
            out.append(q(values[i]));
        }
        return out.append(']').toString();
    }

    private static String[] readArr(String raw) {
        if (raw == null) return new String[0];
        String body = raw.trim();
        if (body.startsWith("[")) body = body.substring(1);
        if (body.endsWith("]")) body = body.substring(0, body.length() - 1);
        if (body.isBlank()) return new String[0];
        List<String> items = new ArrayList<>();
        for (String part : body.split(",")) {
            String item = unquote(part.trim());
            if (!item.isEmpty()) items.add(item);
        }
        return items.toArray(String[]::new);
    }

    private static final class Entry {
        final String section;
        final String key;
        final String comment;
        final Supplier<String> write;
        final Consumer<String> readRaw;

        Entry(String section, String key, String comment, Supplier<String> write, Consumer<String> readRaw) {
            this.section = section;
            this.key = key;
            this.comment = comment;
            this.write = write;
            this.readRaw = readRaw;
        }

        String write() {
            return write.get();
        }

        void read(Map<String, String> values) {
            String raw = values.get(section + "." + key);
            if (raw != null) readRaw.accept(raw);
        }
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
        public String[] customIslands = new String[0];
        public String islandBiome = "";
        public int islandBiomeRange = 0;
        public int islandYLevel = 88;
        public String bottomBlockType = "BEDROCK";
        public boolean autoCreate = false;
        public boolean autoCreateServersOnly = false;
        public boolean allowIslandCreation = true;
        public boolean resetInventory = true;
        public boolean forceSpawn = false;
        public boolean handleRespawn = true;
        public int buffTimer = 1200;
        public boolean islandLockdown = false;
        public int islandLockdownRange = 500;
        public boolean deleteIslandOnAbandonment = false;
        public boolean defaultVoidWorld = true;
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
