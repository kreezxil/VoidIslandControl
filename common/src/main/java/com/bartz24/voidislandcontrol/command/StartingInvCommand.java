package com.bartz24.voidislandcontrol.command;

import com.bartz24.voidislandcontrol.config.VicConfig;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.nio.file.Path;

public final class StartingInvCommand {
    private StartingInvCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("startingInv")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> capture(ctx.getSource().getPlayerOrException())));
    }

    private static int capture(ServerPlayer player) {
        String[] items = new String[player.getInventory().getContainerSize()];
        for (int i = 0; i < items.length; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) {
                items[i] = "";
                continue;
            }
            String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            String nbt = stack.getTag() == null ? "" : stack.getTag().toString();
            items[i] = id + nbt + "*" + stack.getCount();
        }
        VicConfig.islandSettings.startingItems = items;
        try {
            Path dir = VicConfig.configDir;
            VicConfig.save(dir);
        } catch (IOException e) {
            player.sendSystemMessage(Component.literal("Failed to save starting inventory: " + e.getMessage()));
            return 0;
        }
        player.sendSystemMessage(Component.literal("Starting inventory captured to config/voidislandcontrol.json"));
        return 1;
    }
}
