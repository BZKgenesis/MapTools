package net.bzkgns.maptools.client.screen.components;

import net.bzkgns.maptools.entities.AbstractCommandData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class CommandList<
        C extends AbstractCommandData<T>,
        T extends Enum<T>>
        extends AbstractSelectionList<CommandList<C,T>.CommandEntry> {

    private final Screen screen;

    private final BiFunction<String, T, C> commandFactory;
    private final T defaultTrigger;

    private final T[] triggerValues;

    public CommandList(
            Screen screen,
            Minecraft minecraft,
            int x,
            int y,
            int width,
            int height,
            Class<T> triggerClass,
            T defaultTrigger,
            BiFunction<String, T, C> commandFactory
    ) {
        super(
                minecraft,
                width,
                height,
                y,
                28
        );

        this.screen = screen;
        this.commandFactory = commandFactory;
        this.defaultTrigger = defaultTrigger;
        this.triggerValues = triggerClass.getEnumConstants();

        this.setX(x);
    }

    @Override
    public int getRowWidth() {
        return 350;
    }

    public void loadCommands(List<C> commands) {
        this.clearEntries();

        for (C command : commands) {
            addEntry(
                    new CommandEntry(
                            minecraft,
                            screen,
                            this,
                            getRowWidth(),
                            commandFactory.apply(
                                    command.getCommand(),
                                    command.getTrigger()
                            )
                    )
            );
        }
    }

    public void addCommand() {
        addEntry(
                new CommandEntry(
                        minecraft,
                        screen,
                        this,
                        getRowWidth(),
                        commandFactory.apply(
                                "",
                                defaultTrigger
                        )
                )
        );

        if (!children().isEmpty()) {
            setSelected(children().getLast());
        }
    }

    public void removeCommand(CommandEntry entry) {
        removeEntry(entry);
    }

    public List<C> getCommands() {
        List<C> commands = new ArrayList<>();

        for (CommandEntry entry : children()) {
            commands.add(entry.getCommandData());
        }

        return commands;
    }

    public void setFocusedEntry(CommandEntry entry) {
        for (CommandEntry current : children()) {
            if (current != entry) {
                current.clearFocus();
            }
        }

        setSelected(entry);
    }

    @Override
    protected boolean removeEntry(@NotNull CommandEntry entry) {
        List<CommandEntry> entries = new ArrayList<>(children());

        boolean result = entries.remove(entry);

        replaceEntries(entries);
        return result;
    }

    @Override
    protected void renderItem(
            @NotNull GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick,
            int index,
            int left,
            int top,
            int width,
            int height
    ) {
        CommandEntry entry = getEntry(index);

        entry.setPosition(left, top);

        entry.render(
                guiGraphics,
                index,
                top,
                left,
                width,
                height,
                mouseX,
                mouseY,
                isMouseOver(mouseX, mouseY),
                partialTick
        );
    }

    @Override
    protected void renderListBackground(
            GuiGraphics guiGraphics
    ) {
        guiGraphics.fill(
                getRowLeft(),
                getY(),
                getRowRight(),
                getBottom(),
                0x80000000
        );
    }

    @Override
    protected void renderDecorations(
            @NotNull GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {
        super.renderDecorations(
                guiGraphics,
                mouseX,
                mouseY
        );
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        boolean result = super.mouseClicked(mouseX, mouseY, button);

        if (!result) {
            clearInputFocus();
        }

        return result;
    }

    public void clearInputFocus() {
        for (CommandEntry entry : children()) {
            entry.clearFocus();
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}


    public class CommandEntry extends AbstractSelectionList.Entry<CommandEntry> {

        private final CommandList<C,T> parent;

        private final CycleButton<T> trigger;
        private final EditBox command;
        private final Button remove;

        private final CommandSuggestions suggestions;

        private GuiEventListener focusedChild;

        public CommandEntry(
                Minecraft minecraft,
                Screen screen,
                CommandList<C,T> parent,
                int width,
                C command
        ) {
            this.parent = parent;

            this.trigger = CycleButton
                    .builder((T t) ->
                            Component.literal(t.name())
                    )
                    .withValues(triggerValues)
                    .withInitialValue(command.getTrigger())
                    .displayOnlyValue()
                    .create(
                            0,
                            0,
                            90,
                            20,
                            Component.literal("Trigger"),
                            (button, value) ->
                                    command.setTrigger(value)
                    );

            this.command = new EditBox(
                    minecraft.font,
                    0,
                    0,
                    width - 120,
                    20,
                    Component.literal("Commande")
            );

            this.command.setMaxLength(32000);
            this.command.setValue(command.getCommand());

            this.command.setResponder(command::setCommand);

            this.remove = Button.builder(
                    Component.literal("X"),
                    button -> parent.removeCommand(this)
            ).bounds(
                    0,
                    0,
                    20,
                    20
            ).build();

            this.suggestions = new CommandSuggestions(
                    minecraft,
                    screen,
                    this.command,
                    minecraft.font,
                    true,
                    true,
                    0,
                    7,
                    false,
                    Integer.MIN_VALUE
            );

            this.suggestions.setAllowSuggestions(true);

            this.command.setResponder(value -> {
                command.setCommand(value);
                suggestions.updateCommandInfo();
            });
        }


        public C getCommandData() {
            return commandFactory.apply(
                    command.getValue(),
                    trigger.getValue()
            );
        }

        @SuppressWarnings("unused")
        public EditBox getCommandBox() {
            return command;
        }

        @SuppressWarnings("unused")
        public CommandSuggestions getSuggestions() {
            return suggestions;
        }

        public void setPosition(int x, int y) {
            int commandWidth = parent.getRowWidth() - 120;

            trigger.setPosition(x, y);
            command.setPosition(x + 95, y);
            remove.setPosition(x + commandWidth + 100, y);
        }

        @Override
        public void render(
                @NotNull GuiGraphics guiGraphics,
                int index,
                int top,
                int left,
                int width,
                int height,
                int mouseX,
                int mouseY,
                boolean hovering,
                float partialTick
        ) {
            int triggerWidth = 100;
            int removeWidth = 20;
            int spacing = 5;

            int commandWidth =
                    width
                            - triggerWidth
                            - removeWidth
                            - spacing * 2;

            trigger.setX(left);
            trigger.setY(top);

            command.setX(
                    left + triggerWidth + spacing
            );
            command.setY(top);

            command.setWidth(commandWidth);

            remove.setX(
                    left
                            + triggerWidth
                            + spacing
                            + commandWidth
                            + spacing
            );
            remove.setY(top);

            trigger.render(
                    guiGraphics,
                    mouseX,
                    mouseY,
                    partialTick
            );

            command.render(
                    guiGraphics,
                    mouseX,
                    mouseY,
                    partialTick
            );

            remove.render(
                    guiGraphics,
                    mouseX,
                    mouseY,
                    partialTick
            );
        }

        @Override
        public boolean mouseClicked(
                double mouseX,
                double mouseY,
                int button
        ) {
            if (trigger.mouseClicked(mouseX, mouseY, button)) {
                parent.setFocusedEntry(this);
                setFocusedChild(trigger);
                return true;
            }

            if (command.mouseClicked(mouseX, mouseY, button)) {
                parent.setFocusedEntry(this);
                setFocusedChild(command);

                suggestions.updateCommandInfo();

                return true;
            }

            if (remove.mouseClicked(mouseX, mouseY, button)) {
                parent.setFocusedEntry(this);
                setFocusedChild(remove);
                return true;
            }

            return false;
        }

        @Override
        public boolean mouseReleased(
                double mouseX,
                double mouseY,
                int button
        ) {
            return trigger.mouseReleased(mouseX, mouseY, button)
                    || command.mouseReleased(mouseX, mouseY, button)
                    || remove.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(
                double mouseX,
                double mouseY,
                int button,
                double dragX,
                double dragY
        ) {
            return command.mouseDragged(
                    mouseX,
                    mouseY,
                    button,
                    dragX,
                    dragY
            );
        }

        @Override
        public boolean keyPressed(
                int keyCode,
                int scanCode,
                int modifiers
        ) {
            if (command.isFocused()) {
                if (suggestions.keyPressed(
                        keyCode,
                        scanCode,
                        modifiers
                )) {
                    return true;
                }

                if (command.keyPressed(
                        keyCode,
                        scanCode,
                        modifiers
                )) {
                    return true;
                }
            }

            if (trigger.isFocused()) {
                return trigger.keyPressed(
                        keyCode,
                        scanCode,
                        modifiers
                );
            }

            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            if (command.isFocused()) {
                return command.charTyped(codePoint, modifiers);
            }

            return false;
        }


        @Override
        public void setFocused(boolean focused) {
            super.setFocused(focused);

            if (!focused) {
                clearChildFocus();
            } else if (focusedChild != null) {
                setFocusedChild(focusedChild);
            }
        }

        public void clearFocus() {
            super.setFocused(false);
            focusedChild = null;
            clearChildFocus();
        }

        private void clearChildFocus() {
            command.setFocused(false);
            trigger.setFocused(false);
            remove.setFocused(false);
            suggestions.hide();
        }

        private void setFocusedChild(@Nullable GuiEventListener listener) {
            focusedChild = listener;
            command.setFocused(listener == command);
            trigger.setFocused(listener == trigger);
            remove.setFocused(listener == remove);

            if (listener != command) {
                suggestions.hide();
            }
        }
    }
}