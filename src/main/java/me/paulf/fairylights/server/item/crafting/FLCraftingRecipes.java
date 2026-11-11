package me.paulf.fairylights.server.item.crafting;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.*;
import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.item.DyeableItem;
import me.paulf.fairylights.server.item.FLItems;
import me.paulf.fairylights.server.item.components.FLComponents;
import me.paulf.fairylights.server.item.components.ModifiableDataComponentMap;
import me.paulf.fairylights.server.string.StringTypes;
import me.paulf.fairylights.util.Blender;
import me.paulf.fairylights.util.OreDictUtils;
import me.paulf.fairylights.util.Utils;
import me.paulf.fairylights.util.crafting.GenericRecipe;
import me.paulf.fairylights.util.crafting.GenericRecipeBuilder;
import me.paulf.fairylights.util.crafting.ingredient.*;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public final class FLCraftingRecipes {
    private FLCraftingRecipes() {}

    public static final TagKey<Item> LIGHTS = TagKey.create(Registries.ITEM, FairyLights.id("lights"));

    public static final TagKey<Item> TWINKLING_LIGHTS = TagKey.create(Registries.ITEM, FairyLights.id("twinkling_lights"));

    public static final TagKey<Item> PENNANTS = TagKey.create(Registries.ITEM, FairyLights.id("pennants"));

    public static final TagKey<Item> DYEABLE = TagKey.create(Registries.ITEM, FairyLights.id("dyeable"));

    public static final TagKey<Item> DYEABLE_LIGHTS = TagKey.create(Registries.ITEM, FairyLights.id("dyeable_lights"));

    public static final Function<HolderLookup.Provider, RegularIngredient> DYE_SUBTYPE_INGREDIENT = registries -> new BasicRegularIngredient(Ingredient.of(registries.getOrThrow(ConventionalItemTags.DYES))) {
        @Override
        public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
            return DyeableItem.getDyeColor(output).map(dye -> ImmutableList.of(OreDictUtils.getDyes(dye))).orElse(ImmutableList.of());
        }

        @Override
        public boolean dictatesOutputType() {
            return true;
        }

        @Override
        public void matched(ItemStack ingredient, ModifiableDataComponentMap components) {
            components.set(FLComponents.COLOR, OreDictUtils.getDyeColor(ingredient).getTextureDiffuseColor());
        }
    };

    public static final RecipeSerializer<GenericRecipe> HANGING_LIGHTS = register("crafting_special_hanging_lights", FLCraftingRecipes::createHangingLights);

    public static final RecipeSerializer<GenericRecipe> HANGING_LIGHTS_AUGMENTATION = register("crafting_special_hanging_lights_augmentation", FLCraftingRecipes::createHangingLightsAugmentation);

    public static final RecipeSerializer<GenericRecipe> TINSEL_GARLAND = register("crafting_special_tinsel_garland", FLCraftingRecipes::createTinselGarland);

    public static final RecipeSerializer<GenericRecipe> PENNANT_BUNTING = register("crafting_special_pennant_bunting", FLCraftingRecipes::createPennantBunting);

    public static final RecipeSerializer<GenericRecipe> PENNANT_BUNTING_AUGMENTATION = register("crafting_special_pennant_bunting_augmentation", FLCraftingRecipes::createPennantBuntingAugmentation);

    public static final RecipeSerializer<GenericRecipe> TRIANGLE_PENNANT = register("crafting_special_triangle_pennant", FLCraftingRecipes::createTrianglePennant);

    public static final RecipeSerializer<GenericRecipe> SPEARHEAD_PENNANT = register("crafting_special_spearhead_pennant", FLCraftingRecipes::createSpearheadPennant);

    public static final RecipeSerializer<GenericRecipe> SWALLOWTAIL_PENNANT = register("crafting_special_swallowtail_pennant", FLCraftingRecipes::createSwallowtailPennant);

    public static final RecipeSerializer<GenericRecipe> SQUARE_PENNANT = register("crafting_special_square_pennant", FLCraftingRecipes::createSquarePennant);

    public static final RecipeSerializer<GenericRecipe> FAIRY_LIGHT = register("crafting_special_fairy_light", FLCraftingRecipes::createFairyLight);

    public static final RecipeSerializer<GenericRecipe> PAPER_LANTERN = register("crafting_special_paper_lantern", FLCraftingRecipes::createPaperLantern);

    public static final RecipeSerializer<GenericRecipe> ORB_LANTERN = register("crafting_special_orb_lantern", FLCraftingRecipes::createOrbLantern);

    public static final RecipeSerializer<GenericRecipe> FLOWER_LIGHT = register("crafting_special_flower_light", FLCraftingRecipes::createFlowerLight);

    public static final RecipeSerializer<GenericRecipe> CANDLE_LANTERN_LIGHT = register("crafting_special_candle_lantern_light", FLCraftingRecipes::createCandleLanternLight);

    public static final RecipeSerializer<GenericRecipe> OIL_LANTERN_LIGHT = register("crafting_special_oil_lantern_light", FLCraftingRecipes::createOilLanternLight);

    public static final RecipeSerializer<GenericRecipe> JACK_O_LANTERN = register("crafting_special_jack_o_lantern", FLCraftingRecipes::createJackOLantern);

    public static final RecipeSerializer<GenericRecipe> SKULL_LIGHT = register("crafting_special_skull_light", FLCraftingRecipes::createSkullLight);

    public static final RecipeSerializer<GenericRecipe> GHOST_LIGHT = register("crafting_special_ghost_light", FLCraftingRecipes::createGhostLight);

    public static final RecipeSerializer<GenericRecipe> SPIDER_LIGHT = register("crafting_special_spider_light", FLCraftingRecipes::createSpiderLight);

    public static final RecipeSerializer<GenericRecipe> WITCH_LIGHT = register("crafting_special_witch_light", FLCraftingRecipes::createWitchLight);

    public static final RecipeSerializer<GenericRecipe> SNOWFLAKE_LIGHT = register("crafting_special_snowflake_light", FLCraftingRecipes::createSnowflakeLight);

    public static final RecipeSerializer<GenericRecipe> HEART_LIGHT = register("crafting_special_heart_light", FLCraftingRecipes::createHeartLight);

    public static final RecipeSerializer<GenericRecipe> MOON_LIGHT = register("crafting_special_moon_light", FLCraftingRecipes::createMoonLight);

    public static final RecipeSerializer<GenericRecipe> STAR_LIGHT = register("crafting_special_star_light", FLCraftingRecipes::createStarLight);

    public static final RecipeSerializer<GenericRecipe> ICICLE_LIGHTS = register("crafting_special_icicle_lights", FLCraftingRecipes::createIcicleLights);

    public static final RecipeSerializer<GenericRecipe> METEOR_LIGHT = register("crafting_special_meteor_light", FLCraftingRecipes::createMeteorLight);

    public static final RecipeSerializer<GenericRecipe> LIGHT_TWINKLE = register("crafting_special_light_twinkle", FLCraftingRecipes::createLightTwinkle);

    public static final RecipeSerializer<GenericRecipe> COLOR_CHANGING_LIGHT = register("crafting_special_color_changing_light", FLCraftingRecipes::createColorChangingLight);

    public static final RecipeSerializer<GenericRecipe> EDIT_COLOR = register("crafting_special_edit_color", FLCraftingRecipes::createDyeColor);

    public static final RecipeSerializer<CopyColorRecipe> COPY_COLOR = register("crafting_special_copy_color", () -> new RecipeSerializer<>(CopyColorRecipe.CODEC, CopyColorRecipe.STREAM_CODEC));

    private static <T extends Recipe<?>> RecipeSerializer<T> register(String name, Supplier<RecipeSerializer<T>> register) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, FairyLights.id(name), register.get());
    }

    public static void register() {}

    private static RecipeSerializer<GenericRecipe> createDyeColor() {
        return createSerializer(new GenericRecipeBuilder(() -> EDIT_COLOR)
            .withShape("I")
            .withIngredient('I', DYEABLE).withOutput('I')
            .withDirectAuxiliaryIngredient(registries -> new BasicAuxiliaryIngredient<Blender>(Ingredient.of(registries.getOrThrow(ConventionalItemTags.DYES)), true, 8) {
                @Override
                public Blender accumulator() {
                    return new Blender();
                }

                @Override
                public void consume(final Blender data, final ItemStack ingredient) {
                    data.add(DyeableItem.getColor(OreDictUtils.getDyeColor(ingredient)));
                }

                @Override
                public boolean finish(Blender data, ModifiableDataComponentMap components) {
                    components.set(FLComponents.COLOR, data.blend());
                    return false;
                }
            }));
    }

    private static RecipeSerializer<GenericRecipe> createLightTwinkle() {
        return createSerializer(new GenericRecipeBuilder(() -> LIGHT_TWINKLE)
            .withShape("L")
            .withIngredient('L', TWINKLING_LIGHTS).withOutput('L')
            .withDirectAuxiliaryIngredient(registries -> new InertBasicAuxiliaryIngredient(Ingredient.of(registries.getOrThrow(ConventionalItemTags.GLOWSTONE_DUSTS)), true, 1) {
                @Override
                public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
                    return output.has(FLComponents.TWINKLES) && output.get(FLComponents.TWINKLES) ? super.getInput(output) : ImmutableList.of();
                }

                @Override
                public void present(final ModifiableDataComponentMap nbt) {
                    nbt.set(FLComponents.TWINKLES, true);
                }

                @Override
                public void absent(final ModifiableDataComponentMap nbt) {
                    nbt.set(FLComponents.TWINKLES, false);
                }

                @Override
                public void addTooltip(final List<Component> tooltip) {
                    super.addTooltip(tooltip);
                    tooltip.add(Utils.formatRecipeTooltip("recipe.fairylights.twinkling_lights.glowstone"));
                }
            }));
    }

    private static RecipeSerializer<GenericRecipe> createColorChangingLight() {
        return createSerializer(new GenericRecipeBuilder(() -> COLOR_CHANGING_LIGHT)
            .withShape("IG")
            .withIngredient('I', DYEABLE_LIGHTS).withOutput('I')
            .withIngredient('G', ConventionalItemTags.GOLD_NUGGETS)
            .withDirectAuxiliaryIngredient(registries -> new BasicAuxiliaryIngredient<ListTag>(Ingredient.of(registries.getOrThrow(ConventionalItemTags.DYES)), true, 8) {
                @Override
                public ListTag accumulator() {
                    return new ListTag();
                }

                @Override
                public void consume(final ListTag data, final ItemStack ingredient) {
                    data.add(IntTag.valueOf(DyeableItem.getColor(OreDictUtils.getDyeColor(ingredient))));
                }

                @Override
                public boolean finish(final ListTag data, final ModifiableDataComponentMap nbt) {
                    if (!data.isEmpty()) {
                        if (nbt.has(FLComponents.COLOR)) {
                            data.add(0, IntTag.valueOf(nbt.get(FLComponents.COLOR)));
                            nbt.remove(FLComponents.COLOR);
                        }
                        nbt.set(FLComponents.COLORS, data.stream().map(s -> ((IntTag) s).value()).toList());
                    }
                    return false;
                }
            }));
    }

    private static RecipeSerializer<GenericRecipe> createHangingLights() {
        return createSerializer(new GenericRecipeBuilder(() -> HANGING_LIGHTS, FLItems.HANGING_LIGHTS)
            .withShape("I-I")
            .withIngredient('I', ConventionalItemTags.IRON_INGOTS)
            .withIngredient('-', ConventionalItemTags.STRINGS)
            .withDirectAuxiliaryIngredient(registries -> new LightIngredient(registries, true))
            .withDirectAuxiliaryIngredient(registries -> new InertBasicAuxiliaryIngredient(Ingredient.of(registries.getOrThrow(ConventionalItemTags.WHITE_DYES)), false, 1) {
                @Override
                public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
                    return output.has(FLComponents.STRING_TYPE) && output.get(FLComponents.STRING_TYPE) == StringTypes.WHITE_STRING ? super.getInput(output) : ImmutableList.of();
                }

                @Override
                public void present(final ModifiableDataComponentMap nbt) {
                    nbt.set(FLComponents.STRING_TYPE, StringTypes.WHITE_STRING);
                }

                @Override
                public void absent(final ModifiableDataComponentMap nbt) {
                    nbt.set(FLComponents.STRING_TYPE, StringTypes.BLACK_STRING);
                }

                @Override
                public void addTooltip(final List<Component> tooltip) {
                    super.addTooltip(tooltip);
                    tooltip.add(Utils.formatRecipeTooltip("recipe.fairylights.hangingLights.string"));
                }
            }));
    }

    private static HolderLookup.Provider fromRegistryOps(RegistryOps<?> ops) {
        return new HolderLookup.Provider() {
            @Override
            public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
                return Stream.empty();
            }

            @Override
            public <T> Optional<? extends HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> key) {
                return ops.getter(key)
                    .map(registry -> new HolderLookup.RegistryLookup<T>() {
                        // we don't need *everything*
                        @Override
                        public ResourceKey<? extends Registry<? extends T>> key() {
                            return key;
                        }

                        @Override
                        public Lifecycle registryLifecycle() {
                            return Lifecycle.stable();
                        }

                        @Override
                        public Stream<Holder.Reference<T>> listElements() {
                            return Stream.empty();
                        }

                        @Override
                        public Stream<HolderSet.Named<T>> listTags() {
                            return Stream.empty();
                        }

                        @Override
                        public Optional<Holder.Reference<T>> get(ResourceKey<T> id) {
                            return registry.get(id);
                        }

                        @Override
                        public Optional<HolderSet.Named<T>> get(TagKey<T> id) {
                            return registry.get(id);
                        }
                    });
            }
        };
    }

    private static RecipeSerializer<GenericRecipe> createSerializer(GenericRecipeBuilder builder) {
        return new RecipeSerializer<>(new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.empty();
            }

            @Override
            public <T> DataResult<GenericRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
                if (ops instanceof RegistryOps<T> registryOps) {
                    return DataResult.success(builder.build(fromRegistryOps(registryOps)));
                }

                return DataResult.error(() -> "Was not decoded with registries!");
            }

            @Override
            public <T> RecordBuilder<T> encode(GenericRecipe input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return prefix;
            }
        }, new StreamCodec<>() {
            @Override
            public GenericRecipe decode(RegistryFriendlyByteBuf input) {
                return builder.build(input.registryAccess());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf output, GenericRecipe value) {
            }
        });
    }

    /*
     *  The JEI shown recipe is adding glowstone, eventually I should allow a recipe to provide a number of
     *  different recipe layouts the the input ingredients can be generated for so I could show applying a
     *  new light pattern as well.
     */
    private static RecipeSerializer<GenericRecipe> createHangingLightsAugmentation() {
        return createSerializer(new GenericRecipeBuilder(() -> HANGING_LIGHTS_AUGMENTATION, FLItems.HANGING_LIGHTS)
            .withShape("F")
            .withIngredient('F', new BasicRegularIngredient(Ingredient.of(FLItems.HANGING_LIGHTS)) {
                @Override
                public ImmutableList<ItemStack> getInputs() {
                    return this.ingredient.items()
                        .map(ItemStack::new)
                        .flatMap(stack -> {
                            return makeHangingLightsExamples(stack).stream();
                        }).collect(ImmutableList.toImmutableList());
                }

                @Override
                public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
                    final ItemStack stack = output.copy();
                    if (stack.getComponents().isEmpty()) {
                        return ImmutableList.of();
                    }
                    stack.setCount(1);
                    return ImmutableList.of(ImmutableList.of(stack));
                }

                @Override
                public void matched(final ItemStack ingredient, final ModifiableDataComponentMap nbt) {
                    if (ingredient.getComponents() != null) {
                        nbt.merge(ingredient.getComponents());
                    }
                }
            })
            .withDirectAuxiliaryIngredient(registries -> new LightIngredient(registries, true) {
                @Override
                public ImmutableList<ItemStack> getInputs() {
                    return ImmutableList.of();
                }

                @Override
                public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
                    return ImmutableList.of();
                }
            }));
    }

    private static ImmutableList<ItemStack> makeHangingLightsExamples(final ItemStack stack) {
        return ImmutableList.of(
            makeHangingLights(stack, DyeColor.CYAN, DyeColor.MAGENTA, DyeColor.CYAN, DyeColor.WHITE),
            makeHangingLights(stack, DyeColor.CYAN, DyeColor.LIGHT_BLUE, DyeColor.CYAN, DyeColor.LIGHT_BLUE),
            makeHangingLights(stack, DyeColor.LIGHT_GRAY, DyeColor.PINK, DyeColor.CYAN, DyeColor.GREEN),
            makeHangingLights(stack, DyeColor.LIGHT_GRAY, DyeColor.PURPLE, DyeColor.LIGHT_GRAY, DyeColor.GREEN),
            makeHangingLights(stack, DyeColor.CYAN, DyeColor.YELLOW, DyeColor.CYAN, DyeColor.PURPLE)
        );
    }

    public static ItemStack makeHangingLights(final ItemStack base, final DyeColor... colors) {
        final ItemStack stack = base.copy();
        final List<ItemStack> lights = new ArrayList<>();
        for (final DyeColor color : colors) {
            lights.add(DyeableItem.setColor(new ItemStack(FLItems.FAIRY_LIGHT), color));
        }
        stack.set(FLComponents.PATTERN, lights);
        stack.set(FLComponents.STRING_TYPE, StringTypes.BLACK_STRING);
        return stack;
    }

    private static RecipeSerializer<GenericRecipe> createTinselGarland() {
        return createSerializer(new GenericRecipeBuilder(() -> TINSEL_GARLAND, FLItems.TINSEL)
            .withShape(" P ", "I-I", " D ")
            .withIngredient('P', Items.PAPER)
            .withIngredient('I', ConventionalItemTags.IRON_INGOTS)
            .withIngredient('-', ConventionalItemTags.STRINGS)
            .withIngredient('D', DYE_SUBTYPE_INGREDIENT));
    }

    private static RecipeSerializer<GenericRecipe> createPennantBunting() {
        return createSerializer(new GenericRecipeBuilder(() -> PENNANT_BUNTING, FLItems.PENNANT_BUNTING)
            .withShape("I-I")
            .withIngredient('I', ConventionalItemTags.IRON_INGOTS)
            .withIngredient('-', ConventionalItemTags.STRINGS)
            .withDirectAuxiliaryIngredient(PennantIngredient::new));
    }

    private static RecipeSerializer<GenericRecipe> createPennantBuntingAugmentation() {
        return createSerializer(new GenericRecipeBuilder(() -> PENNANT_BUNTING_AUGMENTATION, FLItems.PENNANT_BUNTING)
            .withShape("B")
            .withIngredient('B', new BasicRegularIngredient(Ingredient.of(FLItems.PENNANT_BUNTING)) {
                @Override
                public ImmutableList<ItemStack> getInputs() {
                    return this.ingredient.items()
                        .map(ItemStack::new)
                        .flatMap(stack -> {
                            return makePennantExamples(stack).stream();
                        }).collect(ImmutableList.toImmutableList());
                }

                @Override
                public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
                    if (output.getComponents().isEmpty()) {
                        return ImmutableList.of();
                    }
                    return ImmutableList.of(makePennantExamples(output));
                }

                @Override
                public void matched(final ItemStack ingredient, final ModifiableDataComponentMap nbt) {
                    final DataComponentMap compound = ingredient.getComponents();
                    if (compound != null && !compound.isEmpty()) {
                        nbt.merge(compound);
                    }
                }
            })
            .withDirectAuxiliaryIngredient(PennantIngredient::new));
    }

    private static ImmutableList<ItemStack> makePennantExamples(final ItemStack stack) {
        return ImmutableList.of(
            makePennant(stack, DyeColor.BLUE, DyeColor.YELLOW, DyeColor.RED),
            makePennant(stack, DyeColor.PINK, DyeColor.LIGHT_BLUE),
            makePennant(stack, DyeColor.ORANGE, DyeColor.WHITE),
            makePennant(stack, DyeColor.LIME, DyeColor.YELLOW)
        );
    }

    public static ItemStack makePennant(final ItemStack base, final DyeColor... colors) {
        final ItemStack stack = base.copy();
        final List<ItemStack> pennants = new ArrayList<>();
        for (final DyeColor color : colors) {
            final ItemStack pennant = new ItemStack(FLItems.TRIANGLE_PENNANT);
            DyeableItem.setColor(pennant, color);
            pennants.add(pennant);
        }
        stack.set(FLComponents.PATTERN, pennants);
        stack.set(FLComponents.STYLED_STRING, Component.empty());
        return stack;
    }

    private static RecipeSerializer<GenericRecipe> createPennant(Supplier<RecipeSerializer<GenericRecipe>> serializer, final Item item, final String pattern) {
        return createSerializer(new GenericRecipeBuilder(serializer, item)
            .withShape("- -", "PDP", pattern)
            .withIngredient('P', Items.PAPER)
            .withIngredient('-', ConventionalItemTags.STRINGS)
            .withIngredient('D', DYE_SUBTYPE_INGREDIENT));
    }

    private static RecipeSerializer<GenericRecipe> createTrianglePennant() {
        return createPennant(() -> TRIANGLE_PENNANT, FLItems.TRIANGLE_PENNANT, " P ");
    }

    private static RecipeSerializer<GenericRecipe> createSpearheadPennant() {
        return createPennant(() -> SPEARHEAD_PENNANT, FLItems.SPEARHEAD_PENNANT, " PP");
    }

    private static RecipeSerializer<GenericRecipe> createSwallowtailPennant() {
        return createPennant(() -> SWALLOWTAIL_PENNANT, FLItems.SWALLOWTAIL_PENNANT, "P P");
    }

    private static RecipeSerializer<GenericRecipe> createSquarePennant() {
        return createPennant(() -> SQUARE_PENNANT, FLItems.SQUARE_PENNANT, "PPP");
    }

    private static RecipeSerializer<GenericRecipe> createFairyLight() {
        return createLight(() -> FAIRY_LIGHT, FLItems.FAIRY_LIGHT, b -> b
            .withShape(" I ", "IDI", " G ")
            .withIngredient('G', ConventionalItemTags.GLASS_PANES_COLORLESS)
        );
    }

    private static RecipeSerializer<GenericRecipe> createPaperLantern() {
        return createLight(() -> PAPER_LANTERN, FLItems.PAPER_LANTERN, b -> b
            .withShape(" I ", "PDP", "PPP")
            .withIngredient('P', Items.PAPER)
        );
    }

    private static RecipeSerializer<GenericRecipe> createOrbLantern() {
        return createLight(() -> ORB_LANTERN, FLItems.ORB_LANTERN, b -> b
            .withShape(" I ", "SDS", " W ")
            .withIngredient('S', ConventionalItemTags.STRINGS)
            .withIngredient('W', Items.WOOL.white())
        );
    }

    private static RecipeSerializer<GenericRecipe> createFlowerLight() {
        return createLight(() -> FLOWER_LIGHT, FLItems.FLOWER_LIGHT, b -> b
            .withShape(" I ", "RDB", " Y ")
            .withIngredient('R', Items.POPPY)
            .withIngredient('Y', Items.DANDELION)
            .withIngredient('B', Items.BLUE_ORCHID)
        );
    }

    private static RecipeSerializer<GenericRecipe> createCandleLanternLight() {
        return createLight(() -> CANDLE_LANTERN_LIGHT, FLItems.CANDLE_LANTERN_LIGHT, b -> b
            .withShape(" I ", "GDG", "IGI")
            .withIngredient('G', ConventionalItemTags.GOLD_NUGGETS)
        );
    }

    private static RecipeSerializer<GenericRecipe> createOilLanternLight() {
        return createLight(() -> OIL_LANTERN_LIGHT, FLItems.OIL_LANTERN_LIGHT, b -> b
            .withShape(" I ", "SDS", "IGI")
            .withIngredient('S', Items.STICK)
            .withIngredient('G', ConventionalItemTags.GLASS_PANES_COLORLESS)
        );
    }

    private static RecipeSerializer<GenericRecipe> createJackOLantern() {
        return createLight(() -> JACK_O_LANTERN, FLItems.JACK_O_LANTERN, b -> b
            .withShape(" I ", "SDS", "GPG")
            .withIngredient('S', ItemTags.WOODEN_SLABS)
            .withIngredient('G', Items.TORCH)
            .withIngredient('P', Items.JACK_O_LANTERN)
        );
    }

    private static RecipeSerializer<GenericRecipe> createSkullLight() {
        return createLight(() -> SKULL_LIGHT, FLItems.SKULL_LIGHT, b -> b
            .withShape(" I ", "IDI", " B ")
            .withIngredient('B', ConventionalItemTags.BONES)
        );
    }

    private static RecipeSerializer<GenericRecipe> createGhostLight() {
        return createLight(() -> GHOST_LIGHT, FLItems.GHOST_LIGHT, b -> b
            .withShape(" I ", "PDP", "IGI")
            .withIngredient('P', Items.PAPER)
            .withIngredient('G', Items.STAINED_GLASS_PANE.white())
        );
    }

    private static RecipeSerializer<GenericRecipe> createSpiderLight() {
        return createLight(() -> SPIDER_LIGHT, FLItems.SPIDER_LIGHT, b -> b
            .withShape(" I ", "WDW", "SES")
            .withIngredient('W', Items.COBWEB)
            .withIngredient('S', ConventionalItemTags.STRINGS)
            .withIngredient('E', Items.SPIDER_EYE)
        );
    }

    private static RecipeSerializer<GenericRecipe> createWitchLight() {
        return createLight(() -> WITCH_LIGHT, FLItems.WITCH_LIGHT, b -> b
            .withShape(" I ", "BDW", " S ")
            .withIngredient('B', Items.GLASS_BOTTLE)
            .withIngredient('W', Items.WHEAT)
            .withIngredient('S', Items.STICK)
        );
    }

    private static RecipeSerializer<GenericRecipe> createSnowflakeLight() {
        return createLight(() -> SNOWFLAKE_LIGHT, FLItems.SNOWFLAKE_LIGHT, b -> b
            .withShape(" I ", "SDS", " G ")
            .withIngredient('S', Items.SNOWBALL)
            .withIngredient('G', Items.STAINED_GLASS_PANE.white())
        );
    }

    private static RecipeSerializer<GenericRecipe> createHeartLight() {
        return createLight(() -> HEART_LIGHT, FLItems.HEART_LIGHT, b -> b
            .withShape(" I ", "IDI", " G ")
            .withIngredient('G', Items.STAINED_GLASS_PANE.red())
        );
    }

    private static RecipeSerializer<GenericRecipe> createMoonLight() {
        return createLight(() -> MOON_LIGHT, FLItems.MOON_LIGHT, b -> b
            .withShape(" I ", "GDG", " C ")
            .withIngredient('G', Items.STAINED_GLASS_PANE.white())
            .withIngredient('C', Items.CLOCK)
        );
    }


    private static RecipeSerializer<GenericRecipe> createStarLight() {
        return createLight(() -> STAR_LIGHT, FLItems.STAR_LIGHT, b -> b
            .withShape(" I ", "PDP", " G ")
            .withIngredient('P', Items.STAINED_GLASS_PANE.white())
            .withIngredient('G', ConventionalItemTags.GOLD_NUGGETS)
        );
    }

    private static RecipeSerializer<GenericRecipe> createIcicleLights() {
        return createLight(() -> ICICLE_LIGHTS, FLItems.ICICLE_LIGHTS, b -> b
            .withShape(" I ", "GDG", " B ")
            .withIngredient('G', ConventionalItemTags.GLASS_PANES_COLORLESS)
            .withIngredient('B', Items.WATER_BUCKET)
        );
    }

    private static RecipeSerializer<GenericRecipe> createMeteorLight() {
        return createLight(() -> METEOR_LIGHT, FLItems.METEOR_LIGHT, b -> b
            .withShape(" I ", "GDG", "IPI")
            .withIngredient('G', ConventionalItemTags.GLOWSTONE_DUSTS)
            .withIngredient('P', Items.PAPER)
        );
    }

    private static RecipeSerializer<GenericRecipe> createLight(Supplier<RecipeSerializer<GenericRecipe>> serializer, Item variant, final UnaryOperator<GenericRecipeBuilder> recipe) {
        return createSerializer(recipe.apply(new GenericRecipeBuilder(serializer))
            .withIngredient('I', ConventionalItemTags.IRON_INGOTS)
            .withIngredient('D', FLCraftingRecipes.DYE_SUBTYPE_INGREDIENT)
            .withOutput(variant, 4));
    }

    private static class LightIngredient extends BasicAuxiliaryIngredient<List<ItemStack>> {
        private LightIngredient(final HolderLookup.Provider registries, final boolean isRequired) {
            super(Ingredient.of(registries.getOrThrow(LIGHTS)), isRequired, 8);
        }

        @Override
        public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
            final DataComponentMap compound = output.getComponents();
            if (compound == null || compound.isEmpty()) {
                return ImmutableList.of();
            }
            final List<ItemStack> pattern = compound.get(FLComponents.PATTERN);
            if (pattern.isEmpty()) {
                return ImmutableList.of();
            }
            final ImmutableList.Builder<ImmutableList<ItemStack>> lights = ImmutableList.builder();
            for (int i = 0; i < pattern.size(); i++) {
                lights.add(ImmutableList.of(pattern.get(i)));
            }
            return lights.build();
        }

        @Override
        public boolean dictatesOutputType() {
            return true;
        }

        @Override
        public List<ItemStack> accumulator() {
            return new ArrayList<>();
        }

        @Override
        public void consume(final List<ItemStack> patternList, final ItemStack ingredient) {
            patternList.add(ingredient);
        }

        @Override
        public boolean finish(final List<ItemStack> pattern, final ModifiableDataComponentMap nbt) {
            if (!pattern.isEmpty()) {
                nbt.set(FLComponents.PATTERN, pattern);
            }
            return false;
        }

        @Override
        public void addTooltip(final List<Component> tooltip) {
            tooltip.add(Utils.formatRecipeTooltip("recipe.fairylights.hangingLights.light"));
        }
    }

    private static class PennantIngredient extends BasicAuxiliaryIngredient<List<ItemStack>> {
        private PennantIngredient(final HolderLookup.Provider registries) {
            super(Ingredient.of(registries.getOrThrow(PENNANTS)), true, 8);
        }

        @Override
        public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
            final DataComponentMap compound = output.getComponents();
            if (compound == null || compound.isEmpty()) {
                return ImmutableList.of();
            }
            final List<ItemStack> pattern = compound.get(FLComponents.PATTERN);
            if (pattern.isEmpty()) {
                return ImmutableList.of();
            }
            final ImmutableList.Builder<ImmutableList<ItemStack>> pennants = ImmutableList.builder();
            for (int i = 0; i < pattern.size(); i++) {
                pennants.add(ImmutableList.of(pattern.get(i)));
            }
            return pennants.build();
        }

        @Override
        public boolean dictatesOutputType() {
            return true;
        }

        @Override
        public List<ItemStack> accumulator() {
            return new ArrayList<>();
        }

        @Override
        public void consume(final List<ItemStack> patternList, final ItemStack ingredient) {
            patternList.add(ingredient);
        }

        @Override
        public boolean finish(final List<ItemStack> pattern, final ModifiableDataComponentMap nbt) {
            if (!pattern.isEmpty()) {
                nbt.set(FLComponents.PATTERN, pattern);
                nbt.set(FLComponents.STYLED_STRING, Component.empty());
            }
            return false;
        }

        @Override
        public void addTooltip(final List<Component> tooltip) {
            tooltip.add(Utils.formatRecipeTooltip("recipe.fairylights.pennantBunting.pennant"));
        }
    }
}
