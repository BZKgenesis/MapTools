package net.bzkgns.maptools.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.bzkgns.maptools.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ShowWarningKillConfigCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mt")
                .then(Commands.literal("config")
                .requires((source) -> source.hasPermission(2))
                        .then(Commands.literal("showWarningKill")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes((context) -> {
                                    Config.SHOW_WARNING_MESSAGE_KILL.set(BoolArgumentType.getBool(context, "value"));
                                    return Command.SINGLE_SUCCESS;
                                })))));
    }
}
