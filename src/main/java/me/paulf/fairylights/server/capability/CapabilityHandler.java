package me.paulf.fairylights.server.capability;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.ServerProxy;
import me.paulf.fairylights.server.block.entity.FastenerBlockEntity;
import me.paulf.fairylights.server.entity.FenceFastenerEntity;
import me.paulf.fairylights.server.fastener.BlockFastener;
import me.paulf.fairylights.server.fastener.Fastener;
import me.paulf.fairylights.server.fastener.FenceFastener;
import me.paulf.fairylights.server.fastener.PlayerFastener;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.ladysnake.cca.api.v3.block.BlockComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.block.BlockComponentInitializer;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;

public final class CapabilityHandler implements BlockComponentInitializer, EntityComponentInitializer {
    public static final Identifier FASTENER_ID = FairyLights.id("fastener");
    public static final ComponentKey<Fastener<?>> FASTENER_CAP = ComponentRegistry.getOrCreate(FASTENER_ID, (Class<Fastener<?>>) (Object) Fastener.class);

    public static void register() {
    }

    @Override
    public void registerBlockComponentFactories(BlockComponentFactoryRegistry registry) {
        registry.registerFor(FastenerBlockEntity.class, FASTENER_CAP, blockEntity -> new BlockFastener(blockEntity, ServerProxy.buildBlockView()));
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(Player.class, FASTENER_CAP, PlayerFastener::new);
        registry.registerFor(FenceFastenerEntity.class, FASTENER_CAP, FenceFastener::new);
    }
}
