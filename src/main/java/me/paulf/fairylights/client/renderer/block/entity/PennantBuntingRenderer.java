package me.paulf.fairylights.client.renderer.block.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.client.FLModelLayers;
import me.paulf.fairylights.client.renderer.block.entity.state.connection.PennantBuntingConnectionRenderState;
import me.paulf.fairylights.server.connection.PennantBuntingConnection;
import me.paulf.fairylights.server.feature.Pennant;
import me.paulf.fairylights.server.item.FLItems;
import me.paulf.fairylights.util.Curve;
import me.paulf.fairylights.util.Utils;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.function.Function;

public class PennantBuntingRenderer extends ConnectionRenderer<PennantBuntingConnection, PennantBuntingConnectionRenderState> {
    public static final Identifier TRIANGLE_MODEL = FairyLights.id("entity/triangle_pennant");
    public static final Identifier SPEARHEAD_MODEL = FairyLights.id("entity/spearhead_pennant");
    public static final Identifier SWALLOWTAIL_MODEL = FairyLights.id("entity/swallowtail_pennant");
    public static final Identifier SQUARE_MODEL = FairyLights.id("entity/square_pennant");

    public static final ExtraModelKey<BlockStateModel> TRIANGLE_MODEL_KEY = ExtraModelKey.create();
    public static final ExtraModelKey<BlockStateModel> SPEARHEAD_MODEL_KEY = ExtraModelKey.create();
    public static final ExtraModelKey<BlockStateModel> SWALLOWTAIL_MODEL_KEY = ExtraModelKey.create();
    public static final ExtraModelKey<BlockStateModel> SQUARE_MODEL_KEY = ExtraModelKey.create();

    public static final ImmutableSet<Identifier> MODELS = ImmutableSet.of(TRIANGLE_MODEL, SPEARHEAD_MODEL, SWALLOWTAIL_MODEL, SQUARE_MODEL);

    private final ImmutableMap<Item, ExtraModelKey<BlockStateModel>> models = ImmutableMap.of(
        FLItems.TRIANGLE_PENNANT, TRIANGLE_MODEL_KEY,
        FLItems.SPEARHEAD_PENNANT, SPEARHEAD_MODEL_KEY,
        FLItems.SWALLOWTAIL_PENNANT, SWALLOWTAIL_MODEL_KEY,
        FLItems.SQUARE_PENNANT, SQUARE_MODEL_KEY
    );

    public PennantBuntingRenderer(final Function<ModelLayerLocation, ModelPart> baker) {
        super(baker, FLModelLayers.PENNANT_WIRE, 0.25F);
    }

    @Override
    public PennantBuntingConnectionRenderState createRenderState() {
        return new PennantBuntingConnectionRenderState();
    }

    @Override
    public void extractRenderState(PennantBuntingConnection conn, PennantBuntingConnectionRenderState state, float delta) {
        super.extractRenderState(conn, state, delta);
        for (Pennant pennant : conn.getFeatures()) {
            var pennantState = new PennantBuntingConnectionRenderState.PennantRenderState();
            Minecraft.getInstance().getModelManager().getModel(this.models.getOrDefault(pennant.getItem(), TRIANGLE_MODEL_KEY))
                .collectParts(RandomSource.create(), pennantState.model.setupModel(new Matrix4f(), false));

            pennantState.color = pennant.getColor();
            pennantState.point = pennant.getPoint(delta);
            pennantState.yaw = pennant.getYaw(delta);
            pennantState.pitch = pennant.getPitch(delta);
            pennantState.roll = pennant.getRoll(delta);
        }

        state.text = conn.getText();

        int length = state.text.getString().length();

        if (length > state.features.size()) {
            state.text = Utils.substr(state.text, state.features.size());
        }
    }

