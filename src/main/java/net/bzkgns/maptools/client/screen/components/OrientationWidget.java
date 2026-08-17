package net.bzkgns.maptools.client.screen.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class OrientationWidget extends AbstractWidget {

    private final Minecraft minecraft;

    public OrientationWidget(
            Minecraft minecraft,
            int x,
            int y,
            int width,
            int height
    ) {
        super(x, y, width, height, Component.empty());
        this.minecraft = minecraft;
    }

    @Override
    protected void renderWidget(
            @NotNull GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        if (minecraft.player == null) {
            return;
        }

        float yaw = minecraft.player.getViewYRot(partialTick);
        float pitch = minecraft.player.getViewXRot(partialTick);

        Matrix4f rotation = new Matrix4f()
                .rotateX((float) Math.toRadians(pitch))
                .rotateY((float) Math.toRadians(yaw));

        Vector4f xAxis = new Vector4f(-1, 0, 0, 1).mul(rotation);
        Vector4f yAxis = new Vector4f(0, 1, 0, 1).mul(rotation);
        Vector4f zAxis = new Vector4f(0, 0, -1, 1).mul(rotation);

        ArrayList<Tuple<Tuple<Vector4f,Vector4f>,Integer>> floorLines = new ArrayList<>();

        int lineDensity = 3;

        for (int i = -lineDensity; i <= lineDensity;i ++){
            floorLines.add(
                    new Tuple<>(
                        new Tuple<>(
                            new Vector4f(i/(float)lineDensity, 0, 1.0f, 1).mul(rotation),
                            new Vector4f(i/(float)lineDensity, 0, -1.0f, 1).mul(rotation)
                        ),
                    0x77000000)
            );
        }

        for (int i = -lineDensity; i <= lineDensity;i ++){
            floorLines.add(
                    new Tuple<>(
                            new Tuple<>(
                                    new Vector4f(1.0f, 0, i/(float)lineDensity, 1).mul(rotation),
                                    new Vector4f(-1.0f, 0, i/(float)lineDensity, 1).mul(rotation)
                            ),
                            0x77000000)
            );
        }

        int centerX = this.getX() + this.getWidth() / 2;
        int centerY = this.getY() + this.getHeight() / 2;

        float cameraDistance = 4.0f;

        float size = this.getWidth()/2f;

        for (Tuple<Tuple<Vector4f,Vector4f>,Integer> line : floorLines ){
            draw3DLine(
                    guiGraphics,
                    centerX,
                    centerY,
                    line.getA().getA(),
                    line.getA().getB(),
                    size,
                    cameraDistance,
                    line.getB()
            );
        }


        List<Tuple<Vector4f,Integer>> axes = new ArrayList<>(List.of(new Tuple<>(xAxis, 0xAAFF0000),new Tuple<>(yAxis,0xAA00FF00),new Tuple<>(zAxis,0xAA0000FF)));
        axes.sort((x1,x2) -> Float.compare(x1.getA().z,x2.getA().z));

        for (Tuple<Vector4f,Integer> axe : axes ){
            drawAxis(
                    guiGraphics,
                    centerX,
                    centerY,
                    axe.getA(),
                    size,
                    cameraDistance,
                    axe.getB()
            );
        }
    }

    @Override
    protected void updateWidgetNarration(
            @NotNull NarrationElementOutput narrationElementOutput
    ) {
    }
    private void drawAxis(
            GuiGraphics graphics,
            int centerX,
            int centerY,
            Vector4f p2,
            float size,
            float cameraDistance,
            int color
    ){
        draw3DLine(
                graphics,
                centerX,
                centerY,
                new Vector4f(0,0,0,1.0f),
                p2,
                size,
                cameraDistance,
                color
        );
    }

    private void draw3DLine(
            GuiGraphics graphics,
            int centerX,
            int centerY,
            Vector4f p1,
            Vector4f p2,
            float size,
            float cameraDistance,
            int color
    ) {
        float x1 = p1.x();
        float y1 = p1.y();
        float z1 = p1.z();

        float x2 = p2.x();
        float y2 = p2.y();
        float z2 = p2.z();

        float depth1 = cameraDistance - z1;
        float depth2 = cameraDistance - z2;

        if (depth1 < 0.1f || depth2 < 0.1f) {
            return;
        }

        float perspective1 = cameraDistance / depth1;
        float perspective2 = cameraDistance / depth2;

        float screenX1 = x1 * size * perspective1;
        float screenY1 = y1 * size * perspective1;

        float screenX2 = x2 * size * perspective2;
        float screenY2 = y2 * size * perspective2;

        drawLine(
                graphics,
                centerX + screenX1,
                centerY - screenY1,
                centerX + screenX2,
                centerY - screenY2,
                color
        );
    }

    private void drawLine(
            GuiGraphics graphics,
            float x1,
            float y1,
            float x2,
            float y2,
            int color
    ) {
        float dx = x2 - x1;
        float dy = y2 - y1;

        float length = Mth.sqrt(dx * dx + dy * dy);

        if (length < 0.001f) {
            return;
        }

        float nx = dx / length;
        float ny = dy / length;

        for (int i = 0; i < Math.ceil(length); i++) {
            int x = Math.round(x1 + nx * i);
            int y = Math.round(y1 + ny * i);

            graphics.fill(x, y, x + 2, y + 2, color);
        }
    }
}
