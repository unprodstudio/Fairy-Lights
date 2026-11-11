package me.paulf.fairylights.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.architectury.event.events.client.ClientGuiEvent;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.client.color.*;
import me.paulf.fairylights.client.command.JinglerCommand;
import me.paulf.fairylights.client.model.light.*;
import me.paulf.fairylights.client.renderer.FLRenderPipelines;
import me.paulf.fairylights.client.renderer.block.entity.*;
import me.paulf.fairylights.client.renderer.entity.FenceFastenerRenderer;
import me.paulf.fairylights.client.tutorial.ClippyController;
import me.paulf.fairylights.server.ServerProxy;
import me.paulf.fairylights.server.block.entity.FLBlockEntities;
import me.paulf.fairylights.server.entity.FLEntities;
import me.paulf.fairylights.server.feature.light.ColorChangingBehavior;
import me.paulf.fairylights.server.item.DyeableItem;
import me.paulf.fairylights.server.item.FLItems;
import me.paulf.fairylights.server.item.components.FLComponents;
import me.paulf.fairylights.server.net.clientbound.JingleMessage;
import me.paulf.fairylights.server.net.clientbound.OpenEditLetteredConnectionScreenMessage;
import me.paulf.fairylights.server.net.clientbound.UpdateEntityFastenerMessage;
import me.paulf.fairylights.server.string.StringTypes;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorResolverRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.config.ModConfig;

public final class ClientProxy extends ServerProxy {
    public static final Identifier SOLID_TEXTURE = FairyLights.id("entity/connections");
    public static final Identifier TRANSLUCENT_TEXTURE = FairyLights.id("entity/connections");

    @Override
    public void init() {
        ClientPlayNetworking.registerGlobalReceiver(JingleMessage.TYPE, new JingleMessage.Handler());
        ClientPlayNetworking.registerGlobalReceiver(OpenEditLetteredConnectionScreenMessage.TYPE, new OpenEditLetteredConnectionScreenMessage.Handler());
        ClientPlayNetworking.registerGlobalReceiver(UpdateEntityFastenerMessage.TYPE, new UpdateEntityFastenerMessage.Handler());

        super.init();
        new ClippyController().init();
        ConfigRegistry.INSTANCE.register(FairyLights.ID, ModConfig.Type.CLIENT, FLClientConfig.SPEC);
        ClientEventHandler clientEventHandler = new ClientEventHandler();
        ClientGuiEvent.RENDER_HUD.register((guiGraphics, deltaTracker) -> {
            var mc = Minecraft.getInstance();
            clientEventHandler.renderOverlay(mc.gui, guiGraphics, deltaTracker.getGameTimeDeltaPartialTick(true), mc.getWindow().getScreenWidth(), mc.getWindow().getScreenHeight());
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            JinglerCommand.register(dispatcher);
        });
        JinglerCommand.register();
        FLRenderPipelines.init();
 
        setup();
        setupLayerDefinitions();
        setupColors();
        setupModels();
    }

    /*private int getUvIndex(VertexFormat vertexFormat) {
        int position = 0;
        for (final VertexFormatElement ee : vertexFormat.getElements()) {
            if (ee.getUsage() == VertexFormatElement.Usage.UV) {
                if (position % 4 == 0) {
                    return position / 4;
                }
                break;
            }
            position += ee.getByteSize();
        }
        return -1;
    }*/

    private void setup() {
        BlockEntityRenderers.register(FLBlockEntities.FASTENER.get(), context -> new FastenerBlockEntityRenderer(context, ServerProxy.buildBlockView()));
        BlockEntityRenderers.register(FLBlockEntities.LIGHT.get(), LightBlockEntityRenderer::new);
        EntityRenderers.register(FLEntities.FASTENER.get(), FenceFastenerRenderer::new);
        /*final LightRenderer r = new LightRenderer();
        final StringBuilder bob = new StringBuilder();
        FLItems.lights().forEach(l -> {
            final LightModel<?> model = r.getModel(l.getBlock().getVariant(), -1);
            final AxisAlignedBB bb = model.getBounds();
            bob.append(String.format("%n%s new AxisAlignedBB(%.3fD, %.3fD, %.3fD, %.3fD, %.3fD, %.3fD), %.3fD", l.getRegistryName(), bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, model.getFloorOffset()));
        });
        LogManager.getLogger().debug("waldo {}", bob);*/
    }

