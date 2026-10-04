package com.bartz24.voidislandcontrol.world;

import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.api.IslandPos;
import com.bartz24.voidislandcontrol.config.VicConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * Garden of Glass pebble without a hard Botania dependency.
 * If Botania is installed, livingrock / pebble / water bowl are used by id.
 */
public final class GoGSupport {
    private GoGSupport() {
    }

    public static void spawn(ServerLevel level, BlockPos spawn) {
        BlockState rock = IslandPlacer.block("botania:livingrock", Blocks.STONE.defaultBlockState());
        BlockPos center = spawn.below(3);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                level.setBlockAndUpdate(center.offset(x, 0, z), rock);
            }
        }
        level.setBlockAndUpdate(center.above(), Blocks.WATER.defaultBlockState());
        level.setBlockAndUpdate(center.north().above(), Blocks.LAVA.defaultBlockState());
        BlockState sapling = IslandPlacer.block("botania:livingwood", Blocks.OAK_SAPLING.defaultBlockState());
        if (sapling.getBlock() == Blocks.OAK_SAPLING) {
            level.setBlockAndUpdate(center.south().above(), Blocks.OAK_SAPLING.defaultBlockState());
        } else {
            level.setBlockAndUpdate(center.south().above(), Blocks.OAK_SAPLING.defaultBlockState());
        }
    }

    public static InteractionResult onSneakEmpty(ServerPlayer player, BlockPos pos) {
        if (!player.isShiftKeyDown() || !player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
        IslandPos island = IslandManager.getPlayerIsland(player.getUUID());
        if (island == null || !"gog".equals(island.getType())) return InteractionResult.PASS;
        if (!nearIsland(player, island)) return InteractionResult.PASS;
        BlockState state = player.serverLevel().getBlockState(pos);
        Block block = state.getBlock();
        if (block != Blocks.GRASS_BLOCK && block != Blocks.DIRT && block != Blocks.DIRT_PATH && block != Blocks.FARMLAND
                && !is(block, "botania:infused_grass") && !is(block, "botania:dry_grass")) {
            return InteractionResult.PASS;
        }
        Item pebble = item("botania:pebble");
        if (pebble == null) return InteractionResult.PASS;
        if (!player.level().isClientSide && player.level().random.nextFloat() < 0.8F) {
            player.spawnAtLocation(new ItemStack(pebble));
            player.level().playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.4F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult onBowlWater(ServerPlayer player, BlockPos pos) {
        if (!player.getMainHandItem().is(Items.BOWL)) return InteractionResult.PASS;
        IslandPos island = IslandManager.getPlayerIsland(player.getUUID());
        if (island == null || !"gog".equals(island.getType())) return InteractionResult.PASS;
        if (!nearIsland(player, island)) return InteractionResult.PASS;
        if (!player.serverLevel().getFluidState(pos).is(Fluids.WATER)) return InteractionResult.PASS;
        Item bowl = item("botania:water_bowl");
        if (bowl == null) return InteractionResult.PASS;
        player.getMainHandItem().shrink(1);
        player.getInventory().placeItemBackInInventory(new ItemStack(bowl));
        return InteractionResult.SUCCESS;
    }

    private static boolean nearIsland(ServerPlayer player, IslandPos island) {
        int dist = VicConfig.islandSettings.islandDistance;
        double dx = player.getX() - island.getX() * dist;
        double dz = player.getZ() - island.getY() * dist;
        return Math.abs(dx) < VicConfig.islandSettings.protectionBuildRange
                && Math.abs(dz) < VicConfig.islandSettings.protectionBuildRange;
    }

    private static boolean is(Block block, String id) {
        return id.equals(String.valueOf(BuiltInRegistries.BLOCK.getKey(block)));
    }

    private static Item item(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null || !BuiltInRegistries.ITEM.containsKey(rl)) return null;
        Item item = BuiltInRegistries.ITEM.get(rl);
        return item == Items.AIR ? null : item;
    }
}
