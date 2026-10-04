package com.bartz24.voidislandcontrol.command;

import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.api.IslandPos;
import com.bartz24.voidislandcontrol.config.VicConfig;
import com.bartz24.voidislandcontrol.data.VicSavedData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class AdminCommands {
    private AdminCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("islandAdmin")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("kick").then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> kick(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "player")))))
                .then(Commands.literal("assign")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .then(Commands.argument("x", IntegerArgumentType.integer())
                                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                                .executes(ctx -> assign(ctx.getSource().getPlayerOrException(),
                                                        StringArgumentType.getString(ctx, "player"),
                                                        IntegerArgumentType.getInteger(ctx, "x"),
                                                        IntegerArgumentType.getInteger(ctx, "y")))))))
                .then(Commands.literal("assignOwner").then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> assignOwner(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "player")))))
                .then(Commands.literal("getIslandHere").executes(ctx -> here(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("list").executes(ctx -> list(ctx.getSource().getPlayerOrException()))));
    }

    private static int kick(ServerPlayer admin, String name) {
        ServerPlayer other = admin.server.getPlayerList().getPlayerByName(name);
        if (other == null) {
            admin.sendSystemMessage(Component.literal("Player is not online."));
            return 0;
        }
        IslandManager.removePlayer(other.getUUID());
        IslandManager.tpPlayerToPos(other, new BlockPos(0, VicConfig.islandSettings.islandYLevel, 0), null);
        VicSavedData.mark(admin.server.getLevel(VicConfig.baseLevel()));
        admin.sendSystemMessage(Component.literal("Kicked " + name + " from their island."));
        return 1;
    }

    private static int assign(ServerPlayer admin, String name, int x, int y) {
        UUID id = IslandManager.findPlayerUUID(admin.server, name);
        if (id == null) {
            admin.sendSystemMessage(Component.literal("Player doesn't exist or has never logged in."));
            return 0;
        }
        IslandPos island = IslandManager.getIslandAtPos(x, y);
        if (island == null) {
            admin.sendSystemMessage(Component.literal("No island at " + x + ", " + y));
            return 0;
        }
        IslandManager.removePlayer(id);
        island.addNewPlayer(id);
        VicSavedData.mark(admin.server.getLevel(VicConfig.baseLevel()));
        admin.sendSystemMessage(Component.literal("Assigned " + name + " to " + x + ", " + y));
        return 1;
    }

    private static int assignOwner(ServerPlayer admin, String name) {
        UUID id = IslandManager.findPlayerUUID(admin.server, name);
        if (id == null) {
            admin.sendSystemMessage(Component.literal("Player doesn't exist or has never logged in."));
            return 0;
        }
        IslandPos island = IslandManager.getPlayerIsland(id);
        if (island == null) {
            admin.sendSystemMessage(Component.literal("That player has no island."));
            return 0;
        }
        island.getPlayerUUIDs().remove(id.toString());
        island.getPlayerUUIDs().add(0, id.toString());
        VicSavedData.mark(admin.server.getLevel(VicConfig.baseLevel()));
        admin.sendSystemMessage(Component.literal(name + " is now the owner."));
        return 1;
    }

    private static int here(ServerPlayer admin) {
        int dist = Math.max(1, VicConfig.islandSettings.islandDistance);
        int x = Math.round((float) admin.getX() / dist);
        int y = Math.round((float) admin.getZ() / dist);
        IslandPos island = IslandManager.getIslandAtPos(x, y);
        if (island == null) {
            admin.sendSystemMessage(Component.literal("No island at grid " + x + ", " + y));
            return 0;
        }
        admin.sendSystemMessage(Component.literal("Island " + x + ", " + y + " type=" + island.getType()
                + " members=" + island.getPlayerUUIDs()));
        return 1;
    }

    private static int list(ServerPlayer admin) {
        if (IslandManager.currentIslands.isEmpty()) {
            admin.sendSystemMessage(Component.literal("No islands."));
            return 1;
        }
        for (IslandPos island : IslandManager.currentIslands) {
            admin.sendSystemMessage(Component.literal(island.getX() + ", " + island.getY() + " " + island.getType()
                    + " " + island.getPlayerUUIDs()));
        }
        return 1;
    }
}
