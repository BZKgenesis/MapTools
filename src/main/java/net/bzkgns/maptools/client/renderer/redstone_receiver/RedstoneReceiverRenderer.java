package net.bzkgns.maptools.client.renderer.redstone_receiver;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bzkgns.maptools.entities.redstone_receiver.RedstoneReceiver;
import net.bzkgns.maptools.items.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

public class RedstoneReceiverRenderer extends EntityRenderer<RedstoneReceiver> {

    private final BlockRenderDispatcher blockRendererRedstone;

    public RedstoneReceiverRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRendererRedstone = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(
            @NotNull RedstoneReceiver entity,
            float entityYaw,
            float partialTick,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource buffer,
            int packedLight
    ) {

        int fullBright = LightTexture.pack(15, 15);

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) return;


        if (!minecraft.player.getMainHandItem().is(ModItems.REDSTONE_RECEIVER_EDITOR.get())) return;


        poseStack.pushPose();

        poseStack.translate(-0.5D, 0.0D, -0.5D);
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(1.02F, 1.02F, 1.02F);
        poseStack.translate(-0.5, -0.5, -0.5);



        BlockState visibleBlockState;
        if (entity.isEnabled()){
            if (entity.isPowered()) {
                visibleBlockState = Blocks.REDSTONE_LAMP.defaultBlockState().setValue(BlockStateProperties.LIT, true);
            } else {
                visibleBlockState = Blocks.REDSTONE_LAMP.defaultBlockState().setValue(BlockStateProperties.LIT, false);
            }
        } else {
            visibleBlockState = Blocks.DEAD_BRAIN_CORAL_BLOCK.defaultBlockState();
        }
        BlockState occludedBlockState;
        if (entity.isEnabled()){
            if (entity.isPowered()) {
                occludedBlockState = Blocks.RED_STAINED_GLASS.defaultBlockState();
            } else {
                occludedBlockState = Blocks.GRAY_STAINED_GLASS.defaultBlockState();
            }
        } else {
            occludedBlockState = Blocks.TINTED_GLASS.defaultBlockState();
        }

        blockRendererRedstone.renderSingleBlock(
                visibleBlockState,
                poseStack,
                buffer,
                fullBright,
                OverlayTexture.NO_OVERLAY,
                ModelData.builder().build(),
                RedstoneReceiverRenderTypes.VISIBLE
        );
        if (entity.isXrayVisible()){
            blockRendererRedstone.renderSingleBlock(
                    occludedBlockState,
                    poseStack,
                    buffer,
                    fullBright,
                    OverlayTexture.NO_OVERLAY,
                    ModelData.builder().build(),
                    RedstoneReceiverRenderTypes.OCCLUDED
            );
        }


        poseStack.translate(0.5D, .25D, 0.5D);

        renderNameTag(
                entity,
                Component.empty().append(entity.getDisplayName()).withColor(0xFF5555FF),
                poseStack,
                buffer,
                fullBright,
                partialTick
        );


        poseStack.popPose();

    }

    @Override
    public ResourceLocation getTextureLocation(RedstoneReceiver entity) {return null;}
}