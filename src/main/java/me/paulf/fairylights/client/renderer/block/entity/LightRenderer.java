package me.paulf.fairylights.client.renderer.block.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.paulf.fairylights.client.ClientProxy;
import me.paulf.fairylights.client.FLModelLayers;
import me.paulf.fairylights.client.model.light.*;
import me.paulf.fairylights.client.renderer.block.entity.state.LightRenderState;
import me.paulf.fairylights.server.feature.light.LightBehavior;
import me.paulf.fairylights.server.item.LightVariant;
import me.paulf.fairylights.server.item.SimpleLightVariant;
import me.paulf.fairylights.util.FLMth;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class LightRenderer {
    static class DefaultModel extends LightModel<LightBehavior> {
        private static final ModelPart EMPTY = new ModelPart(List.of(), Map.of());

        public DefaultModel() {
            super(new ModelPart(List.of(), Map.of(
                "lit", EMPTY,
                "lit_tint", EMPTY,
                "lit_tint_glow", EMPTY,
                "unlit", EMPTY
            )));
        }

        @Override
        public void renderToBuffer(final PoseStack matrix, final VertexConsumer builder, final int light, final int overlay, final int color) {
        }
    }

    private final LightModelProvider<LightBehavior> defaultLight = LightModelProvider.of(new DefaultModel());

    private final Map<LightVariant<?>, LightModelProvider<?>> lights;

    public LightRenderer(final Function<ModelLayerLocation, ModelPart> baker) {
        lights = new ImmutableMap.Builder<LightVariant<?>, LightModelProvider<?>>()
            .put(SimpleLightVariant.FAIRY_LIGHT, LightModelProvider.of(new FairyLightModel(baker.apply(FLModelLayers.FAIRY_LIGHT))))
            .put(SimpleLightVariant.PAPER_LANTERN, LightModelProvider.of(new PaperLanternModel(baker.apply(FLModelLayers.PAPER_LANTERN))))
            .put(SimpleLightVariant.ORB_LANTERN, LightModelProvider.of(new OrbLanternModel(baker.apply(FLModelLayers.ORB_LANTERN))))
            .put(SimpleLightVariant.FLOWER_LIGHT, LightModelProvider.of(new FlowerLightModel(baker.apply(FLModelLayers.FLOWER_LIGHT))))
            .put(SimpleLightVariant.CANDLE_LANTERN_LIGHT, LightModelProvider.of(new ColorCandleLanternModel(baker.apply(FLModelLayers.CANDLE_LANTERN_LIGHT))))
            .put(SimpleLightVariant.OIL_LANTERN_LIGHT, LightModelProvider.of(new ColorOilLanternModel(baker.apply(FLModelLayers.OIL_LANTERN_LIGHT))))
            .put(SimpleLightVariant.JACK_O_LANTERN, LightModelProvider.of(new JackOLanternLightModel(baker.apply(FLModelLayers.JACK_O_LANTERN))))
            .put(SimpleLightVariant.SKULL_LIGHT, LightModelProvider.of(new SkullLightModel(baker.apply(FLModelLayers.SKULL_LIGHT))))
            .put(SimpleLightVariant.GHOST_LIGHT, LightModelProvider.of(new GhostLightModel(baker.apply(FLModelLayers.GHOST_LIGHT))))
            .put(SimpleLightVariant.SPIDER_LIGHT, LightModelProvider.of(new SpiderLightModel(baker.apply(FLModelLayers.SPIDER_LIGHT))))
            .put(SimpleLightVariant.WITCH_LIGHT, LightModelProvider.of(new WitchLightModel(baker.apply(FLModelLayers.WITCH_LIGHT))))
            .put(SimpleLightVariant.SNOWFLAKE_LIGHT, LightModelProvider.of(new SnowflakeLightModel(baker.apply(FLModelLayers.SNOWFLAKE_LIGHT))))
            .put(SimpleLightVariant.HEART_LIGHT, LightModelProvider.of(new HeartLightModel(baker.apply(FLModelLayers.HEART_LIGHT))))
            .put(SimpleLightVariant.MOON_LIGHT, LightModelProvider.of(new MoonLightModel(baker.apply(FLModelLayers.MOON_LIGHT))))
            .put(SimpleLightVariant.STAR_LIGHT, LightModelProvider.of(new StarLightModel(baker.apply(FLModelLayers.STAR_LIGHT))))
            .put(SimpleLightVariant.ICICLE_LIGHTS, LightModelProvider.of(
                new IcicleLightsModel[] {
                    new IcicleLightsModel(baker.apply(FLModelLayers.ICICLE_LIGHTS_1), 1),
                    new IcicleLightsModel(baker.apply(FLModelLayers.ICICLE_LIGHTS_2), 2),
                    new IcicleLightsModel(baker.apply(FLModelLayers.ICICLE_LIGHTS_3), 3),
                    new IcicleLightsModel(baker.apply(FLModelLayers.ICICLE_LIGHTS_4), 4)
                },
                (models, i) -> models[i < 0 ? 3 : FLMth.mod(FLMth.hash(i), 4)]
            ))
            .put(SimpleLightVariant.METEOR_LIGHT, LightModelProvider.of(new MeteorLightModel(baker.apply(FLModelLayers.METEOR_LIGHT))))
            .put(SimpleLightVariant.OIL_LANTERN, LightModelProvider.of(new OilLanternModel(baker.apply(FLModelLayers.OIL_LANTERN))))
            .put(SimpleLightVariant.CANDLE_LANTERN, LightModelProvider.of(new CandleLanternModel(baker.apply(FLModelLayers.CANDLE_LANTERN))))
            .put(SimpleLightVariant.INCANDESCENT_LIGHT, LightModelProvider.of(new IncandescentLightModel(baker.apply(FLModelLayers.INCANDESCENT_LIGHT))))
            .build();
    }

    public <T extends LightBehavior> LightModel<T> getModel(final LightRenderState light, final int index) {
        return this.getModel(light.variant, index);
    }

    @SuppressWarnings("unchecked")
    public <T extends LightBehavior> LightModel<T> getModel(final LightVariant<?> variant, final int index) {
        return (LightModel<T>) this.lights.getOrDefault(variant, this.defaultLight).get(index);
    }

    public void submit(final PoseStack matrix, final SubmitNodeCollector collector, final LightRenderState light, final int index, final int packedLight, final int packedOverlay) {
        this.submit(matrix, collector, light, this.getModel(light, index), packedLight, packedOverlay);
    }

    public <T extends LightBehavior> void submit(final PoseStack matrix, final SubmitNodeCollector collector, final LightRenderState light, final LightModel<T> model, final int packedLight, final int packedOverlay) {
        var renderType = RenderTypes.entityTranslucent(ClientProxy.TRANSLUCENT_TEXTURE);
        collector.submitModel(model, light, matrix, renderType, packedLight, packedOverlay, -1, null, 0);

        collector.submitCustomGeometry(matrix, renderType, (pose, buf) -> {
            ForwardingVertexConsumer translucent = new ForwardingVertexConsumer() {
                @Override
                protected VertexConsumer delegate() {
                    return buf;
                }

                @Override
                public VertexConsumer setNormal(float x, float y, float z) {
                    return super.setNormal(0.0F, 1.0F, 0.0F);
                }
            };

            var mat = new PoseStack();
            mat.last().pose().set(pose.pose());
            mat.last().normal().set(pose.normal());
            model.renderTranslucent(matrix, translucent, packedLight, packedOverlay, -1);
        });
    }

    interface LightModelProvider<T extends LightBehavior> {
        LightModel<T> get(final int index);

        static <T extends LightBehavior> LightModelProvider<T> of(final LightModel<T> model) {
            return i -> model;
        }

        static <T extends LightBehavior> LightModelProvider<T> of(final Supplier<LightModel<T>> model) {
            return i -> model.get();
        }

        static <T extends LightBehavior, D> LightModelProvider<T> of(final D data, final BiFunction<? super D, Integer, LightModel<T>> function) {
            return i -> function.apply(data, i);
        }
    }

}
