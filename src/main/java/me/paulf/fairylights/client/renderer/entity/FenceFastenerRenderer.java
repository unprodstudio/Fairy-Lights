package me.paulf.fairylights.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.client.renderer.block.entity.FastenerRenderer;
import me.paulf.fairylights.client.renderer.block.entity.state.FastenerRenderState;
import me.paulf.fairylights.server.capability.CapabilityHandler;
import me.paulf.fairylights.server.entity.FenceFastenerEntity;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4f;

public final class FenceFastenerRenderer extends EntityRenderer<FenceFastenerEntity, FenceFastenerRenderState> {
    public static final Identifier MODEL = FairyLights.id("block/fence_fastener");
    public static final ExtraModelKey<BlockStateModel> MODEL_KEY = ExtraModelKey.create();

    private final FastenerRenderer renderer;

    public FenceFastenerRenderer(final EntityRendererProvider.Context context) {
        super(context);
        this.renderer = new FastenerRenderer(context::bakeLayer);
    }

    @Override
    protected int getBlockLightLevel(final FenceFastenerEntity entity, final BlockPos delta) {
        return entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition());
    }

    @Override
    public FenceFastenerRenderState createRenderState() {
        return new FenceFastenerRenderState();
    }

    @Override
    public void extractRenderState(FenceFastenerEntity entity, FenceFastenerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        var model = Minecraft.getInstance().getModelManager().getModel(MODEL_KEY);
        model.collectParts(entity.getRandom(), state.model.setupModel(new Matrix4f(), false));

        CapabilityHandler.FASTENER_CAP.maybeGet(entity).ifPresent(f -> {
            var fastenerState = new FastenerRenderState();
            this.renderer.extractRenderState(f, fastenerState, partialTicks, fastenerState.lightCoords, OverlayTexture.NO_OVERLAY);

            state.fastener = fastenerState;
        });
    }

    @Override
    public void submit(FenceFastenerRenderState state, PoseStack matrix, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        matrix.pushPose();
        state.model.submit(matrix, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        matrix.popPose();
        if (state.fastener != null)
            this.renderer.submit(state.fastener, matrix, submitNodeCollector);

        super.submit(state, matrix, submitNodeCollector, camera);
    }
}
