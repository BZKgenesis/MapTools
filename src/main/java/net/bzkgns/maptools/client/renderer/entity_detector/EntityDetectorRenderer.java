package net.bzkgns.maptools.client.renderer.entity_detector;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bzkgns.maptools.entities.entity_detector.EntityDetector;
import net.bzkgns.maptools.items.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class EntityDetectorRenderer extends EntityRenderer<EntityDetector> {

    private final BlockRenderDispatcher blockRendererRedstone;

    public EntityDetectorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRendererRedstone = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(
            @NotNull EntityDetector entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            @NotNull MultiBufferSource buffer,
            int packedLight
    ) {
        int fullBright = LightTexture.pack(15, 15);

        Minecraft minecraft = Minecraft.getInstance();
        Matrix4f matrix4f = poseStack.last().pose();

        if (minecraft.player == null) return;
        if (!minecraft.player.getMainHandItem().is(ModItems.ENTITY_DETECTOR_EDITOR.get())) return;
        BlockState blockState;
        EntityDetector target = getEntityLookingAt(EntityDetector.class, minecraft.player, 4.5f);


        if (entity.isEnabled()) {
            if (target != null && target.equals(entity)) {
                blockState = Blocks.GREEN_STAINED_GLASS.defaultBlockState();
            } else {
                blockState = Blocks.GRAY_STAINED_GLASS.defaultBlockState();
            }
        }else{
            blockState = Blocks.RED_STAINED_GLASS.defaultBlockState();
        }

        EntityDimensions dimensions = entity.getDimensions(Pose.STANDING);

        float x = dimensions.width()/2f;
        float y = dimensions.height();

        {VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.debugLineStrip(1.0));
        vertexconsumer.addVertex(matrix4f, x, 0f, x).setColor(1.0F, 1.0F, 1.0F, 1.0F);
        vertexconsumer.addVertex(matrix4f, -x, 0f, x).setColor(1.0F, 1.0F, 1.0F, 1.0F);
        vertexconsumer.addVertex(matrix4f, -x, 0f, -x).setColor(1.0F, 1.0F, 1.0F, 1.0F);
        vertexconsumer.addVertex(matrix4f, x, 0f, -x).setColor(1.0F, 1.0F, 1.0F, 1.0F);
        vertexconsumer.addVertex(matrix4f, x, 0f, x).setColor(1.0F, 1.0F, 1.0F, 1.0F);

        vertexconsumer.addVertex(matrix4f, x, y, x).setColor(1.0F, 1.0F, 1.0F, 1.0F);
        vertexconsumer.addVertex(matrix4f, -x, y, x).setColor(1.0F, 1.0F, 1.0F, 1.0F);
        vertexconsumer.addVertex(matrix4f, -x, y, -x).setColor(1.0F, 1.0F, 1.0F, 1.0F);
        vertexconsumer.addVertex(matrix4f, x, y, -x).setColor(1.0F, 1.0F, 1.0F, 1.0F);
        vertexconsumer.addVertex(matrix4f, x, y, x).setColor(1.0F, 1.0F, 1.0F, 1.0F);}

        {VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.debugLineStrip(1.0));
        vertexconsumer.addVertex(matrix4f, x, 0f, x).setColor(1.0F, 1.0F, 1.0F, 1.0F);
        vertexconsumer.addVertex(matrix4f, x, y, x).setColor(1.0F, 1.0F, 1.0F, 1.0F);}
        {VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.debugLineStrip(1.0));
        vertexconsumer.addVertex(matrix4f, -x, 0f, x).setColor(1.0F, 1.0F, 1.0F, 1.0F);
        vertexconsumer.addVertex(matrix4f, -x, y, x).setColor(1.0F, 1.0F, 1.0F, 1.0F);}
        {VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.debugLineStrip(1.0));
        vertexconsumer.addVertex(matrix4f, x, 0f, -x).setColor(1.0F, 1.0F, 1.0F, 1.0F);
        vertexconsumer.addVertex(matrix4f, x, y, -x).setColor(1.0F, 1.0F, 1.0F, 1.0F);}
        {VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.debugLineStrip(1.0));
        vertexconsumer.addVertex(matrix4f, -x, 0f, -x).setColor(1.0F, 1.0F, 1.0F, 1.0F);
        vertexconsumer.addVertex(matrix4f, -x, y, -x).setColor(1.0F, 1.0F, 1.0F, 1.0F);}

        poseStack.pushPose();

        poseStack.translate(0D, .25D, 0D);

        renderNameTag(
                entity,
                Component.empty().append(entity.getDisplayName()).withColor(0xFFFF5555),
                poseStack,
                buffer,
                fullBright,
                partialTick
        );
        poseStack.translate(0D, -.25D, 0D);


        Vector3f size = entity.getSize();
        poseStack.translate(-size.x/2f, 0, -size.z/2f);

        poseStack.scale(size.x,size.y, size.z);

        blockRendererRedstone.renderSingleBlock(
                blockState,
                poseStack,
                buffer,
                fullBright,
                OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY,
                RenderType.TRANSLUCENT
            );




        poseStack.popPose();


    }

    @Override
    public ResourceLocation getTextureLocation(EntityDetector entity) {return null;}

    @Nullable
    public <V extends Entity> V getEntityLookingAt(Class<V> entityClass, Player player, double maxDistance) {
        Vec3 startVec = player.getEyePosition(1.0F);
        Vec3 viewVec = player.getViewVector(1.0F);
        Vec3 endVec = startVec.add(viewVec.x * maxDistance, viewVec.y * maxDistance, viewVec.z * maxDistance);

        AABB boundingBox = player.getBoundingBox().expandTowards(viewVec.scale(maxDistance)).inflate(1.0D);

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                player,
                startVec,
                endVec,
                boundingBox,
                (entity) -> !entity.isSpectator() && entity.isPickable() && entityClass.isInstance(entity),
                maxDistance * maxDistance
        );

        return hitResult != null ? (entityClass.cast(hitResult.getEntity()))  : null;
    }
}