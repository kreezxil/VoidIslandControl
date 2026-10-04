package com.bartz24.voidislandcontrol;

import com.bartz24.voidislandcontrol.command.AdminCommands;
import com.bartz24.voidislandcontrol.command.IslandCommands;
import com.bartz24.voidislandcontrol.command.StartingInvCommand;
import com.bartz24.voidislandcontrol.config.VicConfig;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

import java.nio.file.Path;

public final class CommonInit {
    private CommonInit() {
    }

    public static void init(Path configDir) {
        VicConfig.load(configDir);
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        IslandCommands.register(dispatcher);
        AdminCommands.register(dispatcher);
        StartingInvCommand.register(dispatcher);
    }
}
