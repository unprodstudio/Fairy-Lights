package me.paulf.fairylights.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.paulf.fairylights.client.ClientProxy;
import me.paulf.fairylights.client.FLModelLayers;
import me.paulf.fairylights.client.renderer.block.entity.state.connection.GarlandTinselConnectionRenderState;
import me.paulf.fairylights.server.connection.GarlandTinselConnection;
import me.paulf.fairylights.util.Catenary;
import me.paulf.fairylights.util.RandomArray;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;

import java.util.function.Function;

public class GarlandTinselRenderer extends ConnectionRenderer<GarlandTinselConnection, GarlandTinselConnectionRenderState> {
    private static final RandomArray RAND = new RandomArray(9171, 128);

    private final StripModel strip;

    public GarlandTinselRenderer(final Function<ModelLayerLocation, ModelPart> baker) {
        super(baker, FLModelLayers.TINSEL_WIRE);
        this.strip = new StripModel(baker.apply(FLModelLayers.TINSEL_STRIP));
    }

    @Override
    protected int getWireColor(final GarlandTinselConnection conn) {
        return conn.getColor();
    }

    @Override
    public GarlandTinselConnectionRenderState createRenderState() {
        return new GarlandTinselConnectionRenderState();
    }

    @Override
    public void extractRenderState(GarlandTinselConnection conn, GarlandTinselConnectionRenderState state, float delta) {
        super.extractRenderState(conn, state, delta);
        state.color = conn.getColor();
        state.hash = conn.getUUID().hashCode();
    }

    @Override
    protected void submitSegment(final GarlandTinselConnectionRenderState connection, final Catenary.SegmentView it, final PoseStack matrix, final SubmitNodeCollector collector) {
        super.submitSegment(connection, it, matrix, collector);
        final int color = ARGB.opaque(connection.color);
        matrix.pushPose();
        matrix.translate(it.getX(0.0F), it.getY(0.0F), it.getZ(0.0F));
        matrix.rotate(Axis.YP.rotation(-it.getYaw()));
        matrix.rotate(Axis.ZP.rotation(it.getPitch()));
        final float length = it.getLength();
        final int rings = Mth.ceil(length * 64);
        final int hash = connection.hash;
        final int index = it.getIndex();
        for (int i = 0; i < rings; i++) {
            final double t = i / (float) rings * length;
            matrix.pushPose();
            matrix.translate(t, 0.0F, 0.0F);
            final float rotX = RAND.get(31 * (index + 31 * i) + hash) * 22;
            final float rotY = RAND.get(31 * (index + 3 + 31 * i) + hash) * 180;
            final float rotZ = RAND.get(31 * (index + 7 + 31 * i) + hash) * 180;
            matrix.rotate(Axis.XP.rotationDegrees(rotZ));
            matrix.rotate(Axis.YP.rotationDegrees(rotY));
            matrix.rotate(Axis.ZP.rotationDegrees(rotX));
            matrix.scale(1.0F, RAND.get(i * 63) * 0.1F + 1.0F, 0.5F);
            collector.submitModel(this.strip, Unit.INSTANCE, matrix, RenderTypes.entityCutout(ClientProxy.SOLID_TEXTURE), connection.lightCoords, connection.overlayCoords, color, null, 0);
            matrix.popPose();
        }
        
        matrix.popPose();
    }

    public static LayerDefinition wireLayer() {
        return WireModel.createLayer(62, 0, 1);
    }

    public static class StripModel extends Model<Unit> {
        StripModel(final ModelPart root) {
            super(root, RenderTypes::entityCutout);
        }

        public static LayerDefinition createLayer() {
            MeshDefinition mesh = new MeshDefinition();
            mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create()
                .texOffs(62, 0)
                .addBox(-0.5F, -3.0F, 0.0F, 1.0F, 6.0F, 0.0F), PartPose.ZERO);
            return LayerDefinition.create(mesh, 128, 128);
        }
    }
}
