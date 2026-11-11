package me.paulf.fairylights.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.paulf.fairylights.client.FLModelLayers;
import me.paulf.fairylights.client.renderer.block.entity.state.connection.HangingLightsConnectionRenderState;
import me.paulf.fairylights.client.renderer.block.entity.state.LightRenderState;
import me.paulf.fairylights.server.connection.HangingLightsConnection;
import me.paulf.fairylights.server.feature.light.Light;
import me.paulf.fairylights.server.item.SimpleLightVariant;
import me.paulf.fairylights.util.FLMth;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

public class HangingLightsRenderer extends ConnectionRenderer<HangingLightsConnection, HangingLightsConnectionRenderState> {
    private final LightRenderer lights;

    public HangingLightsRenderer(final Function<ModelLayerLocation, ModelPart> baker) {
        super(baker, FLModelLayers.LIGHTS_WIRE);
        this.lights = new LightRenderer(baker);
    }

    @Override
    protected int getWireColor(final HangingLightsConnection conn) {
        return conn.getString().getColor();
    }

    @Override
    public HangingLightsConnectionRenderState createRenderState() {
        return new HangingLightsConnectionRenderState();
    }

    @Override
    public void extractRenderState(HangingLightsConnection conn, HangingLightsConnectionRenderState state, float delta) {
        super.extractRenderState(conn, state, delta);
        for (Light<?> feature : conn.getFeatures()) {
            state.features.add(LightRenderState.fromLight(feature, delta));
        }
    }

    @Override
    public void submit(final HangingLightsConnectionRenderState conn, final PoseStack matrix, final SubmitNodeCollector collector) {
        super.submit(conn, matrix, collector);
        for (int i = 0; i < conn.features.size(); i++) {
            final LightRenderState light = conn.features.get(i);
            final Vec3 pos = light.point;
            matrix.pushPose();
            matrix.translate(pos.x, pos.y, pos.z);
            matrix.rotate(Axis.YP.rotation(-light.yaw));
            if (light.parallelsCord) {
                matrix.rotate(Axis.ZP.rotation(light.pitch));
            }
            matrix.rotate(Axis.XP.rotation(light.roll));
            if (light.variant != SimpleLightVariant.FAIRY_LIGHT) { // FIXME
                matrix.rotate(Axis.YP.rotation(FLMth.mod(FLMth.hash(i) * FLMth.DEG_TO_RAD, FLMth.TAU) + FLMth.PI / 4.0F));
            }
            matrix.translate(0.0D, -light.descent, 0.0D);
            this.lights.submit(matrix, collector, light, i, conn.lightCoords, conn.overlayCoords);
            matrix.popPose();
        }
    }

    public static LayerDefinition wireLayer() {
        return WireModel.createLayer(0, 0, 2);
    }
}
