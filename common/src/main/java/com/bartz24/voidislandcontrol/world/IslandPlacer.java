package com.bartz24.voidislandcontrol.world;

import com.bartz24.voidislandcontrol.References;
import com.bartz24.voidislandcontrol.api.IslandGen;
import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.config.VicConfig;
import com.bartz24.voidislandcontrol.platform.StructureNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class IslandPlacer {
    private IslandPlacer() {
    }

    public static void registerDefaults() {
        IslandManager.islandGenerations.clear();
        VicConfig.IslandSettings cfg = VicConfig.islandSettings;
        if (cfg.grassSettings.enableGrassIsland) {
            IslandManager.registerIsland(new IslandGen("grass", new BlockPos(0, 7, 0)) {
                @Override
                public void generate(ServerLevel level, BlockPos spawn) {
                    BlockState top = switch (cfg.grassSettings.grassBlockType) {
                        case "DIRT" -> Blocks.DIRT.defaultBlockState();
                        case "COARSEDIRT" -> Blocks.COARSE_DIRT.defaultBlockState();
                        default -> Blocks.GRASS_BLOCK.defaultBlockState();
                    };
                    fillPad(level, spawn, top, Blocks.DIRT.defaultBlockState());
                    if (cfg.grassSettings.spawnTree) placeOak(level, spawn.below(2));
                    maybeChest(level, spawn);
                }
            });
        }
        if (cfg.sandSettings.enableSandIsland) {
            IslandManager.registerIsland(new IslandGen("sand", new BlockPos(0, 2, 0)) {
                @Override
                public void generate(ServerLevel level, BlockPos spawn) {
                    BlockState sand = "NORMAL".equals(cfg.sandSettings.sandBlockType)
                            ? Blocks.SAND.defaultBlockState() : Blocks.RED_SAND.defaultBlockState();
                    fillPad(level, spawn, sand, sand);
                    if (cfg.sandSettings.spawnCactus) {
                        BlockPos cactus = spawn.below(2).north();
                        level.setBlockAndUpdate(cactus, Blocks.CACTUS.defaultBlockState());
                        level.setBlockAndUpdate(cactus.above(), Blocks.CACTUS.defaultBlockState());
                    }
                    maybeChest(level, spawn);
                }
            });
        }
        if (cfg.snowSettings.enableSnowIsland) {
            IslandManager.registerIsland(new IslandGen("snow", new BlockPos(0, 2, 0)) {
                @Override
                public void generate(ServerLevel level, BlockPos spawn) {
                    fillPad(level, spawn, Blocks.SNOW_BLOCK.defaultBlockState(), Blocks.PACKED_ICE.defaultBlockState());
                    if (cfg.snowSettings.spawnPumpkins) {
                        level.setBlockAndUpdate(spawn.below(2).east(), Blocks.PUMPKIN.defaultBlockState());
                        level.setBlockAndUpdate(spawn.below(2).west(), Blocks.PUMPKIN.defaultBlockState());
                    }
                    if (cfg.snowSettings.spawnIgloo) {
                        int r = Math.max(2, cfg.islandSize);
                        for (int x = -r; x <= r; x++) {
                            for (int z = -r; z <= r; z++) {
                                if (Math.abs(x) == r || Math.abs(z) == r) {
                                    level.setBlockAndUpdate(spawn.offset(x, -2, z), Blocks.PACKED_ICE.defaultBlockState());
                                }
                            }
                        }
                    }
                    maybeChest(level, spawn);
                }
            });
        }
        if (cfg.woodSettings.enableWoodIsland) {
            IslandManager.registerIsland(new IslandGen("wood", new BlockPos(0, 2, 0)) {
                @Override
                public void generate(ServerLevel level, BlockPos spawn) {
                    BlockState wood = switch (cfg.woodSettings.woodBlockType) {
                        case "OAK" -> Blocks.OAK_PLANKS.defaultBlockState();
                        case "SPRUCE" -> Blocks.SPRUCE_PLANKS.defaultBlockState();
                        case "BIRCH" -> Blocks.BIRCH_PLANKS.defaultBlockState();
                        case "JUNGLE" -> Blocks.JUNGLE_PLANKS.defaultBlockState();
                        case "ACACIA" -> Blocks.ACACIA_PLANKS.defaultBlockState();
                        default -> Blocks.DARK_OAK_PLANKS.defaultBlockState();
                    };
                    fillPad(level, spawn, wood, wood);
                    if (cfg.woodSettings.spawnWater) {
                        level.setBlockAndUpdate(spawn.below(2), Blocks.WATER.defaultBlockState());
                    }
                    if (cfg.woodSettings.spawnString) {
                        level.setBlockAndUpdate(spawn.below(), Blocks.TRIPWIRE.defaultBlockState());
                    }
                    maybeChest(level, spawn);
                }
            });
        }
        if (cfg.gogSettings.enableGoGIsland) {
            IslandManager.registerIsland(new IslandGen("gog", new BlockPos(0, 2, 0)) {
                @Override
                public void generate(ServerLevel level, BlockPos spawn) {
                    GoGSupport.spawn(level, spawn);
                    maybeChest(level, spawn);
                }
            });
        }
        if (cfg.customIslands != null) {
            for (String name : cfg.customIslands) {
                if (name == null || name.isBlank()) continue;
                String id = name.endsWith(".nbt") ? name.substring(0, name.length() - 4) : name;
                IslandManager.registerIsland(new IslandGen(id, new BlockPos(0, 1, 0)) {
                    @Override
                    public void generate(ServerLevel level, BlockPos spawn) {
                        placeStructure(level, spawn, id);
                        maybeChest(level, spawn);
                    }
                });
            }
        }
    }

    public static void fillPad(ServerLevel level, BlockPos spawn, BlockState top, BlockState secondary) {
        int half = (int) Math.floor(VicConfig.islandSettings.islandSize / 2.0F);
        boolean bedrock = !"SECONDARYBLOCK".equals(VicConfig.islandSettings.bottomBlockType);
        for (int x = -half; x <= half; x++) {
            for (int z = -half; z <= half; z++) {
                BlockPos pos = spawn.offset(x, 0, z);
                level.setBlockAndUpdate(pos.below(3), top);
                level.setBlockAndUpdate(pos.below(4), bedrock ? Blocks.BEDROCK.defaultBlockState() : secondary);
            }
        }
    }

    public static void mainSpawn(ServerLevel level, BlockPos spawn) {
        int half = Math.max(1, (int) Math.floor(VicConfig.islandSettings.islandSize / 2.0F));
        for (int x = -half; x <= half; x++) {
            for (int z = -half; z <= half; z++) {
                level.setBlockAndUpdate(spawn.offset(x, -4, z), Blocks.BEDROCK.defaultBlockState());
                level.setBlockAndUpdate(spawn.offset(x, -3, z), Blocks.GRASS_BLOCK.defaultBlockState());
            }
        }
    }

    public static void placeCommandBlock(ServerLevel level, BlockPos spawn) {
        String type = VicConfig.commandSettings.commandBlockType;
        if (type == null || "NONE".equalsIgnoreCase(type)) return;
        BlockState state = switch (type.toUpperCase()) {
            case "REPEATING" -> Blocks.REPEATING_COMMAND_BLOCK.defaultBlockState();
            case "CHAIN" -> Blocks.CHAIN_COMMAND_BLOCK.defaultBlockState();
            default -> Blocks.COMMAND_BLOCK.defaultBlockState();
        };
        if (state.hasProperty(BlockStateProperties.CONDITIONAL)) {
            state = state.setValue(BlockStateProperties.CONDITIONAL, false);
        }
        VicConfig.CommandBlockPos off = VicConfig.commandSettings.commandBlockPos;
        BlockPos pos = spawn.offset(off.x, off.y - 4, off.z);
        level.setBlockAndUpdate(pos, state);
        if (level.getBlockEntity(pos) instanceof CommandBlockEntity be) {
            be.getCommandBlock().setCommand(VicConfig.commandSettings.commandBlockCommand == null
                    ? "" : VicConfig.commandSettings.commandBlockCommand);
            be.setAutomatic(VicConfig.commandSettings.commandBlockAuto);
        }
    }

    private static void maybeChest(ServerLevel level, BlockPos spawn) {
        if (!VicConfig.islandSettings.spawnChest) return;
        level.setBlockAndUpdate(spawn.below(2).south(), Blocks.CHEST.defaultBlockState());
    }

    private static void placeOak(ServerLevel level, BlockPos base) {
        for (int y = 0; y < 4; y++) level.setBlockAndUpdate(base.above(y), Blocks.OAK_LOG.defaultBlockState());
        BlockPos top = base.above(3);
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 && Math.abs(z) == 2) continue;
                level.setBlockAndUpdate(top.offset(x, 0, z), Blocks.OAK_LEAVES.defaultBlockState());
                if (Math.abs(x) <= 1 && Math.abs(z) <= 1) {
                    level.setBlockAndUpdate(top.offset(x, 1, z), Blocks.OAK_LEAVES.defaultBlockState());
                }
            }
        }
    }

    public static void placeStructure(ServerLevel level, BlockPos spawn, String name) {
        Path file = structureDir(level).resolve(name + ".nbt");
        if (!Files.exists(file)) return;
        try (InputStream in = Files.newInputStream(file)) {
            CompoundTag tag = StructureNbt.INSTANCE.read(in);
            StructureTemplate template = new StructureTemplate();
            template.load(level.holderLookup(net.minecraft.core.registries.Registries.BLOCK), tag);
            BlockPos origin = spawn.offset(-template.getSize().getX() / 2, -2, -template.getSize().getZ() / 2);
            template.placeInWorld(level, origin, origin, new StructurePlaceSettings(), RandomSource.create(), 2);
        } catch (IOException ignored) {
        }
    }

    public static Path structureDir(ServerLevel level) {
        Path dir = VicConfig.configDir.resolve(References.MODID + "structures");
        try {
            Files.createDirectories(dir);
        } catch (IOException ignored) {
        }
        return dir;
    }

    public static BlockState block(String id, BlockState fallback) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null || !BuiltInRegistries.BLOCK.containsKey(rl)) return fallback;
        return BuiltInRegistries.BLOCK.get(rl).defaultBlockState();
    }
}

