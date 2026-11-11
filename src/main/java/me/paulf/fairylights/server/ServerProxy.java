package me.paulf.fairylights.server;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.capability.CapabilityHandler;
import me.paulf.fairylights.server.config.FLConfig;
import me.paulf.fairylights.server.fastener.BlockView;
import me.paulf.fairylights.server.fastener.CreateBlockViewEvent;
import me.paulf.fairylights.server.fastener.RegularBlockView;
import me.paulf.fairylights.server.jingle.JingleManager;
import me.paulf.fairylights.server.net.Message;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.fml.config.ModConfig;

public class ServerProxy {
    public static final ServerEventHandler serverHandler = new ServerEventHandler();

    public void init() {
        ConfigRegistry.INSTANCE.register(FairyLights.ID, ModConfig.Type.COMMON, FLConfig.GENERAL_SPEC);
        ResourceManagerHelper.get(PackType.SERVER_DATA)
                .registerReloadListener(JingleManager.INSTANCE);
        this.setup();
    }

    private void setup() {
        CapabilityHandler.register();
    }

    public static void sendToPlayersWatchingChunk(final Message message, final Level world, final BlockPos pos) {
        for (ServerPlayer player : PlayerLookup.tracking((ServerLevel) world, pos)) {
            ServerPlayNetworking.send(player, message);
        }
    }

    public static void sendToPlayersWatchingEntity(final Message message, final Entity entity) {
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(player, message);
        }

        if (entity instanceof ServerPlayer player) {
            ServerPlayNetworking.send(player, message);
        }
    }

    public static BlockView buildBlockView() {
        final CreateBlockViewEvent evt = new CreateBlockViewEvent(new RegularBlockView());
        CreateBlockViewEvent.EVENT.invoker().onCreateBlockView(evt);
        return evt.getView();
    }

    public void initIntegration() {
		/*if (Loader.isModLoaded(ValkyrienWarfareMod.MODID)) {
			final Class<?> vw;
			try {
				vw = Class.forName("ValkyrienWarfare");
			} catch (final ClassNotFoundException e) {
				throw new AssertionError(e);
			}
			MinecraftForge.EVENT_BUS.register(vw);
		}*/
    }
}
