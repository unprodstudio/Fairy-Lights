package me.paulf.fairylights.util.crafting;

import com.google.common.base.Strings;
import me.paulf.fairylights.util.crafting.ingredient.*;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public final class GenericRecipeBuilder {
    private static final char EMPTY_SPACE = ' ';

    private final Supplier<? extends RecipeSerializer<GenericRecipe>> serializer;

    private ItemStack output;

    private char outputChar = 0;

    private char[] shape = new char[0];

    private int width;

    private int height;

    private final Map<Character, Function<HolderLookup.Provider, RegularIngredient>> ingredients = new HashMap<>();

    private final List<Function<HolderLookup.Provider, AuxiliaryIngredient<?>>> auxiliaryIngredients = new ArrayList<>();

    public GenericRecipeBuilder(final Supplier<? extends RecipeSerializer<GenericRecipe>> serializer) {
        this(serializer, ItemStack.EMPTY);
    }

    public GenericRecipeBuilder(final Supplier<? extends RecipeSerializer<GenericRecipe>> serializer, final Item item) {
        this(serializer, new ItemStack(item));
    }

    public GenericRecipeBuilder(final Supplier<? extends RecipeSerializer<GenericRecipe>> serializer, final Block block) {
        this(serializer, new ItemStack(block));
    }

    public GenericRecipeBuilder(final Supplier<? extends RecipeSerializer<GenericRecipe>> serializer, final ItemStack output) {
        this.serializer = serializer;
        this.output = Objects.requireNonNull(output, "output");
    }

    public GenericRecipeBuilder withShape(final String... shape) {
        Objects.requireNonNull(shape, "shape");
        this.width = 0;
        this.height = 0;
        for (final String row : shape) {
            if (row != null && row.length() > this.width) {
                this.width = row.length();
            }
            this.height++;
        }
        final StringBuilder bob = new StringBuilder();
        for (final String row : shape) {
            bob.append(Strings.nullToEmpty(row));
            int trail = this.width - (row == null ? 0 : row.length());
            while (trail-- > 0) {
                bob.append(' ');
            }
        }
        this.shape = bob.toString().toCharArray();
        return this;
    }

    public GenericRecipeBuilder withOutput(final Item item) {
        return this.withOutput(Objects.requireNonNull(item, "item"), 1);
    }

    public GenericRecipeBuilder withOutput(final Item item, final int size) {
        return this.withOutput(new ItemStack(Objects.requireNonNull(item, "item"), size));
    }

    public GenericRecipeBuilder withOutput(final Block block) {
        return this.withOutput(Objects.requireNonNull(block, "block"), 1);
    }

    public GenericRecipeBuilder withOutput(final Block block, final int size) {
        return this.withOutput(new ItemStack(Objects.requireNonNull(block, "block"), size));
    }

    public GenericRecipeBuilder withOutput(final ItemStack output) {
        this.output = Objects.requireNonNull(output, "output");
        return this;
    }

    public GenericRecipeBuilder withOutput(final char key) {
        this.outputChar = key;
        return this;
    }

    public GenericRecipeBuilder withIngredient(final char key, final Item item) {
        return this.withIngredient(key, new ItemStack(Objects.requireNonNull(item, "item"), 1));
    }

    public GenericRecipeBuilder withIngredient(final char key, final Block block) {
        return this.withIngredient(key, new ItemStack(Objects.requireNonNull(block, "block"), 1));
    }

    public GenericRecipeBuilder withIngredient(final char key, final ItemStack stack) {
        return this.withIngredient(key, DefaultCustomIngredients.components(Objects.requireNonNull(stack, "stack")));
    }

    public GenericRecipeBuilder withIngredient(final char key, final Ingredient ingredient) {
        return this.withIngredient(key, new BasicRegularIngredient(ingredient));
    }

    public GenericRecipeBuilder withIngredient(final char key, final TagKey<Item> tag) {
        return this.withIngredient(key, provider -> new BasicRegularIngredient(Ingredient.of(provider.getOrThrow(tag))));
    }

    public GenericRecipeBuilder withIngredient(final char key, final RegularIngredient ingredient) {
        return this.withIngredient(key, _ -> ingredient);
    }

    public GenericRecipeBuilder withIngredient(final char key, final Function<HolderLookup.Provider, RegularIngredient> ingredient) {
        this.ingredients.put(key, Util.memoize(Objects.requireNonNull(ingredient, "ingredient")));
        return this;
    }

    public GenericRecipeBuilder withAuxiliaryIngredient(final Item item) {
        return this.withAuxiliaryIngredient(new ItemStack(Objects.requireNonNull(item, "item")));
    }

    public GenericRecipeBuilder withAuxiliaryIngredient(final Item item, final boolean isRequired, final int limit) {
        return this.withAuxiliaryIngredient(new ItemStack(Objects.requireNonNull(item, "item")), isRequired, limit);
    }

    public GenericRecipeBuilder withAuxiliaryIngredient(final Block block) {
        return this.withAuxiliaryIngredient(new ItemStack(Objects.requireNonNull(block, "block")));
    }

    public GenericRecipeBuilder withAuxiliaryIngredient(final Block block, final boolean isRequired, final int limit) {
        return this.withAuxiliaryIngredient(new ItemStack(Objects.requireNonNull(block, "block")), isRequired, limit);
    }

    public GenericRecipeBuilder withAuxiliaryIngredient(final ItemStack stack) {
        return this.withAuxiliaryIngredient(Objects.requireNonNull(stack, "stack"), false, 1);
    }

    public GenericRecipeBuilder withAuxiliaryIngredient(final ItemStack stack, final boolean isRequired, final int limit) {
        return this.withDirectAuxiliaryIngredient(provider -> new InertBasicAuxiliaryIngredient(DefaultCustomIngredients.components(Objects.requireNonNull(stack, "stack")), isRequired, limit));
    }

    public GenericRecipeBuilder withAuxiliaryIngredient(final TagKey<Item> tag) {
        return this.withAuxiliaryIngredient(tag, false, 1);
    }

    public GenericRecipeBuilder withAuxiliaryIngredient(final TagKey<Item> tag, final boolean isRequired, final int limit) {
        return this.withDirectAuxiliaryIngredient(provider -> new InertBasicAuxiliaryIngredient(Ingredient.of(provider.getOrThrow(tag)), isRequired, limit));
    }

    public GenericRecipeBuilder withAuxiliaryIngredient(final Function<HolderLookup.Provider, Ingredient> ingredient) {
        return this.withAuxiliaryIngredient(ingredient, false, 1);
    }

    public GenericRecipeBuilder withAuxiliaryIngredient(final Function<HolderLookup.Provider, Ingredient> ingredient, final boolean isRequired, final int limit) {
        return this.withDirectAuxiliaryIngredient(registry -> new InertBasicAuxiliaryIngredient(ingredient.apply(registry), isRequired, limit));
    }

    public GenericRecipeBuilder withAuxiliaryIngredient(AuxiliaryIngredient<?> ingredient) {
        return this.withDirectAuxiliaryIngredient(_ -> ingredient);
    }

    public GenericRecipeBuilder withDirectAuxiliaryIngredient(final Function<HolderLookup.Provider, AuxiliaryIngredient<?>> ingredient) {
        this.auxiliaryIngredients.add(Util.memoize(Objects.requireNonNull(ingredient, "ingredient")));
        return this;
    }

    public GenericRecipe build(HolderLookup.Provider registries) {
        final RegularIngredient[] ingredients = new RegularIngredient[this.width * this.height];
        int output = -1;
        for (int i = 0; i < this.shape.length; i++) {
            final char key = this.shape[i];
            var ingredient = this.ingredients.get(key).apply(registries);
            if (ingredient == null) {
                if (key != EMPTY_SPACE) {
                    throw new IllegalArgumentException("An ingredient is missing for the shape, \"" + key + "\"");
                }
                ingredient = GenericRecipe.EMPTY;
            }
            ingredients[i] = ingredient;
            if (output == -1 && key == this.outputChar) {
                output = i;
            }
        }
        final AuxiliaryIngredient<?>[] auxiliaryIngredients = this.auxiliaryIngredients
            .stream()
            .map(e -> e.apply(registries))
            .toArray(AuxiliaryIngredient<?>[]::new);
        return new GenericRecipe(this.serializer, this.output, ingredients, auxiliaryIngredients, this.width, this.height, output);
    }
}
