package me.paulf.fairylights.mixin.fabric;

import com.google.common.collect.ImmutableMap;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.triggers.Criterion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Advancement.Builder.class)
public interface AdvancementBuilderAccessor {
    @Accessor
    ImmutableMap.Builder<String, Criterion<?>> getCriteria();
}
