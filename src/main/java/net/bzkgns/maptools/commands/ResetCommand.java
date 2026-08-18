package net.bzkgns.maptools.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import net.bzkgns.maptools.entities.ResetableEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

public class ResetCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mt")
                .then(Commands.literal("reset").then(Commands.argument("entities", EntityArgument.entities()).requires(
                        ctx -> ctx.hasPermission(2))
                        .executes(ctx -> {
                            EntityArgument.getEntities(ctx, "entities").stream()
                                    .filter(e -> (e instanceof ResetableEntity)).map(e -> (ResetableEntity) e)
                                    .forEach(e -> e.reset());
                            return Command.SINGLE_SUCCESS;
                        }))));

    }
}
