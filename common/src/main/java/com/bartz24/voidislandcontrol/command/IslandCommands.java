package com.bartz24.voidislandcontrol.command;

import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.api.IslandPos;
import com.bartz24.voidislandcontrol.api.VicEvents;
import com.bartz24.voidislandcontrol.api.VisitPerms;
import com.bartz24.voidislandcontrol.config.VicConfig;
import com.bartz24.voidislandcontrol.data.VicSavedData;
import com.bartz24.voidislandcontrol.logic.Protection;
import com.bartz24.voidislandcontrol.logic.SpawnHandler;
import com.bartz24.voidislandcontrol.world.IslandPlacer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

import java.util.UUID;

public final class IslandCommands {
    private IslandCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        String name = VicConfig.commandSettings.commandName == null || VicConfig.commandSettings.commandName.isBlank()
                ? "island" : VicConfig.commandSettings.commandName;
        dispatcher.register(Commands.literal(name)
                .executes(ctx -> { help(ctx.getSource().getPlayerOrException()); return 1; })
                .then(Commands.literal("create")
                        .executes(ctx -> create(ctx.getSource().getPlayerOrException(), new String[]{"create"}))
                        .then(Commands.argument("type", StringArgumentType.word()).suggests(TYPES)
                                .executes(ctx -> create(ctx.getSource().getPlayerOrException(),
                                        new String[]{"create", StringArgumentType.getString(ctx, "type")}))))
                .then(Commands.literal("invite").then(Commands.argument("player", StringArgumentType.word()).suggests(ONLINE)
                        .executes(ctx -> invite(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "player")))))
                .then(Commands.literal("join").executes(ctx -> join(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("leave").executes(ctx -> leave(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("kick").then(Commands.argument("player", StringArgumentType.word()).suggests(ONLINE)
                        .executes(ctx -> kick(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "player")))))
                .then(Commands.literal("home").executes(ctx -> home(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("spawn").executes(ctx -> spawn(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("reset")
                        .executes(ctx -> reset(ctx.getSource().getPlayerOrException(), new String[]{"reset"}))
                        .then(Commands.argument("type", StringArgumentType.word()).suggests(TYPES)
                                .executes(ctx -> reset(ctx.getSource().getPlayerOrException(),
                                        new String[]{"reset", StringArgumentType.getString(ctx, "type")}))))
                .then(Commands.literal("onechunk").executes(ctx -> oneChunk(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("visit").then(Commands.argument("player", StringArgumentType.word()).suggests(KNOWN)
                        .executes(ctx -> visit(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "player"), false))))
                .then(Commands.literal("spectate").then(Commands.argument("player", StringArgumentType.word()).suggests(KNOWN)
                        .executes(ctx -> visit(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "player"), true))))
                .then(Commands.literal("list").executes(ctx -> list(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("permission")
                        .then(Commands.argument("player", StringArgumentType.word()).suggests(KNOWN)
                                .then(Commands.argument("flag", StringArgumentType.word()).suggests(FLAGS)
                                        .then(Commands.argument("value", StringArgumentType.word()).suggests(BOOLS)
                                                .executes(ctx -> permission(ctx.getSource().getPlayerOrException(),
                                                        StringArgumentType.getString(ctx, "player"),
                                                        StringArgumentType.getString(ctx, "flag"),
                                                        StringArgumentType.getString(ctx, "value"))))))));
    }

    private static final SuggestionProvider<CommandSourceStack> TYPES = (ctx, builder) ->
            SharedSuggestionProvider.suggest(IslandManager.getIslandGenTypes(), builder);
    private static final SuggestionProvider<CommandSourceStack> ONLINE = (ctx, builder) ->
            SharedSuggestionProvider.suggest(ctx.getSource().getServer().getPlayerNames(), builder);
    private static final SuggestionProvider<CommandSourceStack> KNOWN = (ctx, builder) ->
            SharedSuggestionProvider.suggest(IslandManager.getKnownIslandPlayerNames(ctx.getSource().getServer()), builder);
    private static final SuggestionProvider<CommandSourceStack> FLAGS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(VisitPerms.FLAGS, builder);
    private static final SuggestionProvider<CommandSourceStack> BOOLS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(new String[]{"true", "false"}, builder);

    private static boolean vic(ServerPlayer player) {
        if (!Protection.isVic(player.serverLevel()) && !(player.server.getLevel(VicConfig.baseLevel()) != null
                && Protection.isVic(player.server.getLevel(VicConfig.baseLevel())))) {
            player.sendSystemMessage(Component.literal("You are not in a void world type."));
            return false;
        }
        return true;
    }

    public static int create(ServerPlayer player, String[] args) {
        if (!vic(player)) return 0;
        SpawnHandler.ensureIslands();
        if (!VicConfig.islandSettings.allowIslandCreation && (args.length < 2 || !"bypass".equals(args[1]))) {
            player.sendSystemMessage(Component.literal("Not allowed to create islands!"));
            return 0;
        }
        if (IslandManager.playerHasIsland(player.getUUID())) {
            player.sendSystemMessage(Component.literal("You already have an island."));
            return 0;
        }
        if (IslandManager.islandGenerations.isEmpty()) {
            player.sendSystemMessage(Component.literal("No island types are enabled."));
            return 0;
        }
        endVisit(player);
        int type = -1;
        if (args.length > 1 && !"bypass".equals(args[1])) type = IslandManager.getIndexOfIslandType(args[1]);
        if (type < 0) {
            String spawnType = VicConfig.islandSettings.islandSpawnType;
            type = "random".equalsIgnoreCase(spawnType)
                    ? player.level().random.nextInt(IslandManager.islandGenerations.size())
                    : IslandManager.getIndexOfIslandType(spawnType);
        }
        if (type < 0) type = 0;
        IslandPos grid = IslandManager.getNextIsland();
        var gen = IslandManager.islandGenerations.get(type);
        IslandPos island = new IslandPos(gen.identifier, grid.getX(), grid.getY(), player.getUUID());
        IslandManager.currentIslands.add(island);
        ServerLevel level = player.server.getLevel(VicConfig.baseLevel());
        BlockPos spawn = IslandManager.worldPos(island);
        gen.generate(level, spawn);
        IslandPlacer.placeCommandBlock(level, spawn);
        IslandManager.tpPlayerToPosSpawn(player, spawn, island);
        IslandManager.setStartingInv(player);
        VicSavedData.mark(level);
        IslandPos created = IslandManager.getPlayerIsland(player.getUUID());
        VicEvents.CREATE.listeners().forEach(l -> l.accept(player, created));
        player.sendSystemMessage(Component.literal("Island created (" + gen.identifier + ")."));
        return 1;
    }

    private static int invite(ServerPlayer player, String name) {
        if (!vic(player)) return 0;
        IslandPos island = IslandManager.getPlayerIsland(player.getUUID());
        if (island == null || !island.isOwner(player.getUUID())) {
            player.sendSystemMessage(Component.literal("Only the island owner can invite."));
            return 0;
        }
        ServerPlayer other = player.server.getPlayerList().getPlayerByName(name);
        if (other == null) {
            player.sendSystemMessage(Component.literal("That player is not online."));
            return 0;
        }
        if (IslandManager.playerHasIsland(other.getUUID())) {
            player.sendSystemMessage(Component.literal("That player already has an island."));
            return 0;
        }
        IslandManager.setJoinLoc(other.getUUID(), island.getX(), island.getY());
        other.sendSystemMessage(Component.literal(player.getGameProfile().getName() + " invited you. /"
                + VicConfig.commandSettings.commandName + " join"));
        player.sendSystemMessage(Component.literal("Invited " + name + "."));
        VicEvents.INVITE.listeners().forEach(l -> l.accept(player, island));
        return 1;
    }

    private static int join(ServerPlayer player) {
        if (!vic(player)) return 0;
        IslandManager.JoinState join = IslandManager.getJoinLoc(player.getUUID());
        if (join == null) {
            player.sendSystemMessage(Component.literal("No pending invite."));
            return 0;
        }
        if (IslandManager.playerHasIsland(player.getUUID())) {
            player.sendSystemMessage(Component.literal("Leave your island first."));
            return 0;
        }
        IslandPos island = IslandManager.getIslandAtPos(join.x, join.y);
        if (island == null) {
            player.sendSystemMessage(Component.literal("That island is gone."));
            return 0;
        }
        endVisit(player);
        island.addNewPlayer(player.getUUID());
        IslandManager.removeJoinLoc(player.getUUID());
        IslandManager.tpPlayerToPosSpawn(player, IslandManager.worldPos(island), island);
        VicSavedData.mark(player.server.getLevel(VicConfig.baseLevel()));
        player.sendSystemMessage(Component.literal("Joined the island."));
        return 1;
    }

    private static int leave(ServerPlayer player) {
        if (!vic(player)) return 0;
        IslandPos island = IslandManager.getPlayerIsland(player.getUUID());
        if (island == null) {
            player.sendSystemMessage(Component.literal("You don't have an island."));
            return 0;
        }
        if (island.getPlayerUUIDs().size() <= 1 && !IslandManager.hasLeaveConfirm(player.getUUID())) {
            IslandManager.setLeaveConfirm(player.getUUID());
            player.sendSystemMessage(Component.literal("You are the last member. Run leave again to abandon the island."));
            return 1;
        }
        endVisit(player);
        IslandManager.removePlayer(player.getUUID());
        IslandManager.removeLeaveConfirm(player.getUUID());
        if (VicConfig.islandSettings.resetInventory) player.getInventory().clearContent();
        BlockPos spawn = new BlockPos(0, VicConfig.islandSettings.islandYLevel, 0);
        IslandManager.tpPlayerToPos(player, spawn, IslandManager.currentIslands.isEmpty() ? null : IslandManager.currentIslands.get(0));
        player.setGameMode(GameType.SURVIVAL);
        VicSavedData.mark(player.server.getLevel(VicConfig.baseLevel()));
        VicEvents.LEAVE.listeners().forEach(l -> l.accept(player, island));
        player.sendSystemMessage(Component.literal("Left the island."));
        return 1;
    }

    private static int kick(ServerPlayer player, String name) {
        if (!vic(player)) return 0;
        IslandPos island = IslandManager.getPlayerIsland(player.getUUID());
        if (island == null || !island.isOwner(player.getUUID())) {
            player.sendSystemMessage(Component.literal("Only the island owner can kick."));
            return 0;
        }
        ServerPlayer other = player.server.getPlayerList().getPlayerByName(name);
        if (other == null) {
            player.sendSystemMessage(Component.literal("That player is not online."));
            return 0;
        }
        if (!island.getPlayerUUIDs().contains(other.getUUID().toString())) {
            player.sendSystemMessage(Component.literal("Player isn't on your island."));
            return 0;
        }
        for (int i = 0; i < other.getInventory().getContainerSize(); i++) {
            ItemStack stack = other.getInventory().getItem(i);
            if (!stack.isEmpty()) other.spawnAtLocation(stack.copy());
        }
        other.getInventory().clearContent();
        IslandManager.removePlayer(other.getUUID());
        IslandManager.tpPlayerToPos(other, new BlockPos(0, VicConfig.islandSettings.islandYLevel, 0), null);
        other.sendSystemMessage(Component.literal("You were kicked from the island."));
        VicSavedData.mark(player.server.getLevel(VicConfig.baseLevel()));
        return 1;
    }

    private static int home(ServerPlayer player) {
        if (!vic(player)) return 0;
        if (!VicConfig.commandSettings.allowHomeCommand) {
            player.sendSystemMessage(Component.literal("This command was disabled"));
            return 0;
        }
        IslandPos island = IslandManager.getPlayerIsland(player.getUUID());
        if (island == null) {
            player.sendSystemMessage(Component.literal("You don't have an island."));
            return 0;
        }
        BlockPos center = IslandManager.worldPos(island);
        double dx = player.getX() - center.getX();
        double dz = player.getZ() - center.getZ();
        if (Math.abs(dx) < VicConfig.islandSettings.protectionBuildRange && Math.abs(dz) < VicConfig.islandSettings.protectionBuildRange
                && player.level().dimension() == VicConfig.baseLevel()) {
            player.sendSystemMessage(Component.literal("You must be at least "
                    + VicConfig.islandSettings.protectionBuildRange + " blocks away."));
            return 0;
        }
        endVisit(player);
        IslandManager.tpPlayerToPos(player, center, island);
        player.setGameMode(GameType.SURVIVAL);
        VicEvents.HOME.listeners().forEach(l -> l.accept(player, island));
        return 1;
    }

    private static int spawn(ServerPlayer player) {
        if (!vic(player)) return 0;
        endVisit(player);
        IslandManager.tpPlayerToPos(player, new BlockPos(0, VicConfig.islandSettings.islandYLevel, 0),
                IslandManager.currentIslands.isEmpty() ? null : IslandManager.currentIslands.get(0));
        player.setGameMode(GameType.SURVIVAL);
        VicEvents.SPAWN.listeners().forEach(l -> l.accept(player));
        return 1;
    }

    private static int reset(ServerPlayer player, String[] args) {
        if (!vic(player)) return 0;
        if (!VicConfig.islandSettings.allowIslandCreation) {
            player.sendSystemMessage(Component.literal("Not allowed to create islands!"));
            return 0;
        }
        ServerLevel level = player.server.getLevel(VicConfig.baseLevel());
        if (IslandManager.worldOneChunk) {
            for (int x = -8; x < 9; x++) {
                for (int z = -8; z < 9; z++) {
                    for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                        level.setBlockAndUpdate(new BlockPos(x, y, z), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                    }
                }
            }
            SpawnHandler.createSpawnIsland(level, new BlockPos(0, VicConfig.islandSettings.islandYLevel, 0));
            for (ServerPlayer p : player.server.getPlayerList().getPlayers()) {
                p.getInventory().clearContent();
                IslandManager.tpPlayerToPos(p, new BlockPos(0, VicConfig.islandSettings.islandYLevel, 0), IslandManager.currentIslands.get(0));
            }
            player.sendSystemMessage(Component.literal("Chunk Reset!"));
            return 1;
        }
        leave(player);
        create(player, args.length > 1 ? new String[]{"create", args[1]} : new String[]{"create"});
        VicEvents.RESET.listeners().forEach(l -> l.accept(player, IslandManager.getPlayerIsland(player.getUUID())));
        return 1;
    }

    private static int oneChunk(ServerPlayer player) {
        if (!vic(player)) return 0;
        if (!VicConfig.commandSettings.oneChunkCommandAllowed) {
            player.sendSystemMessage(Component.literal("This command is not allowed!"));
            return 0;
        }
        if (IslandManager.worldOneChunk) {
            player.sendSystemMessage(Component.literal("Already in one chunk mode!"));
            return 0;
        }
        IslandManager.currentIslands.clear();
        IslandManager.currentIslands.add(new IslandPos(0, 0));
        ServerLevel level = player.server.getLevel(VicConfig.baseLevel());
        SpawnHandler.enableOneChunk(level);
        reset(player, new String[]{"reset"});
        VicSavedData.mark(level);
        return 1;
    }

    private static int visit(ServerPlayer player, String name, boolean spectate) {
        if (!vic(player)) return 0;
        if (!VicConfig.commandSettings.allowVisitCommand) {
            player.sendSystemMessage(Component.literal("This command was disabled"));
            return 0;
        }
        if (IslandManager.worldOneChunk) {
            player.sendSystemMessage(Component.literal("Can't use this command in this mode."));
            return 0;
        }
        if (IslandManager.initialIslandDistance != VicConfig.islandSettings.islandDistance) {
            player.sendSystemMessage(Component.literal("This isn't going to work. The island distance has changed!"));
            return 0;
        }
        MinecraftServer server = player.server;
        UUID target = IslandManager.findPlayerUUID(server, name);
        if (target == null) {
            player.sendSystemMessage(Component.literal("Player doesn't exist or has never logged in."));
            return 0;
        }
        if (target.equals(player.getUUID())) {
            player.sendSystemMessage(Component.literal("Can't visit your own island."));
            return 0;
        }
        IslandPos island = IslandManager.getPlayerIsland(target);
        if (island == null) {
            player.sendSystemMessage(Component.literal("That player doesn't have an island."));
            return 0;
        }
        BlockPos pos = IslandManager.worldPos(island);
        IslandManager.setVisitLoc(player.getUUID(), island.getX(), island.getY(), spectate);
        player.setGameMode(spectate ? GameType.SPECTATOR : IslandManager.visitGameType(player));
        ServerLevel level = server.getLevel(VicConfig.baseLevel());
        player.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), player.getXRot());
        VicEvents.VISIT.listeners().forEach(l -> l.accept(player, island));
        return 1;
    }

    private static int list(ServerPlayer player) {
        var names = IslandManager.getKnownIslandPlayerNames(player.server);
        names.removeIf(n -> n.equalsIgnoreCase(player.getGameProfile().getName()));
        if (names.isEmpty()) {
            player.sendSystemMessage(Component.literal("No visitable islands found."));
            return 1;
        }
        player.sendSystemMessage(Component.literal("Visitable islands:"));
        for (String name : names) player.sendSystemMessage(Component.literal("  " + name));
        return 1;
    }

    private static int permission(ServerPlayer player, String name, String flag, String valueRaw) {
        if (!VicConfig.commandSettings.visitSettings.allowPerPlayerOverrides) {
            player.sendSystemMessage(Component.literal("Per-player visit permissions are disabled in config (allowPerPlayerOverrides)."));
            return 0;
        }
        IslandPos island = IslandManager.getPlayerIsland(player.getUUID());
        if (island == null) {
            player.sendSystemMessage(Component.literal("You don't have an island."));
            return 0;
        }
        if (!island.isOwner(player.getUUID())) {
            player.sendSystemMessage(Component.literal("Only the island owner can set visit permissions."));
            return 0;
        }
        UUID target = IslandManager.findPlayerUUID(player.server, name);
        if (target == null) {
            player.sendSystemMessage(Component.literal("Player doesn't exist or has never logged in."));
            return 0;
        }
        if (target.equals(player.getUUID())) {
            player.sendSystemMessage(Component.literal("You already own this island."));
            return 0;
        }
        boolean value = valueRaw.equalsIgnoreCase("true");
        if (!value && !valueRaw.equalsIgnoreCase("false")) {
            player.sendSystemMessage(Component.literal("Value must be true or false."));
            return 0;
        }
        try {
            island.setVisitPerm(target, flag, value);
        } catch (IllegalArgumentException e) {
            player.sendSystemMessage(Component.literal("Unknown flag. Use: " + String.join(", ", VisitPerms.FLAGS)));
            return 0;
        }
        VicSavedData.mark(player.server.getLevel(VicConfig.baseLevel()));
        player.sendSystemMessage(Component.literal("Set " + flag + " = " + value + " for " + name + " on your island."));
        return 1;
    }

    private static void endVisit(ServerPlayer player) {
        if (IslandManager.hasVisitLoc(player.getUUID())) {
            IslandManager.removeVisitLoc(player.getUUID());
            if (!player.isCreative()) player.setGameMode(GameType.SURVIVAL);
        }
    }

    private static void help(ServerPlayer player) {
        String n = VicConfig.commandSettings.commandName;
        player.sendSystemMessage(Component.literal("/" + n + " create [type] | invite | join | leave | kick | home | spawn | reset | onechunk | visit | spectate | list | permission"));
    }
}
