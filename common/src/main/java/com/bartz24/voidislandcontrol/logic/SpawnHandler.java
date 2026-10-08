package com.bartz24.voidislandcontrol.logic;

import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.api.IslandPos;
import com.bartz24.voidislandcontrol.api.VicEvents;
import com.bartz24.voidislandcontrol.command.IslandCommands;
import com.bartz24.voidislandcontrol.config.VicConfig;
import com.bartz24.voidislandcontrol.data.VicSavedData;
import com.bartz24.voidislandcontrol.world.IslandPlacer;
import com.bartz24.voidislandcontrol.world.VoidChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;

public final class SpawnHandler {
    private static boolean islandsRegistered;

    private SpawnHandler() {
    }

    public static void onJoin(ServerPlayer player) {
        ensureIslands();
        ServerLevel level = player.serverLevel();
        if (!(level.getChunkSource().getGenerator() instanceof VoidChunkGenerator)) return;
        if (level.dimension() != VicConfig.baseLevel()) return;
        VicSavedData.get(level);
        if (IslandManager.hasPlayerSpawned(player.getUUID())) return;

        BlockPos spawn = new BlockPos(0, VicConfig.islandSettings.islandYLevel, 0);
        if (!IslandManager.hasPlayerSpawned(player.getUUID())) {
            createSpawnIsland(level, spawn);
            level.setDefaultSpawnPos(spawn, 0.0F);
        }

        boolean auto = VicConfig.islandSettings.autoCreate
                || (player.server.isDedicatedServer() && VicConfig.islandSettings.autoCreateServersOnly);
        if (auto && !IslandManager.worldOneChunk) {
            IslandCommands.create(player, new String[]{"create", "bypass"});
        } else {
            if (VicConfig.islandSettings.oneChunk) enableOneChunk(level);
           // IslandManager.tpPlayerToPosSpawn(player, spawn, IslandManager.currentIslands.get(0));
            player.clearFire();
        }
        IslandManager.spawnedPlayers.add(player.getUUID().toString());
        VicSavedData.mark(level);
        runWorldLoadCommands(player);
    }

    public static void onRespawn(ServerPlayer player) {
        if (!VicConfig.islandSettings.handleRespawn) return;
        if (!(player.serverLevel().getChunkSource().getGenerator() instanceof VoidChunkGenerator)) return;
        IslandPos island = IslandManager.getPlayerIsland(player.getUUID());
        BlockPos pos = island == null
                ? new BlockPos(0, VicConfig.islandSettings.islandYLevel, 0)
                : IslandManager.worldPos(island);
        IslandManager.tpPlayerToPos(player, pos, island == null ? IslandManager.currentIslands.get(0) : island);
    }

    public static void tick(ServerPlayer player) {
        IslandManager.tickTransient(player.getUUID());
        if (!(player.serverLevel().getChunkSource().getGenerator() instanceof VoidChunkGenerator)) return;
        if (player.level().dimension() != VicConfig.baseLevel() || player.isCreative()) return;

        IslandManager.VisitState visit = IslandManager.getVisitLoc(player.getUUID());
        if (visit != null) {
            var want = IslandManager.visitGameType(player);
            if (player.gameMode.getGameModeForPlayer() != want) player.setGameMode(want);
            if (VicConfig.islandSettings.islandLockdown && !IslandManager.isOperator(player)) {
                int dist = VicConfig.islandSettings.islandDistance;
                int cx = visit.x * dist;
                int cz = visit.y * dist;
                if (far(player, cx, cz, VicConfig.islandSettings.islandLockdownRange)) {
                    if (player.tickCount % 60 == 0) {
                        player.sendSystemMessage(Component.literal("You can't be visiting that far away!"));
                    }
                    player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                    IslandManager.removeVisitLoc(player.getUUID());
                    IslandManager.tpPlayerToPos(player, new BlockPos(cx, VicConfig.islandSettings.islandYLevel, cz),
                            IslandManager.getIslandAtPos(visit.x, visit.y));
                }
            }
        } else if (player.server.isDedicatedServer() && VicConfig.islandSettings.islandLockdown
                && !IslandManager.isOperator(player)) {
            IslandPos pos = IslandManager.getPlayerIsland(player.getUUID());
            int dist = VicConfig.islandSettings.islandDistance;
            int cx = pos == null ? 0 : pos.getX() * dist;
            int cz = pos == null ? 0 : pos.getY() * dist;
            if (far(player, cx, cz, VicConfig.islandSettings.islandLockdownRange)) {
                if (player.tickCount % 60 == 0) {
                    player.sendSystemMessage(Component.literal("You can't be away from your island or spawn that far away!"));
                }
                player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                IslandManager.tpPlayerToPos(player, new BlockPos(cx, VicConfig.islandSettings.islandYLevel, cz), pos);
            }
        }
    }

    public static void ensureSpawnIsland(ServerLevel level) {
        ensureIslands();
        BlockPos spawn = new BlockPos(0, VicConfig.islandSettings.islandYLevel, 0);
        createSpawnIsland(level, spawn);
        level.setDefaultSpawnPos(spawn, 0.0F);
    }

    public static void createSpawnIsland(ServerLevel level, BlockPos spawn) {
        ensureIslands();
        String main = VicConfig.islandSettings.islandMainSpawnType;
        if ("bedrock".equalsIgnoreCase(main)) {
            IslandPlacer.mainSpawn(level, spawn);
        } else {
            int type = "random".equalsIgnoreCase(main)
                    ? level.random.nextInt(Math.max(1, IslandManager.islandGenerations.size()))
                    : IslandManager.getIndexOfIslandType(main);
            if (type >= 0 && type < IslandManager.islandGenerations.size()) {
                IslandManager.islandGenerations.get(type).generate(level, spawn);
            } else {
                IslandPlacer.mainSpawn(level, spawn);
            }
        }
        IslandPlacer.placeCommandBlock(level, spawn);
    }

    public static void enableOneChunk(ServerLevel level) {
        WorldBorder border = level.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(16);
        border.setWarningBlocks(1);
        IslandManager.worldOneChunk = true;
        if (IslandManager.currentIslands.isEmpty()) IslandManager.currentIslands.add(new IslandPos(0, 0));
    }

    public static void ensureIslands() {
        if (!islandsRegistered) {
            IslandPlacer.registerDefaults();
            islandsRegistered = true;
            IslandManager.initialIslandDistance = VicConfig.islandSettings.islandDistance;
        }
    }

    public static void resetIslandRegistry() {
        islandsRegistered = false;
    }

    private static void runWorldLoadCommands(ServerPlayer player) {
        if (IslandManager.worldLoaded) return;
        IslandManager.worldLoaded = true;
        MinecraftServer server = player.server;
        if (VicConfig.commandSettings.worldLoadCommands == null) return;
        var source = server.createCommandSourceStack().withPermission(2).withSuppressedOutput();
        for (String cmd : VicConfig.commandSettings.worldLoadCommands) {
            if (cmd != null && !cmd.isBlank()) server.getCommands().performPrefixedCommand(source, cmd);
        }
        VicEvents.SPAWN.listeners().forEach(l -> l.accept(player));
    }

    private static boolean far(ServerPlayer player, int x, int z, int range) {
        Vec3 p = player.position();
        return Math.abs(p.x - x) > range || Math.abs(p.z - z) > range;
    }
}
