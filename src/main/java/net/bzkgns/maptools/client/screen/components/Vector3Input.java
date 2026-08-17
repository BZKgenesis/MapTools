package net.bzkgns.maptools.client.screen.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;


public class Vector3Input extends AbstractContainerWidget {

    private final EditBox xBox;
    private final EditBox yBox;
    private final EditBox zBox;
    private final Font font;
    private final String name;
    private final Consumer<Vector3f> setter;

    public Vector3Input(
            Font font,
            int x,
            int y,
            int width,
            int height,
            String name,
            Component message,
            Vector3f initialValue,
            Consumer<Vector3f> setter
    ) {
        super(x, y, width, height, message);
        this.name = name;
        this.font = font;
        this.setter = setter;
        int spacing = 4;
        int boxWidth = (width - spacing * 2) / 3;

        this.xBox = createBox(
                font,
                x,
                y,
                boxWidth,
                initialValue.x()
        );
        this.xBox.setTextColor(0xFFFF8888);

        this.yBox = createBox(
                font,
                x + boxWidth + spacing,
                y,
                boxWidth,
                initialValue.y()
        );
        this.yBox.setTextColor(0xFF88FF88);

        this.zBox = createBox(
                font,
                x + (boxWidth + spacing) * 2,
                y,
                boxWidth,
                initialValue.z()
        );
        this.zBox.setTextColor(0xFF8888FF);
    }

    private EditBox createBox(
            Font font,
            int x,
            int y,
            int width,
            float value
    ) {
        EditBox box = new EditBox(
                font,
                x,
                y,
                width,
                height,
                Component.empty()
        );

        box.setMaxLength(32);
        box.setValue(Float.toString(value));

        box.setResponder(
                s -> this.setter.accept(this.getValue())
        );

        return box;
    }

    public Vector3f getValue() {
        return new Vector3f(
                parseFloat(xBox.getValue(), 0.0f),
                parseFloat(yBox.getValue(), 0.0f),
                parseFloat(zBox.getValue(), 0.0f)
        );
    }

    private float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @SuppressWarnings("unused")
    public void setValue(Vector3f value) {
        xBox.setValue(Float.toString(value.x()));
        yBox.setValue(Float.toString(value.y()));
        zBox.setValue(Float.toString(value.z()));
    }

    @SuppressWarnings("unused")
    public EditBox getXBox() {
        return xBox;
    }

    @SuppressWarnings("unused")
    public EditBox getYBox() {
        return yBox;
    }

    @SuppressWarnings("unused")
    public EditBox getZBox() {
        return zBox;
    }

    public void clearInputFocus() {
        setFocused(null);
    }

    @Override
    protected void renderWidget(
            @NotNull GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        xBox.render(guiGraphics, mouseX, mouseY, partialTick);
        yBox.render(guiGraphics, mouseX, mouseY, partialTick);
        zBox.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawString(this.font, this.name, getX()-this.font.width(this.name)-3, getY()+getHeight()/2-this.font.lineHeight/2, Color.WHITE.getRGB());
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return List.of(
                xBox,
                yBox,
                zBox
        );
    }

    @Override
    protected void updateWidgetNarration(
            @NotNull NarrationElementOutput narrationElementOutput
    ) {
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }

        if (xBox.mouseClicked(mouseX, mouseY, button)) {
            setFocused(xBox);
            return true;
        }

        if (yBox.mouseClicked(mouseX, mouseY, button)) {
            setFocused(yBox);
            return true;
        }

        if (zBox.mouseClicked(mouseX, mouseY, button)) {
            setFocused(zBox);
            return true;
        }

        return false;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener listener) {
        super.setFocused(listener);

        xBox.setFocused(listener == xBox);
        yBox.setFocused(listener == yBox);
        zBox.setFocused(listener == zBox);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);

        if (!focused) {
            xBox.setFocused(false);
            yBox.setFocused(false);
            zBox.setFocused(false);
        } else {
            GuiEventListener f = this.getFocused();
            xBox.setFocused(f == xBox);
            yBox.setFocused(f == yBox);
            zBox.setFocused(f == zBox);
        }
    }

    @Override
    public boolean mouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {
        return xBox.mouseReleased(mouseX, mouseY, button)
                || yBox.mouseReleased(mouseX, mouseY, button)
                || zBox.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        return xBox.keyPressed(keyCode, scanCode, modifiers)
                || yBox.keyPressed(keyCode, scanCode, modifiers)
                || zBox.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(
            char codePoint,
            int modifiers
    ) {
        return xBox.charTyped(codePoint, modifiers)
                || yBox.charTyped(codePoint, modifiers)
                || zBox.charTyped(codePoint, modifiers);
    }
}