    @Override
    protected void submit(final PennantBuntingConnectionRenderState conn, final Curve catenary, final PoseStack matrix, final SubmitNodeCollector collector) {
        super.submit(conn, catenary, matrix, collector);
        int count = conn.features.size();
        if (count > 0) {
            final Font font = Minecraft.getInstance().font;

            int length = conn.text.getString().length();
            final int offset = (count - length) / 2;
            for (int i = 0; i < count; i++) {
                final var currPennant = conn.features.get(i);
                final int color = currPennant.color;
                final Vec3 pos = currPennant.point;
                matrix.pushPose();
                matrix.translate(pos.x, pos.y, pos.z);
                matrix.rotate(Axis.YP.rotation(-currPennant.yaw));
                matrix.rotate(Axis.ZP.rotation(currPennant.pitch));
                matrix.rotate(Axis.XP.rotation(currPennant.roll));
                matrix.pushPose();
                currPennant.model.tintLayers().add(color);
                currPennant.model.submit(matrix, collector, conn.lightCoords, conn.overlayCoords, 0);
                matrix.popPose();
                if (i >= offset && i < offset + length) {
                    this.drawLetter(matrix, collector, currPennant, conn.lightCoords, font, conn.text, i - offset, 1);
                    this.drawLetter(matrix, collector, currPennant, conn.lightCoords, font, conn.text, length - 1 - (i - offset), -1);
                }
                matrix.popPose();
            }
        }
    }

    private void drawLetter(final PoseStack matrix, final SubmitNodeCollector collector, final PennantBuntingConnectionRenderState.PennantRenderState pennant, final int packedLight, final Font font, final Component text, final int index, final int side) {
        final Style style = Utils.styleAt(text, index);
        final StringBuilder bob = new StringBuilder();
        if (style.isObfuscated()) bob.append(ChatFormatting.OBFUSCATED);
        if (style.isBold()) bob.append(ChatFormatting.BOLD);
        if (style.isStrikethrough()) bob.append(ChatFormatting.STRIKETHROUGH);
        if (style.isUnderlined()) bob.append(ChatFormatting.UNDERLINE);
        if (style.isItalic()) bob.append(ChatFormatting.ITALIC);
        bob.append(text.getString().charAt(index));
        final String chr = bob.toString();
        final Matrix3f m = new Matrix3f();
        m.rotate(Axis.YP.rotation(pennant.yaw));
        m.rotate(Axis.ZP.rotation(pennant.pitch));
        m.rotate(Axis.XP.rotation(pennant.roll));
        final Vector3f v = new Vector3f(0.0F, 0.0F, side);
        m.transform(v);
        // TODO: correct entity diffuse
        final float brightness = diffuseLight(v.x(), v.y(), v.z());
        final int styleColor = MoreObjects.firstNonNull(style.getColor().getValue(), 0xFFFFFF);
        final int r = (int) ((styleColor >> 16 & 0xFF) * brightness);
        final int g = (int) ((styleColor >> 8 & 0xFF) * brightness);
        final int b = (int) ((styleColor & 0xFF) * brightness);
        final int argb = 0xFF000000 | r << 16 | g << 8 | b;
        matrix.pushPose();
        matrix.translate(0.0F, -0.25F, 0.04F * side);
        final float s = 0.03075F;
        matrix.scale(s * side, -s, s);
        final float w = font.width(chr);
        collector.submitText(matrix, -(w - 1.0F) / 2.0F, -4.0F, Component.literal(chr).getVisualOrderText(), false, Font.DisplayMode.NORMAL, packedLight, argb, 0, 0);
        matrix.popPose();
    }

    public static float diffuseLight(float p_144949_, float p_144950_, float p_144951_) {
        return Math.min(p_144949_ * p_144949_ * 0.6F + p_144950_ * p_144950_ * ((3.0F + p_144950_) / 4.0F) + p_144951_ * p_144951_ * 0.8F, 1.0F);
    }


    public static LayerDefinition wireLayer() {
        return WireModel.createLayer(0, 17, 1);
    }
}
