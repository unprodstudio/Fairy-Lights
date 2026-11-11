package me.paulf.fairylights.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import me.paulf.fairylights.client.renderer.block.entity.state.FastenerBlockEntityRenderState;
import me.paulf.fairylights.client.renderer.block.entity.state.FastenerRenderState;
import me.paulf.fairylights.server.block.entity.FastenerBlockEntity;
import me.paulf.fairylights.server.capability.CapabilityHandler;
import me.paulf.fairylights.server.fastener.BlockView;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FastenerBlockEntityRenderer implements BlockEntityRenderer<FastenerBlockEntity, FastenerBlockEntityRenderState> {

    private final BlockView view;
    private final FastenerRenderer renderer;

    public FastenerBlockEntityRenderer(final BlockEntityRendererProvider.Context context, final BlockView view) {
        this.view = view;
        this.renderer = new FastenerRenderer(context::bakeLayer);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public FastenerBlockEntityRenderState createRenderState() {
        return new FastenerBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(FastenerBlockEntity blockEntity, FastenerBlockEntityRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        CapabilityHandler.FASTENER_CAP.maybeGet(blockEntity).ifPresent(f -> {
            FastenerRenderState fastenerRenderState = new FastenerRenderState();
            this.renderer.extractRenderState(f, fastenerRenderState, partialTicks, state.lightCoords, OverlayTexture.NO_OVERLAY);
            state.fastener = fastenerRenderState;
            state.offset = blockEntity.getOffset();
        });
    }

    @Override
    public void submit(FastenerBlockEntityRenderState state, PoseStack matrix, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.fastener != null) {
            //this.bindTexture(FastenerRenderer.TEXTURE);
            matrix.pushPose();
            final Vec3 offset = state.offset;
            matrix.translate(offset.x, offset.y, offset.z);
            //this.view.unrotate(this.getWorld(), f.getPos(), FastenerBlockEntityRenderer.GlMatrix.INSTANCE, delta);
            this.renderer.submit(state.fastener, matrix, submitNodeCollector);
            matrix.popPose();
        }
    }
}
