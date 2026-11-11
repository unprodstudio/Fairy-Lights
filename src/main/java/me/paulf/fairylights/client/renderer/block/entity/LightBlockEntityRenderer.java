package me.paulf.fairylights.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.paulf.fairylights.client.model.light.LightModel;
import me.paulf.fairylights.client.renderer.block.entity.state.LightBlockEntityRenderState;
import me.paulf.fairylights.client.renderer.block.entity.state.LightRenderState;
import me.paulf.fairylights.server.block.LightBlock;
import me.paulf.fairylights.server.block.entity.LightBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class LightBlockEntityRenderer implements BlockEntityRenderer<LightBlockEntity, LightBlockEntityRenderState> {
    private final LightRenderer lights;

    public LightBlockEntityRenderer(final BlockEntityRendererProvider.Context context) {
        this.lights = new LightRenderer(context::bakeLayer);
    }

    @Override
    public LightBlockEntityRenderState createRenderState() {
        return new LightBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(LightBlockEntity blockEntity, LightBlockEntityRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        final BlockState blockState = blockEntity.getBlockState();
        state.face = blockState.getValue(LightBlock.FACE);
        state.rotation = blockState.getValue(LightBlock.FACING).toYRot();

        var light = blockEntity.getLight();
        state.isOrientable = light.getVariant().isOrientable();
        state.light = LightRenderState.fromLight(light, partialTicks);
        state.model = this.lights.getModel(state.light, -1);
    }

    @Override
    public void submit(LightBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {

    }

    private void submit(final LightBlockEntityRenderState state, final PoseStack matrix, final SubmitNodeCollector collector) {
        final LightModel<?> model = state.model;
        final AABB box = model.getBounds();

        final AttachFace face = state.face;
        final float rotation = state.rotation;
        matrix.pushPose();
        matrix.translate(0.5D, 0.5D, 0.5D);
        matrix.rotate(Axis.YP.rotationDegrees(180.0F - rotation));
        if (state.isOrientable) {
            if (face == AttachFace.WALL) {
                matrix.rotate(Axis.XP.rotationDegrees(90.0F));
            } else if (face == AttachFace.FLOOR) {
                matrix.rotate(Axis.XP.rotationDegrees(-180.0F));
            }
            matrix.translate(0.0D, 0.5D, 0.0D);
        } else {
            if (face == AttachFace.CEILING) {
                matrix.translate(0.0D, 0.25D, 0.0D);
            } else if (face == AttachFace.WALL) {
                matrix.translate(0.0D, 3.0D / 16.0D, 0.125D);
            } else {
                matrix.translate(0.0D, -box.minY - model.getFloorOffset() - 0.5D, 0.0D);
            }
        }
        this.lights.submit(matrix, collector, state.light, model, state.lightCoords, OverlayTexture.NO_OVERLAY);
        matrix.popPose();
    }
}
