package me.paulf.fairylights.server.entity;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.paulf.fairylights.FairyLights;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class FLEntities {
    private FLEntities() {}

    public static final DeferredRegister<EntityType<?>> REG = DeferredRegister.create(FairyLights.ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<FenceFastenerEntity>> FASTENER = REG.register("fastener", () ->
        EntityType.Builder.<FenceFastenerEntity>of(FenceFastenerEntity::new, MobCategory.MISC)
            .sized(1.15F, 2.8F)
            .clientTrackingRange(10)
            .updateInterval(Integer.MAX_VALUE)
            .alwaysUpdateVelocity(false)
            /*
             * Because this entity is inside of a block when
             * EntityLivingBase#canEntityBeSeen performs its
             * raytracing it will always return false during
             * NetHandlerPlayServer#processUseEntity, making
             * the player reach distance be limited at three
             * blocks as opposed to the standard six blocks.
             * EntityLivingBase#canEntityBeSeen will add the
             * value given by getEyeHeight to the y position
             * of the entity to calculate the end point from
             * which to raytrace to. Returning one lets most
             * interactions with a player succeed, typically
             * for breaking the connection or creating a new
             * connection. I hope you enjoy my line lengths.
             */
            .eyeHeight(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, FairyLights.id("fastener")))
    );
}
