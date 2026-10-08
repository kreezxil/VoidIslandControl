package com.bartz24.voidislandcontrol.logic;

import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.api.IslandPos;
import com.bartz24.voidislandcontrol.api.VisitPerms;
import com.bartz24.voidislandcontrol.config.VicConfig;
import com.bartz24.voidislandcontrol.world.GoGSupport;
import com.bartz24.voidislandcontrol.world.VoidChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;

public final class Protection {
    private static final Component CREATE_FIRST = Component.literal("create an island first");

    private Protection() {
    }

    public static boolean isVic(ServerLevel level) {
        if (level == null) return false;
        ChunkGenerator gen = level.getChunkSource().getGenerator();
        if (gen instanceof VoidChunkGenerator) return true;
        String name = gen.getClass().getName();
        return name.endsWith("VoidChunkGenerator") || name.contains("voidislandcontrol");
    }

    public static boolean denyBreak(ServerPlayer player, BlockPos pos) {
        return deny(player, pos, true, false, false);
    }

    public static boolean denyPlace(ServerPlayer player, BlockPos pos) {
        return deny(player, pos, false, true, false);
    }

    public static boolean denyUse(ServerPlayer player, BlockPos pos, ItemStack stack) {
        InteractionResult gog = GoGSupport.onSneakEmpty(player, pos);
        if (gog.consumesAction()) return true;
        gog = GoGSupport.onBowlWater(player, pos);
        if (gog.consumesAction()) return true;
        return deny(player, pos, false, false, true);
    }

    public static boolean denyAttack(ServerPlayer player, Entity target) {
        if (!(player.level() instanceof ServerLevel level) || !isVic(level)) return false;
        if (needsIsland(player) && onSpawnIsland(player)) {
            tellCreate(player);
            return true;
        }
        if (!isRestrictedVisitor(player)) return false;
        VisitPerms perms = effective(player);
        return perms != null && !perms.allowAttack;
    }

    public static boolean denyPickup(ServerPlayer player, ItemEntity item) {
        if (!(player.level() instanceof ServerLevel level) || !isVic(level)) return false;
        if (VicConfig.islandSettings.spawnProtection && needsIsland(player) && onSpawnIsland(player)) {
            tellCreate(player);
            return true;
        }
        if (!isRestrictedVisitor(player)) return false;
        VisitPerms perms = effective(player);
        return perms != null && !perms.allowPickup;
    }

    private static boolean deny(ServerPlayer player, BlockPos pos, boolean harvest, boolean place, boolean use) {
        if (!(player.level() instanceof ServerLevel level)) return false;
        if (player.isCreative() || IslandManager.isOperator(player)) return false;
        if (level.dimension() != VicConfig.baseLevel()) return false;
        if (VicConfig.islandSettings.spawnProtection && needsIsland(player) && onSpawnIsland(player, pos)) {
            tellCreate(player);
            return true;
        }
        if (!isVic(level)) return false;

        if (isRestrictedVisitor(player)) {
            VisitPerms perms = effective(player);
            if (perms == null) return true;
            if (harvest && !perms.allowBlockHarvest) return true;
            if (place && !perms.allowBlockPlace) return true;
            if (use && !perms.allowBlockInteract && !perms.allowItemUse) return true;
            return false;
        }

        if (!VicConfig.islandSettings.islandProtection) return false;
        IslandPos own = IslandManager.getPlayerIsland(player.getUUID());
        if (own == null) return false;
        int dist = VicConfig.islandSettings.islandDistance;
        int range = Math.min(VicConfig.islandSettings.protectionBuildRange, Math.max(1, dist / 2));
        int cx = own.getX() * dist;
        int cz = own.getY() * dist;
        return Math.abs(pos.getX() - cx) > range || Math.abs(pos.getZ() - cz) > range;
    }

    private static boolean needsIsland(ServerPlayer player) {
        return IslandManager.getPlayerIsland(player.getUUID()) == null;
    }

    private static boolean onSpawnIsland(ServerPlayer player) {
        return onSpawnIsland(player, player.blockPosition());
    }

    private static boolean onSpawnIsland(ServerPlayer player, BlockPos pos) {
        int range = Math.max(8, VicConfig.islandSettings.islandSize + 8);
        return Math.abs(pos.getX()) <= range && Math.abs(pos.getZ()) <= range
                && player.level().dimension() == VicConfig.baseLevel();
    }

    private static void tellCreate(ServerPlayer player) {
        String command = VicConfig.commandSettings.commandName;
        if (command == null || command.isBlank()) command = "island";
        Component line = Component.literal("create an island first. try /" + command + " create [<type>]");
        player.displayClientMessage(line, true);
        if (player.tickCount % 20 == 0) player.sendSystemMessage(line);
    }

    private static boolean isRestrictedVisitor(ServerPlayer player) {
        IslandManager.VisitState visit = IslandManager.getVisitLoc(player.getUUID());
        return visit != null && !visit.spectate && !IslandManager.isOperator(player) && !player.isCreative();
    }

    private static VisitPerms effective(ServerPlayer player) {
        IslandManager.VisitState visit = IslandManager.getVisitLoc(player.getUUID());
        if (visit == null) return VisitPerms.fromConfig();
        IslandPos island = IslandManager.getIslandAtPos(visit.x, visit.y);
        return island == null ? VisitPerms.fromConfig() : island.effectivePerms(player.getUUID());
    }

    public static boolean managed(Level level) {
        return level instanceof ServerLevel server && isVic(server);
    }
}
