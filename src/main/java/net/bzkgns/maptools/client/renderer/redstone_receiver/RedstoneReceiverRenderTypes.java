package net.bzkgns.maptools.client.renderer.redstone_receiver;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.lwjgl.opengl.GL11;

public final class RedstoneReceiverRenderTypes {

    private RedstoneReceiverRenderTypes() {}

    public static final RenderType VISIBLE = RenderType.create(
            "redstone_receiver_visible",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            4194304,
            true,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_SOLID_SHADER)
                    .setTextureState(
                            new RenderStateShard.TextureStateShard(
                                    TextureAtlas.LOCATION_BLOCKS,
                                    false,
                                    false
                            )
                    )
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .setDepthTestState(
                            new RenderStateShard.DepthTestStateShard(
                                    "less",
                                    GL11.GL_LESS
                            )
                    )
                    .setCullState(RenderStateShard.CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .createCompositeState(true)
    );


    public static final RenderType OCCLUDED = RenderType.create(
            "redstone_receiver_occluded",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            4194304,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_TRANSLUCENT_SHADER)
                    .setTextureState(
                            new RenderStateShard.TextureStateShard(
                                    TextureAtlas.LOCATION_BLOCKS,
                                    false,
                                    false
                            )
                    )
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(
                            new RenderStateShard.DepthTestStateShard(
                                    "greater",
                                    GL11.GL_GREATER
                            )
                    )
                    .setCullState(RenderStateShard.CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );
}