    private void setupLayerDefinitions() {
        ModelLayerRegistry.registerModelLayer(FLModelLayers.BOW, BowModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.GARLAND_RINGS, GarlandVineRenderer.RingsModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.TINSEL_STRIP, GarlandTinselRenderer.StripModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.FAIRY_LIGHT, FairyLightModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.PAPER_LANTERN, PaperLanternModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.ORB_LANTERN, OrbLanternModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.FLOWER_LIGHT, FlowerLightModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.CANDLE_LANTERN_LIGHT, ColorCandleLanternModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.OIL_LANTERN_LIGHT, ColorOilLanternModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.JACK_O_LANTERN, JackOLanternLightModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.SKULL_LIGHT, SkullLightModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.GHOST_LIGHT, GhostLightModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.SPIDER_LIGHT, SpiderLightModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.WITCH_LIGHT, WitchLightModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.SNOWFLAKE_LIGHT, SnowflakeLightModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.HEART_LIGHT, HeartLightModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.MOON_LIGHT, MoonLightModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.STAR_LIGHT, StarLightModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.ICICLE_LIGHTS_1, () -> IcicleLightsModel.createLayer(1));
        ModelLayerRegistry.registerModelLayer(FLModelLayers.ICICLE_LIGHTS_2, () -> IcicleLightsModel.createLayer(2));
        ModelLayerRegistry.registerModelLayer(FLModelLayers.ICICLE_LIGHTS_3, () -> IcicleLightsModel.createLayer(3));
        ModelLayerRegistry.registerModelLayer(FLModelLayers.ICICLE_LIGHTS_4, () -> IcicleLightsModel.createLayer(4));
        ModelLayerRegistry.registerModelLayer(FLModelLayers.METEOR_LIGHT, MeteorLightModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.OIL_LANTERN, OilLanternModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.CANDLE_LANTERN, CandleLanternModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.INCANDESCENT_LIGHT, IncandescentLightModel::createLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.LETTER_WIRE, LetterBuntingRenderer::wireLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.PENNANT_WIRE, PennantBuntingRenderer::wireLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.TINSEL_WIRE, GarlandTinselRenderer::wireLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.VINE_WIRE, GarlandVineRenderer::wireLayer);
        ModelLayerRegistry.registerModelLayer(FLModelLayers.LIGHTS_WIRE, HangingLightsRenderer::wireLayer);
    }

    private void setupModels() {
        ModelLoadingPlugin.register(context -> {
            context.addModel(FenceFastenerRenderer.MODEL_KEY, SimpleUnbakedExtraModel.blockStateModel(FenceFastenerRenderer.MODEL));

            context.addModel(PennantBuntingRenderer.TRIANGLE_MODEL_KEY, SimpleUnbakedExtraModel.blockStateModel(PennantBuntingRenderer.TRIANGLE_MODEL));
            context.addModel(PennantBuntingRenderer.SPEARHEAD_MODEL_KEY, SimpleUnbakedExtraModel.blockStateModel(PennantBuntingRenderer.SPEARHEAD_MODEL));
            context.addModel(PennantBuntingRenderer.SWALLOWTAIL_MODEL_KEY, SimpleUnbakedExtraModel.blockStateModel(PennantBuntingRenderer.SWALLOWTAIL_MODEL));
            context.addModel(PennantBuntingRenderer.SQUARE_MODEL_KEY, SimpleUnbakedExtraModel.blockStateModel(PennantBuntingRenderer.SQUARE_MODEL));

            LetterBuntingRenderer.MODEL_KEYS.forEach((k, key) -> {
                Identifier id = LetterBuntingRenderer.MODELS.get(k);
                context.addModel(key, SimpleUnbakedExtraModel.blockStateModel(id));
            });
        });
    }

    private void setupColors() {
        ItemTintSources.ID_MAPPER.put(FairyLights.id("color_changing"), ColorChangingItemTintSource.CODEC);
        ItemTintSources.ID_MAPPER.put(FairyLights.id("string_type"), StringTypeBasedItemTintSource.CODEC);
        ItemTintSources.ID_MAPPER.put(FairyLights.id("pattern_or_default"), PatternOrDefaultItemTintSource.CODEC);
        ItemTintSources.ID_MAPPER.put(FairyLights.id("pattern"), PatternItemTintSource.CODEC);
        ItemTintSources.ID_MAPPER.put(FairyLights.id("styled_string"), StyledStringItemTintSource.CODEC);
    }
}
