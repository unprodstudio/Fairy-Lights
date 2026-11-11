package me.paulf.fairylights.data;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.client.color.*;
import me.paulf.fairylights.mixin.fabric.ShapedRecipeBuilderAccessor;
import me.paulf.fairylights.server.block.FLBlocks;
import me.paulf.fairylights.server.item.FLItems;
import me.paulf.fairylights.server.item.components.FLComponents;
import me.paulf.fairylights.server.item.crafting.FLCraftingRecipes;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class DataGatherer implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator gen) {
        var pack = gen.createPack();
        pack.addProvider(RecipeGenerator::new);
        pack.addProvider(BlockLootTableGenerator::new);
        pack.addProvider(ModelGenerator::new);
    }

    static class ModelGenerator extends FabricModelProvider {
        public ModelGenerator(FabricPackOutput output) {
            super(output);
        }

        @Override
        public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
        }

        @Override
        public void generateItemModels(ItemModelGenerators generator) {
            List.of(
                FLItems.FAIRY_LIGHT,
                FLItems.PAPER_LANTERN,
                FLItems.ORB_LANTERN,
                FLItems.FLOWER_LIGHT,
                FLItems.CANDLE_LANTERN_LIGHT,
                FLItems.OIL_LANTERN_LIGHT,
                FLItems.JACK_O_LANTERN,
                FLItems.SKULL_LIGHT,
                FLItems.GHOST_LIGHT,
                FLItems.SPIDER_LIGHT,
                FLItems.WITCH_LIGHT,
                FLItems.SNOWFLAKE_LIGHT,
                FLItems.HEART_LIGHT,
                FLItems.MOON_LIGHT,
                FLItems.STAR_LIGHT,
                FLItems.ICICLE_LIGHTS,
                FLItems.METEOR_LIGHT
            )
                .forEach(item -> {
                    item(generator, item, List.of(
                        new Constant(0xFFFFFFFF),
                        new ColorChangingItemTintSource()
                    ));
                });

            item(generator, FLItems.HANGING_LIGHTS, List.of(
                new StringTypeBasedItemTintSource(),
                new PatternOrDefaultItemTintSource(1),
                new PatternOrDefaultItemTintSource(2),
                new PatternOrDefaultItemTintSource(3)
            ));

            item(generator, FLItems.TINSEL, List.of(
                new Dye(0xFFFFFFFF)
            ));

            item(generator, FLItems.PENNANT_BUNTING, List.of(
                new Dye(0xFFFFFFFF),
                new PatternItemTintSource(1),
                new PatternItemTintSource(2),
                new PatternItemTintSource(3)
            ));

            List<ItemTintSource> secondLayerColor = List.of(new Constant(0xFFFFFFFF), new Dye(0xFFFFFFFF));

            item(generator, FLItems.TRIANGLE_PENNANT, secondLayerColor);
            item(generator, FLItems.SPEARHEAD_PENNANT, secondLayerColor);
            item(generator, FLItems.SWALLOWTAIL_PENNANT, secondLayerColor);
            item(generator, FLItems.SQUARE_PENNANT, secondLayerColor);

            item(generator, FLItems.LETTER_BUNTING, List.of(
                new Constant(0xFFFFFFFF),
                new StyledStringItemTintSource(1),
                new StyledStringItemTintSource(2),
                new StyledStringItemTintSource(3)
            ));
        }

        private static void item(ItemModelGenerators generator, Item item, List<ItemTintSource> tintSources) {
            generator.itemModelOutput.accept(item, new CuboidItemModelWrapper.Unbaked(
                ModelLocationUtils.getModelLocation(item),
                Optional.empty(),
                tintSources
            ));
        }
    }

    static class RecipeGenerator extends FabricRecipeProvider {
        public RecipeGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
            return new RecipeProvider(recipes, advancements) {
                private ShapedRecipeBuilder shaped(HolderGetter<Item> itemsRegistry, RecipeCategory category, ItemLike item, DataComponentPatch components) {
                    return shaped(itemsRegistry, category, item, 1, components);
                }

                private ShapedRecipeBuilder shaped(HolderGetter<Item> itemsRegistry, RecipeCategory category, ItemLike item, int count, DataComponentPatch components) {
                    var template = new ItemStackTemplate(item.asItem().builtInRegistryHolder(), count, components);
                    return ShapedRecipeBuilderAccessor.createShapedRecipeBuilder(itemsRegistry, category, template);
                }

                @Override
                public void buildRecipes() {
                    var components = DataComponentPatch.builder()
                        .set(FLComponents.STYLED_STRING, Component.empty())
                        .build();
                    var items = registries.lookupOrThrow(Registries.ITEM);
                    shaped(items, RecipeCategory.DECORATIONS, FLItems.LETTER_BUNTING, components)
                        .pattern("I-I")
                        .pattern("PBF")
                        .define('I', ConventionalItemTags.IRON_INGOTS)
                        .define('-', ConventionalItemTags.STRINGS)
                        .define('P', Items.PAPER)
                        .define('B', Items.INK_SAC)
                        .define('F', ConventionalItemTags.FEATHERS)
                        .unlockedBy("has_iron", has(ConventionalItemTags.IRON_INGOTS))
                        .unlockedBy("has_string", has(ConventionalItemTags.STRINGS))
                        .save(output);
                    ShapedRecipeBuilder.shaped(items, RecipeCategory.DECORATIONS,FLItems.GARLAND, 2)
                        .pattern("I-I")
                        .define('I', ConventionalItemTags.IRON_INGOTS)
                        .define('-', Items.VINE)
                        .unlockedBy("has_iron", has(ConventionalItemTags.IRON_INGOTS))
                        .unlockedBy("has_vine", has(Items.VINE))
                        .save(output);
                    ShapedRecipeBuilder.shaped(items, RecipeCategory.DECORATIONS,FLItems.OIL_LANTERN, 4)
                        .pattern(" I ")
                        .pattern("STS")
                        .pattern("IGI")
                        .define('I', ConventionalItemTags.IRON_INGOTS)
                        .define('S', Items.STICK)
                        .define('T', Items.TORCH)
                        .define('G', ConventionalItemTags.GLASS_PANES_COLORLESS)
                        .unlockedBy("has_iron", has(ConventionalItemTags.IRON_INGOTS))
                        .unlockedBy("has_torch", has(Items.TORCH))
                        .save(output);
                    ShapedRecipeBuilder.shaped(items, RecipeCategory.DECORATIONS,FLItems.CANDLE_LANTERN, 4)
                        .pattern(" I ")
                        .pattern("GTG")
                        .pattern("IGI")
                        .define('I', ConventionalItemTags.IRON_INGOTS)
                        .define('G', ConventionalItemTags.GOLD_NUGGETS)
                        .define('T', Items.TORCH)
                        .unlockedBy("has_iron", has(ConventionalItemTags.IRON_INGOTS))
                        .unlockedBy("has_torch", has(Items.TORCH))
                        .save(output);
                    ShapedRecipeBuilder.shaped(items, RecipeCategory.DECORATIONS,FLItems.INCANDESCENT_LIGHT, 4)
                        .pattern(" I ")
                        .pattern("ITI")
                        .pattern(" G ")
                        .define('I', ConventionalItemTags.IRON_INGOTS)
                        .define('G', ConventionalItemTags.GLASS_PANES_COLORLESS)
                        .define('T', Items.TORCH)
                        .unlockedBy("has_iron", has(ConventionalItemTags.IRON_INGOTS))
                        .unlockedBy("has_torch", has(Items.TORCH))
                        .save(output);
                    GenericRecipeBuilder.customRecipe(FLCraftingRecipes.HANGING_LIGHTS)
                        .unlockedBy("has_lights", has(FLCraftingRecipes.LIGHTS))
                        .build(output, registries, FairyLights.id("hanging_lights"));
                    GenericRecipeBuilder.customRecipe(FLCraftingRecipes.HANGING_LIGHTS_AUGMENTATION)
                        .build(output, registries, FairyLights.id("hanging_lights_augmentation"));
                    GenericRecipeBuilder.customRecipe(FLCraftingRecipes.TINSEL_GARLAND)
                        .unlockedBy("has_iron", has(ConventionalItemTags.IRON_INGOTS))
                        .unlockedBy("has_string", has(ConventionalItemTags.STRINGS))
                        .build(output, registries, FairyLights.id("tinsel_garland"));
                    GenericRecipeBuilder.customRecipe(FLCraftingRecipes.PENNANT_BUNTING)
                        .unlockedBy("has_pennants", has(FLCraftingRecipes.PENNANTS))
                        .build(output, registries, FairyLights.id("pennant_bunting"));
                    GenericRecipeBuilder.customRecipe(FLCraftingRecipes.PENNANT_BUNTING_AUGMENTATION)
                        .build(output, registries, FairyLights.id("pennant_bunting_augmentation"));
                    GenericRecipeBuilder.customRecipe(FLCraftingRecipes.EDIT_COLOR)
                        .build(output, registries, FairyLights.id("edit_color"));
                    GenericRecipeBuilder.customRecipe(FLCraftingRecipes.COPY_COLOR)
                        .build(output, registries, FairyLights.id("copy_color"));
                    this.pennantRecipe(FLCraftingRecipes.TRIANGLE_PENNANT)
                        .build(output, registries, FairyLights.id("triangle_pennant"));
                    this.pennantRecipe(FLCraftingRecipes.SPEARHEAD_PENNANT)
                        .build(output, registries, FairyLights.id("spearhead_pennant"));
                    this.pennantRecipe(FLCraftingRecipes.SWALLOWTAIL_PENNANT)
                        .build(output, registries, FairyLights.id("swallowtail_pennant"));
                    this.pennantRecipe(FLCraftingRecipes.SQUARE_PENNANT)
                        .build(output, registries, FairyLights.id("square_pennant"));
                    this.lightRecipe(FLCraftingRecipes.FAIRY_LIGHT)
                        .build(output, registries, FairyLights.id("fairy_light"));
                    this.lightRecipe(FLCraftingRecipes.PAPER_LANTERN)
                        .build(output, registries, FairyLights.id("paper_lantern"));
                    this.lightRecipe(FLCraftingRecipes.ORB_LANTERN)
                        .build(output, registries, FairyLights.id("orb_lantern"));
                    this.lightRecipe(FLCraftingRecipes.FLOWER_LIGHT)
                        .build(output, registries, FairyLights.id("flower_light"));
                    this.lightRecipe(FLCraftingRecipes.CANDLE_LANTERN_LIGHT)
                        .build(output, registries, FairyLights.id("candle_lantern_light"));
                    this.lightRecipe(FLCraftingRecipes.OIL_LANTERN_LIGHT)
                        .build(output, registries, FairyLights.id("oil_lantern_light"));
                    this.lightRecipe(FLCraftingRecipes.JACK_O_LANTERN)
                        .build(output, registries, FairyLights.id("jack_o_lantern"));
                    this.lightRecipe(FLCraftingRecipes.SKULL_LIGHT)
                        .build(output, registries, FairyLights.id("skull_light"));
                    this.lightRecipe(FLCraftingRecipes.GHOST_LIGHT)
                        .build(output, registries, FairyLights.id("ghost_light"));
                    this.lightRecipe(FLCraftingRecipes.SPIDER_LIGHT)
                        .build(output, registries, FairyLights.id("spider_light"));
                    this.lightRecipe(FLCraftingRecipes.WITCH_LIGHT)
                        .build(output, registries, FairyLights.id("witch_light"));
                    this.lightRecipe(FLCraftingRecipes.SNOWFLAKE_LIGHT)
                        .build(output, registries, FairyLights.id("snowflake_light"));
                    this.lightRecipe(FLCraftingRecipes.HEART_LIGHT)
                        .build(output, registries, FairyLights.id("heart_light"));
                    this.lightRecipe(FLCraftingRecipes.MOON_LIGHT)
                        .build(output, registries, FairyLights.id("moon_light"));
                    this.lightRecipe(FLCraftingRecipes.STAR_LIGHT)
                        .build(output, registries, FairyLights.id("star_light"));
                    this.lightRecipe(FLCraftingRecipes.ICICLE_LIGHTS)
                        .build(output, registries, FairyLights.id("icicle_lights"));
                    this.lightRecipe(FLCraftingRecipes.METEOR_LIGHT)
                        .build(output, registries, FairyLights.id("meteor_light"));
                    GenericRecipeBuilder.customRecipe(FLCraftingRecipes.LIGHT_TWINKLE)
                        .build(output, registries, FairyLights.id("light_twinkle"));
                    GenericRecipeBuilder.customRecipe(FLCraftingRecipes.COLOR_CHANGING_LIGHT)
                        .build(output, registries, FairyLights.id("color_changing_light"));
                }

                GenericRecipeBuilder lightRecipe(final RecipeSerializer<?> serializer) {
                    return GenericRecipeBuilder.customRecipe(serializer)
                        .unlockedBy("has_iron", has(ConventionalItemTags.IRON_INGOTS))
                        .unlockedBy("has_dye", has(ConventionalItemTags.DYES));
                }

                GenericRecipeBuilder pennantRecipe(final RecipeSerializer<?> serializer) {
                    return GenericRecipeBuilder.customRecipe(serializer)
                        .unlockedBy("has_paper", has(Items.PAPER))
                        .unlockedBy("has_string", has(ConventionalItemTags.STRINGS));
                }
            };
        }
    }

    static class BlockLootTableGenerator extends FabricBlockLootSubProvider
    {
        protected BlockLootTableGenerator(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture)
        {
            super(packOutput, registriesFuture);
        }

//        @Override
//        protected Iterable<Block> getKnownBlocks() {
//            return StreamSupport.stream(FLBlocks.REG.spliterator(), false).map(RegistrySupplier::get).collect(Collectors.toList());
//        }

        @Override
        public void generate()
        {
            this.add(FLBlocks.FASTENER, noDrop());
            this.add(FLBlocks.FAIRY_LIGHT, noDrop());
            this.add(FLBlocks.PAPER_LANTERN, noDrop());
            this.add(FLBlocks.ORB_LANTERN, noDrop());
            this.add(FLBlocks.FLOWER_LIGHT, noDrop());
            this.add(FLBlocks.CANDLE_LANTERN_LIGHT, noDrop());
            this.add(FLBlocks.OIL_LANTERN_LIGHT, noDrop());
            this.add(FLBlocks.JACK_O_LANTERN, noDrop());
            this.add(FLBlocks.SKULL_LIGHT, noDrop());
            this.add(FLBlocks.GHOST_LIGHT, noDrop());
            this.add(FLBlocks.SPIDER_LIGHT, noDrop());
            this.add(FLBlocks.WITCH_LIGHT, noDrop());
            this.add(FLBlocks.SNOWFLAKE_LIGHT, noDrop());
            this.add(FLBlocks.HEART_LIGHT, noDrop());
            this.add(FLBlocks.MOON_LIGHT, noDrop());
            this.add(FLBlocks.STAR_LIGHT, noDrop());
            this.add(FLBlocks.ICICLE_LIGHTS, noDrop());
            this.add(FLBlocks.METEOR_LIGHT, noDrop());
            this.add(FLBlocks.OIL_LANTERN, noDrop());
            this.add(FLBlocks.CANDLE_LANTERN, noDrop());
            this.add(FLBlocks.INCANDESCENT_LIGHT, noDrop());
        }
    }
}
