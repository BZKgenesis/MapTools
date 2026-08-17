package net.bzkgns.maptools.client.screen;

import net.bzkgns.maptools.client.screen.components.CommandList;
import net.bzkgns.maptools.client.screen.components.OrientationWidget;
import net.bzkgns.maptools.client.screen.components.Vector3Input;
import net.bzkgns.maptools.entities.entity_detector.EntityDetector;
import net.bzkgns.maptools.entities.entity_detector.EntityDetectorCommand;
import net.bzkgns.maptools.entities.entity_detector.EntityDetectorConfig;
import net.bzkgns.maptools.entities.entity_detector.EntityDetectorTrigger;
import net.bzkgns.maptools.network.C2S.UpdateEntityDetectorPayload;
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
public class EntityDetectorEditScreen extends Screen {

    private final EntityDetector detector;

    Checkbox enabledCheckbox;
    private EditBox displayNameBox;
    private EditBox zoneIdBox;

    private Vector3Input sizeInput;
    private Vector3Input posInput;

    private CommandList<EntityDetectorCommand, EntityDetectorTrigger> commandList;

    public EntityDetectorEditScreen(EntityDetector detector) {
        super(Component.literal("Entity Detector Edit"));
        this.detector = detector;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;

        this.displayNameBox = new EditBox(
                this.font,
                centerX - 125,
                30,
                100,
                20,
                Component.literal("Display Name")
        );
        this.displayNameBox.setValue(detector.getDisplayName().getString());
        this.addRenderableWidget(this.displayNameBox);

        this.zoneIdBox = new EditBox(
                this.font,
                centerX + 25,
                30,
                100,
                20,
                Component.literal("Zone Id")
        );
        this.zoneIdBox.setValue(detector.getZoneId());
        this.addRenderableWidget(this.zoneIdBox);

        this.enabledCheckbox = Checkbox.builder(Component.literal("Enabled"), this.font)
                .pos(centerX + 130,31)
                .selected(detector.isEnabled())
                .onValueChange((checkbox,selected)  -> detector.setEnabled(selected))
                .build();
        this.addRenderableWidget(this.enabledCheckbox);

        int listWidth = 410;
        int listHeight = this.height-90-50;

        this.commandList = new CommandList<>(
                this,
                this.minecraft,
                centerX - listWidth / 2,
                100,
                listWidth,
                listHeight,
                EntityDetectorTrigger.class,
                EntityDetectorTrigger.ON_ENTER,
                EntityDetectorCommand::new
        );
        commandList.loadCommands(detector.getCommands());
        this.addRenderableWidget(commandList);

        int SIZE_BOX_Y = 52;
        int SIZE_BOX_WIDTH = 150;
        int SIZE_BOX_HEIGHT = 20;

        this.sizeInput = new Vector3Input(
                this.font,
                centerX-60, SIZE_BOX_Y,
                SIZE_BOX_WIDTH, SIZE_BOX_HEIGHT,
                "Size",
                Component.literal("Size"),
                detector.getSize(),
                s->detector.setSize(s.x,s.y,s.z)
        );
        this.addRenderableWidget(this.sizeInput);

        this.posInput = new Vector3Input(
                this.font,
                centerX-60, SIZE_BOX_Y+SIZE_BOX_HEIGHT+2,
                SIZE_BOX_WIDTH, SIZE_BOX_HEIGHT,
                "Pos",
                Component.literal("Pos"),
                detector.getPosition(0).toVector3f(),
                s->detector.setPos(s.x,s.y,s.z)
        );
        this.addRenderableWidget(this.posInput);


        this.addRenderableWidget(
                Button.builder(Component.literal("+ Add a command"),
                                button ->commandList.addCommand())
                        .bounds(
                                centerX -220, 75,
                                130, 20
                        ).build()
        );

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Validate"),
                        button -> save())
                    .bounds(
                        centerX - 100, this.height-40,
                        200, 20
                    ).build()
        );

        this.addRenderableWidget(
                new OrientationWidget(
                        this.minecraft,
                        0, 0,
                        50,50
                )
        );
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

        if (sizeInput != null) {
            sizeInput.clearInputFocus();
        }

        if (posInput != null) {
            posInput.clearInputFocus();
        }

        if (this.getFocused() != null) {
            this.setFocused(null);
        }
    }

    private void save() {

        EntityDetectorConfig config =
                new EntityDetectorConfig(
                        enabledCheckbox.selected(),
                        commandList.getCommands(),
                        displayNameBox.getValue(),
                        zoneIdBox.getValue(),
                        sizeInput.getValue().x,
                        sizeInput.getValue().y,
                        sizeInput.getValue().z,
                        posInput.getValue().x,
                        posInput.getValue().y,
                        posInput.getValue().z
                );

        PacketDistributor.sendToServer(
            new UpdateEntityDetectorPayload(
                detector.getId(),
                config
            )
        );

        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void render(
            @NotNull GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.render(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );

        guiGraphics.drawCenteredString(
                this.font,
                this.title,
                this.width / 2,
                20,
                0xFFFFFF
        );

        guiGraphics.drawString(
                this.font,
                "Display Name:",
                this.width / 2 - 125 - this.font.width("Display Name:")-3,
                35,
                0xFFFFFF
        );

        guiGraphics.drawString(
                this.font,
                "Zone Id:",
                this.width / 2 + 25 - this.font.width("Zone Id:")-3,
                35,
                0xFFFFFF
        );



    }
}