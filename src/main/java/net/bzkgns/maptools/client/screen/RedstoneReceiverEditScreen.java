package net.bzkgns.maptools.client.screen;

import net.bzkgns.maptools.client.screen.components.CommandList;
import net.bzkgns.maptools.client.screen.components.OrientationWidget;
import net.bzkgns.maptools.entities.redstone_receiver.RedstoneReceiver;
import net.bzkgns.maptools.entities.redstone_receiver.RedstoneReceiverCommand;
import net.bzkgns.maptools.entities.redstone_receiver.RedstoneReceiverConfig;
import net.bzkgns.maptools.entities.redstone_receiver.RedstoneReceiverTrigger;
import net.bzkgns.maptools.network.C2S.UpdateRedstoneReceiverPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class RedstoneReceiverEditScreen extends Screen {

    private final RedstoneReceiver receiver;

    private Checkbox enabledCheckbox;
    private EditBox displayNameBox;
    private Checkbox xrayVisibleCheckbox;

    private CommandList<RedstoneReceiverCommand, RedstoneReceiverTrigger> commandList;

    public RedstoneReceiverEditScreen(RedstoneReceiver receiver) {
        super(Component.literal("Redstone Receiver"));
        this.receiver = receiver;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;

        this.displayNameBox = new EditBox(
                this.font,
                centerX - 100,
                30,
                200,
                20,
                Component.literal("Display Name"));
        this.displayNameBox.setValue(receiver.getDisplayName().getString());
        this.addRenderableWidget(displayNameBox);

        this.enabledCheckbox = Checkbox.builder(Component.literal("Enabled"), this.font)
                .pos(centerX + 105,
                        31)
                .selected(receiver.isEnabled())
                .onValueChange((checkbox, selected) -> receiver.setEnabled(selected))
                .build();
        this.addRenderableWidget(enabledCheckbox);

        this.xrayVisibleCheckbox = Checkbox.builder(
                Component.literal("Xray visibility"),
                this.font)
                .pos(centerX + 105,
                        55)
                .selected(receiver.isXrayVisible())
                .onValueChange((checkbox, selected) -> receiver.setXrayVisible(selected))
                .build();
        this.addRenderableWidget(xrayVisibleCheckbox);

        int listWidth = 410;
        int listHeight = this.height - 90 - 50;

        this.commandList = new CommandList<>(
                this,
                this.minecraft,
                this.font,
                centerX - listWidth / 2,
                100,
                listWidth,
                listHeight,
                RedstoneReceiverTrigger.class,
                RedstoneReceiverTrigger.ON_SIGNAL,
                RedstoneReceiverCommand::new

        );
        commandList.loadCommands(receiver.getCommands());
        this.addRenderableWidget(commandList);

        this.addRenderableWidget(
                Button.builder(Component.literal("+ Add a command"),
                        button -> commandList.addCommand())
                        .bounds(
                                centerX - 220,
                                75,
                                130,
                                20)
                        .build());

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Validate"),
                        button -> save())
                        .bounds(
                                centerX - 100,
                                this.height - 40,
                                200,
                                20)
                        .build());

        this.addRenderableWidget(
                new OrientationWidget(
                        this.minecraft,
                        0,
                        0,
                        50, 50));
    }

    private void save() {

        RedstoneReceiverConfig config = new RedstoneReceiverConfig(
                enabledCheckbox.selected(),
                commandList.getCommands(),
                displayNameBox.getValue(),
                xrayVisibleCheckbox.selected());

        PacketDistributor.sendToServer(
                new UpdateRedstoneReceiverPayload(
                        receiver.getId(),
                        config));

        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);

        if (!handled) {
            clearInputFocus();
        }

        return handled;
    }

    private void clearInputFocus() {
        if (commandList != null) {
            commandList.clearInputFocus();
        }

    }

    @Override
    public void render(
            @NotNull GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick) {
        super.render(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick);

        guiGraphics.drawCenteredString(
                this.font,
                this.title,
                this.width / 2,
                20,
                0xFFFFFF);

        guiGraphics.drawString(
                this.font,
                "Display Name:",
                this.width / 2 - 100 - this.font.width("Display Name:") - 3,
                35,
                0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
