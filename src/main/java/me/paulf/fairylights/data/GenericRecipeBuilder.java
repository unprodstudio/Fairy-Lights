package me.paulf.fairylights.data;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.mixin.fabric.AdvancementBuilderAccessor;
import net.minecraft.advancements.*;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.RecipeUnlockedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.NullOps;
import net.minecraft.util.Unit;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.Objects;
import java.util.Optional;

public class GenericRecipeBuilder {
    private final RecipeSerializer<?> serializer;

    private final Advancement.Builder advancementBuilder = Advancement.Builder.advancement();

    public GenericRecipeBuilder(final RecipeSerializer<?> serializer) {
        this.serializer = Objects.requireNonNull(serializer, "serializer");
    }

    public GenericRecipeBuilder unlockedBy(final String name, final Criterion<?> criterion) {
        this.advancementBuilder.addCriterion(name, criterion);
        return this;
    }

    public void build(final RecipeOutput consumer, final HolderLookup.Provider registries, final Identifier id) {
        var recipeKey = ResourceKey.create(Registries.RECIPE, id);
        final AdvancementHolder advancementHolder;
        final Identifier advancementId;
        if (((AdvancementBuilderAccessor) this.advancementBuilder).getCriteria().build().isEmpty()) {
            advancementHolder = null;
        } else {
            advancementId = Identifier.fromNamespaceAndPath(id.getNamespace(), "recipes/" + FairyLights.ID + "/" + id.getPath());
            advancementHolder = consumer.advancement()
                .parent(Identifier.withDefaultNamespace("recipes/root"))
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(consumer.lookup(Registries.RECIPE).getOrThrow(recipeKey)))
                .rewards(AdvancementRewards.Builder.recipe(recipeKey))
                .requirements(AdvancementRequirements.Strategy.OR)
                .build(advancementId);
        }

        consumer.accept(recipeKey, this.serializer.codec().codec().decode(RegistryOps.create(NullOps.INSTANCE, registries), Unit.INSTANCE).getOrThrow().getFirst(), advancementHolder);
    }

    public static GenericRecipeBuilder customRecipe(final RecipeSerializer<?> serializer) {
        return new GenericRecipeBuilder(serializer);
    }
}
