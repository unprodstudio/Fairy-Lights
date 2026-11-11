package me.paulf.fairylights.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.paulf.fairylights.client.ClientProxy;
import me.paulf.fairylights.client.FLModelLayers;
import me.paulf.fairylights.client.model.light.BowModel;
import me.paulf.fairylights.client.renderer.block.entity.state.connection.*;
import me.paulf.fairylights.client.renderer.block.entity.state.FastenerRenderState;
import me.paulf.fairylights.server.connection.*;
import me.paulf.fairylights.server.fastener.Fastener;
import me.paulf.fairylights.server.fastener.FenceFastener;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;

public class FastenerRenderer {
    private final HangingLightsRenderer hangingLights;
    private final GarlandVineRenderer garland;
    private final GarlandTinselRenderer tinsel;
    private final PennantBuntingRenderer pennants;
    private final LetterBuntingRenderer letters;
    private final BowModel bow;

    public FastenerRenderer(final Function<ModelLayerLocation, ModelPart> baker) {
        this.hangingLights = new HangingLightsRenderer(baker);
        this.garland = new GarlandVineRenderer(baker);
        this.tinsel = new GarlandTinselRenderer(baker);
        this.pennants = new PennantBuntingRenderer(baker);
        this.letters = new LetterBuntingRenderer(baker);
        this.bow = new BowModel(baker.apply(FLModelLayers.BOW));
    }

    public void extractRenderState(Fastener<?> fastener, FastenerRenderState renderState, float delta, int lightCoords, int overlayCoords) {
        for (final Connection conn : fastener.getAllConnections()) {
            if (conn.getFastener() == fastener) {
                renderState.connections.add(this.extractConnection(conn, delta));
            }
        }

        renderState.isFenceFastener = fastener instanceof FenceFastener;
        renderState.facing = fastener.getFacing();

        if (fastener.getWorld() != null)
            renderState.state = fastener.getWorld().getBlockState(fastener.getPos());

        renderState.lightCoords = lightCoords;
        renderState.overlayCoords = overlayCoords;
    }

    public void submit(final FastenerRenderState fastener, final PoseStack matrix, final SubmitNodeCollector collector) {
        boolean renderBow = true;
        for (final ConnectionRenderState conn : fastener.connections) {
            this.submitConnection(conn, matrix, collector);
            if (renderBow && conn instanceof GarlandVineConnectionRenderState &&
                    this.submitBow(fastener, matrix, collector)) {
                renderBow = false;
            }
        }
    }

    private boolean submitBow(FastenerRenderState fastener, PoseStack matrix, SubmitNodeCollector collector) {
        if (fastener.isFenceFastener) {
            final BlockState state = fastener.state;
            if (!state.is(BlockTags.FENCES)) {
                return false;
            }
            final RenderType renderType = RenderTypes.entityCutout(ClientProxy.SOLID_TEXTURE);
            final float offset = -1.5F / 16.0F;
            final boolean north = state.getValue(FenceBlock.NORTH);
            final boolean east = state.getValue(FenceBlock.EAST);
            final boolean south = state.getValue(FenceBlock.SOUTH);
            final boolean west = state.getValue(FenceBlock.WEST);
            boolean tryDirX = true;
            boolean bow = false;
            if (!north && (east || west)) {
                this.bow(matrix, Direction.NORTH, offset, collector, renderType, fastener.lightCoords, fastener.overlayCoords);
                tryDirX = false;
                bow = true;
            }
            if (!south && (east || west)) {
                this.bow(matrix, Direction.SOUTH, offset, collector, renderType, fastener.lightCoords, fastener.overlayCoords);
                tryDirX = false;
                bow = true;
            }
            if (tryDirX) {
                if (!east && (north || south)) {
                    this.bow(matrix, Direction.EAST, offset, collector, renderType, fastener.lightCoords, fastener.overlayCoords);
                    bow = true;
                }
                if (!west && (north || south)) {
                    this.bow(matrix, Direction.WEST, offset, collector, renderType, fastener.lightCoords, fastener.overlayCoords);
                    bow = true;
                }
            }
            return bow;
        } else if (fastener.facing.getAxis() != Direction.Axis.Y) {
            this.bow(matrix, fastener.facing, 0.0F, collector, RenderTypes.entityCutout(ClientProxy.SOLID_TEXTURE), fastener.lightCoords, fastener.overlayCoords);
            return true;
        }
        return false;
    }

    private void bow(PoseStack matrix, Direction dir, float offset, SubmitNodeCollector collector, RenderType renderType, int packedLight, int packedOverlay) {
        matrix.pushPose();
        matrix.rotate(Axis.YP.rotationDegrees(180.0F - dir.toYRot()));
        if (offset != 0.0F) {
            matrix.translate(0.0D, 0.0D, offset);
        }
        collector.submitModel(this.bow, Unit.INSTANCE, matrix, renderType, packedLight, packedOverlay, 0);
        matrix.popPose();
    }

    private ConnectionRenderState extractConnection(Connection conn, float delta) {
        ConnectionRenderer<?, ?> renderer;
        switch (conn) {
            case HangingLightsConnection _ -> renderer = this.hangingLights;
            case GarlandVineConnection _ -> renderer = this.garland;
            case GarlandTinselConnection _ -> renderer = this.tinsel;
            case PennantBuntingConnection _ -> renderer = this.pennants;
            case LetterBuntingConnection _ -> renderer = this.letters;
            default -> {
                return null;
            }
        }

        var state = renderer.createRenderState();
        ((ConnectionRenderer<Connection, ConnectionRenderState>) renderer).extractRenderState(conn, state, delta);
        return state;
    }

    private void submitConnection(ConnectionRenderState state, PoseStack matrix, SubmitNodeCollector collector) {
        ConnectionRenderer<?, ?> renderer;
        switch (state) {
            case HangingLightsConnectionRenderState _ -> renderer = this.hangingLights;
            case GarlandVineConnectionRenderState _ -> renderer = this.garland;
            case GarlandTinselConnectionRenderState _ -> renderer = this.tinsel;
            case PennantBuntingConnectionRenderState _ -> renderer = this.pennants;
            case LetterBuntingConnectionRenderState _ -> renderer = this.letters;
            default -> {
                return;
            }
        }

        ((ConnectionRenderer<Connection, ConnectionRenderState>) renderer).submit(state, matrix, collector);
    }
}
