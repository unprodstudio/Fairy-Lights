package me.paulf.fairylights.client.renderer.block.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.client.FLModelLayers;
import me.paulf.fairylights.client.renderer.block.entity.state.connection.LetterBuntingConnectionRenderState;
import me.paulf.fairylights.server.connection.LetterBuntingConnection;
import me.paulf.fairylights.server.feature.Letter;
import me.paulf.fairylights.util.Curve;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Locale;
import java.util.function.Function;

public class LetterBuntingRenderer extends ConnectionRenderer<LetterBuntingConnection, LetterBuntingConnectionRenderState> {
    public static final Int2ObjectMap<Identifier> MODELS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ&!?".chars()
        .collect(
            Int2ObjectOpenHashMap::new,
            (map, cp) -> map.put(cp, FairyLights.id("entity/letter/" + Character.getName(cp).toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "_"))),
            Int2ObjectOpenHashMap::putAll
        );

    public static final Int2ObjectMap<ExtraModelKey<BlockStateModel>> MODEL_KEYS = new Int2ObjectOpenHashMap<>(Maps.transformValues(MODELS, _ -> ExtraModelKey.create()));

    public LetterBuntingRenderer(final Function<ModelLayerLocation, ModelPart> baker) {
        super(baker, FLModelLayers.LETTER_WIRE);
    }

    @Override
    public LetterBuntingConnectionRenderState createRenderState() {
        return new LetterBuntingConnectionRenderState();
    }

    @Override
    public void extractRenderState(LetterBuntingConnection conn, LetterBuntingConnectionRenderState state, float delta) {
        super.extractRenderState(conn, state, delta);

        var letters = conn.getLetters();
        if (letters == null)
            return;

        for (Letter letter : letters) {
            final ExtraModelKey<BlockStateModel> path = MODEL_KEYS.get(letter.getLetter());
            if (path == null)
                continue;

            final var letterState = new LetterBuntingConnectionRenderState.LetterRenderState();
            letterState.color = letter.getStyle().getColor().getValue();
            letterState.pos = letter.getPoint(delta);
            letterState.yaw = letter.getYaw(delta);
            letterState.pitch = letter.getPitch(delta);
            letterState.roll = letter.getRoll(delta);
            Minecraft.getInstance().getModelManager().getModel(path)
                .collectParts(RandomSource.create(), letterState.model.setupModel(new Matrix4f(), false));
        }
    }

    @Override
    protected void submit(final LetterBuntingConnectionRenderState conn, final Curve catenary, final PoseStack matrix, final SubmitNodeCollector collector) {
        super.submit(conn, catenary, matrix, collector);
        final int count = conn.letters.size();
        if (count == 0) {
            return;
        }
        for (final var letter : conn.letters) {
            final int color = ARGB.opaque(letter.color);
            final Vec3 pos = letter.pos;
            matrix.pushPose();
            matrix.translate(pos.x, pos.y, pos.z);
            matrix.rotate(Axis.YP.rotation(-letter.yaw));
            matrix.rotate(Axis.ZP.rotation(letter.pitch));
            matrix.rotate(Axis.XP.rotation(letter.roll));
            matrix.translate(-0.5F, -1.0F - 0.5F / 16.0F, -0.5F);
            letter.model.tintLayers().add(color);
            letter.model.submit(matrix, collector, conn.lightCoords, conn.overlayCoords, 0);
            matrix.popPose();
        }
    }

    public static LayerDefinition wireLayer() {
        return WireModel.createLayer(0, 17, 1);
    }
}
