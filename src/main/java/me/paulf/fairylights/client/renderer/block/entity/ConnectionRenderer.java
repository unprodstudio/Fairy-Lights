package me.paulf.fairylights.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.paulf.fairylights.client.ClientProxy;
import me.paulf.fairylights.client.renderer.block.entity.state.connection.ConnectionRenderState;
import me.paulf.fairylights.server.connection.Connection;
import me.paulf.fairylights.util.Catenary;
import me.paulf.fairylights.util.Curve;
import me.paulf.fairylights.util.FLMth;
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
import net.minecraft.util.Unit;

import java.util.function.Function;

public abstract class ConnectionRenderer<C extends Connection, S extends ConnectionRenderState> {
    private final WireModel model;
    private final float wireInflate;

    protected ConnectionRenderer(final Function<ModelLayerLocation, ModelPart> baker, final ModelLayerLocation wireModelLocation) {
        this(baker, wireModelLocation, 0.0F);
    }

    protected ConnectionRenderer(final Function<ModelLayerLocation, ModelPart> baker, final ModelLayerLocation wireModelLocation, final float wireInflate) {
        this.model = new WireModel(baker.apply(wireModelLocation));
        this.wireInflate = wireInflate;
    }

    public abstract S createRenderState();

    public void extractRenderState(C conn, S state, float delta) {
        final Curve currCat = conn.getCatenary();
        final Curve prevCat = conn.getPrevCatenary();

        if (currCat != null && prevCat != null) {
            state.catenary = prevCat.lerp(currCat, delta);
            state.wireColor = this.getWireColor(conn);
        }
    }

    public void submit(final S state, final PoseStack matrix, final SubmitNodeCollector collector) {
        if (state.catenary != null) {
            final Curve cat = state.catenary;
            final Curve.SegmentIterator it = cat.iterator();
            final int color = ARGB.opaque(state.wireColor);
            while (it.next()) {
                matrix.pushPose();
                matrix.translate(it.getX(0.0F), it.getY(0.0F),  it.getZ(0.0F));
                matrix.rotate(Axis.YP.rotation(FLMth.PI / 2.0F - it.getYaw()));
                matrix.rotate(Axis.XP.rotation(-it.getPitch()));
                matrix.scale(1.0F + this.wireInflate, 1.0F, it.getLength() * 16.0F);
                collector.submitModel(this.model, Unit.INSTANCE, matrix, RenderTypes.entityCutout(ClientProxy.SOLID_TEXTURE), state.lightCoords, state.overlayCoords, color, null, 0);
                matrix.popPose();
                this.submitSegment(state, it, matrix, collector);
            }
            this.submit(state, cat, matrix, collector);
        }
    }

    protected int getWireColor(final C conn) {
        return 0xFFFFFF;
    }

    protected void submit(final S state, final Curve catenary, final PoseStack matrix, final SubmitNodeCollector collector) {}

    protected void submitSegment(final S state, final Catenary.SegmentView it, final PoseStack matrix, final SubmitNodeCollector collector) {}

    public static class WireModel extends Model<Unit> {
        WireModel(final ModelPart root) {
            super(root, RenderTypes::entityCutout);
        }

        public static LayerDefinition createLayer(final int u, final int v, final int size) {
            MeshDefinition mesh = new MeshDefinition();
            mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create()
                .texOffs(u, v)
                .addBox(-size * 0.5F, -size * 0.5F, 0.0F, size, size, 1.0F), PartPose.ZERO);
            return LayerDefinition.create(mesh, 128, 128);
        }
    }
}
