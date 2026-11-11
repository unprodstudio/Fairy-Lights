package me.paulf.fairylights.server.entity;

import me.paulf.fairylights.FairyLights;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Function;

public final class FLEntities {
    private FLEntities() {}

    public static final EntityType<FenceFastenerEntity> FASTENER = register("fastener", key ->
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
            .build(key)
    );

    public static <T extends Entity> EntityType<T> register(String name, Function<ResourceKey<EntityType<?>>, EntityType<T>> register) {
        var key = ResourceKey.create(Registries.ENTITY_TYPE, FairyLights.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, register.apply(key));
    }

    public static void register() {}
}
