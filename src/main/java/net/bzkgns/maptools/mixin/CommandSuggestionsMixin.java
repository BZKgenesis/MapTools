package net.bzkgns.maptools.mixin;

import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CommandSuggestions.class)
public class CommandSuggestionsMixin {

    @ModifyArg(
            method = "showSuggestions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/CommandSuggestions$SuggestionsList;<init>(Lnet/minecraft/client/gui/components/CommandSuggestions;IIILjava/util/List;Z)V"
            ),
            index = 2
    )
    private int maptools$positionSuggestions(int y) {

        CommandSuggestionsAccessor self =
                (CommandSuggestionsAccessor) this;

        if (self.maptools$isAnchorToBottom()) {
            return y;
        }else{
            EditBox input = self.maptools$getInput();
            return input.getY() + input.getHeight();
        }
    }
}