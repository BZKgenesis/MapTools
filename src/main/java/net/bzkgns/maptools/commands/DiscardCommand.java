package net.bzkgns.maptools.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;

public class DiscardCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("discard")
                .requires((source) -> source.hasPermission(2))
                .then(Commands.argument("targets", EntityArgument.entities())
                        .executes((context) -> discard(context.getSource(), EntityArgument.getEntities(context, "targets")))));
    }

    private static int discard(CommandSourceStack source, Collection<? extends Entity> targets) {
        for(Entity entity : targets) {
            if (!(entity instanceof Player)){
                entity.discard();
            }
        }

        if (targets.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.kill.success.single", targets.iterator().next().getDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.kill.success.multiple", targets.size()), true);
        }

        return targets.size();

    }

}
