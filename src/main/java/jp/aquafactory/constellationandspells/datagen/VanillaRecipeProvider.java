package jp.aquafactory.constellationandspells.datagen;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.recipe.ResetImbueSmithingRecipe;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import jp.aquafactory.constellationandspells.registry.SchoolTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.List;

import static io.redspace.ironsspellbooks.registries.ItemRegistry.ARCANE_ESSENCE;

public final class VanillaRecipeProvider extends RecipeProvider {
    public VanillaRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        ShapedRecipeBuilder
                .shaped(RecipeCategory.MISC, ItemRegistry.ZODIAC_RUNE.get())
                .pattern("FFF")
                .pattern("FRF")
                .pattern("FFF")
                .define('F', SchoolTags.ZODIAC_FOCUS)
                .define('R', io.redspace.ironsspellbooks.registries.ItemRegistry.BLANK_RUNE.get())
                .unlockedBy("has_blank_rune", has(io.redspace.ironsspellbooks.registries.ItemRegistry.BLANK_RUNE.get()))
                .save(output);

        ShapedRecipeBuilder
                .shaped(RecipeCategory.MISC, ItemRegistry.ZODIAC_UPGRADE_ORB.get())
                .pattern("RRR")
                .pattern("ROR")
                .pattern("RRR")
                .define('R', ItemRegistry.ZODIAC_RUNE.get())
                .define('O', io.redspace.ironsspellbooks.registries.ItemRegistry.UPGRADE_ORB.get())
                .unlockedBy("has_upgrade_orb", has(io.redspace.ironsspellbooks.registries.ItemRegistry.UPGRADE_ORB.get()))
                .save(output);

        ShapedRecipeBuilder
                .shaped(RecipeCategory.COMBAT, ItemRegistry.CELESTIAL_SURVEY_STAFF.get())
                .pattern(" CS")
                .pattern(" MG")
                .pattern("MO ")
                .define('S', ItemRegistry.STARDUST_SHARD.get())
                .define('C', Items.COMPASS)
                .define('M', Items.CALCITE)
                .define('G', Items.GOLD_INGOT)
                .define('O', Items.OBSIDIAN)
                .unlockedBy("has_stardust_shard", has(ItemRegistry.STARDUST_SHARD.get()))
                .save(output);

        ShapedRecipeBuilder
                .shaped(RecipeCategory.COMBAT, ItemRegistry.CONSTELLATION_TABLET.get())
                .pattern("GWG")
                .pattern("SRS")
                .pattern("GMG")
                .define('R', ItemRegistry.ZODIAC_RUNE.get())
                .define('S', ItemRegistry.STARDUST_SHARD.get())
                .define('M', io.redspace.ironsspellbooks.registries.ItemRegistry.MITHRIL_SCRAP.get())
                .define('W', Items.MAP)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_zodiac_rune", has(ItemRegistry.ZODIAC_RUNE.get()))
                .save(output);

        ShapedRecipeBuilder
                .shaped(RecipeCategory.COMBAT, ItemRegistry.STAR_GATHERERS_HOOD.get())
                .pattern("WWW")
                .pattern("WSW")
                .define('S', ItemRegistry.STARDUST_SHARD.get())
                .define('W', ItemTags.WOOL)
                .unlockedBy("has_wools", has(ItemTags.WOOL))
                .save(output);

        ShapedRecipeBuilder
                .shaped(RecipeCategory.COMBAT, ItemRegistry.STAR_GATHERERS_TUNIC.get())
                .pattern("WSW")
                .pattern("WWW")
                .pattern("WWW")
                .define('S', ItemRegistry.STARDUST_SHARD.get())
                .define('W', ItemTags.WOOL)
                .unlockedBy("has_wools", has(ItemTags.WOOL))
                .save(output);

        ShapedRecipeBuilder
                .shaped(RecipeCategory.COMBAT, ItemRegistry.STAR_GATHERERS_TROUSERS.get())
                .pattern("WWW")
                .pattern("WSW")
                .pattern("W W")
                .define('S', ItemRegistry.STARDUST_SHARD.get())
                .define('W', ItemTags.WOOL)
                .unlockedBy("has_wools", has(ItemTags.WOOL))
                .save(output);

        ShapedRecipeBuilder
                .shaped(RecipeCategory.COMBAT, ItemRegistry.STAR_GATHERERS_BOOTS.get())
                .pattern("W W")
                .pattern("WSW")
                .define('S', ItemRegistry.STARDUST_SHARD.get())
                .define('W', ItemTags.WOOL)
                .unlockedBy("has_wools", has(ItemTags.WOOL))
                .save(output);

        schoolArmorRecipes(output, ItemRegistry.ZODIAC_RUNE.get(), wizardBase("helmet"), ItemRegistry.ASTRAL_OBSERVERS_HOOD.get());
        schoolArmorRecipes(output, ItemRegistry.ZODIAC_RUNE.get(), wizardBase("chestplate"), ItemRegistry.ASTRAL_OBSERVERS_COAT.get());
        schoolArmorRecipes(output, ItemRegistry.ZODIAC_RUNE.get(), wizardBase("leggings"), ItemRegistry.ASTRAL_OBSERVERS_LEGGINGS.get());
        schoolArmorRecipes(output, ItemRegistry.ZODIAC_RUNE.get(), wizardBase("boots"), ItemRegistry.ASTRAL_OBSERVERS_BOOTS.get());

        buildingRecipes(output);
        for (var holder : List.of(BlockRegistry.STELLAR_DROP_ORE, BlockRegistry.DEEPSLATE_STELLAR_DROP_ORE)) {
            var ore = holder.get();
            var name = holder.getId().getPath();
            SimpleCookingRecipeBuilder.smelting(Ingredient.of(ore), RecipeCategory.MISC,
                            ItemRegistry.STARDUST_SHARD.get(), 1.5F, 200)
                    .unlockedBy("has_" + name, has(ore))
                    .save(output, ConstellationnSpells.MODID + ":stardust_shard_from_smelting_" + name);
            SimpleCookingRecipeBuilder.blasting(Ingredient.of(ore), RecipeCategory.MISC,
                            ItemRegistry.STARDUST_SHARD.get(), 1.5F, 100)
                    .unlockedBy("has_" + name, has(ore))
                    .save(output, ConstellationnSpells.MODID + ":stardust_shard_from_blasting_" + name);
        }
    }

    private static void buildingRecipes(RecipeOutput output) {
        var shard = ItemRegistry.STARDUST_SHARD.get();
        var base = BlockRegistry.BLOCK_OF_STARDUST_SHARD.get();
        var smooth = BlockRegistry.SMOOTH_STARCONDENSE_BLOCK.get();
        var shimmer = BlockRegistry.SHIMMER_STARCONDENSE_BLOCK.get();
        var smoothShimmer = BlockRegistry.SMOOTH_SHIMMER_STARCONDENSE_BLOCK.get();
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, base)
                .pattern("SS").pattern("SS").define('S', shard)
                .unlockedBy("has_stardust_shard", has(shard)).save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, shard, 4).requires(base)
                .unlockedBy("has_stardust_block", has(base))
                .save(output, ConstellationnSpells.MODID + ":stardust_shard_from_block");
        stoneVariants(output, base, BlockRegistry.STARCONDENSE_SLAB.get(), BlockRegistry.STARCONDENSE_STAIRS.get());
        stoneVariants(output, smooth, BlockRegistry.SMOOTH_STARCONDENSE_SLAB.get(), BlockRegistry.SMOOTH_STARCONDENSE_STAIRS.get());
        smoothRecipe(output, base, smooth);
        smoothRecipe(output, shimmer, smoothShimmer);
        surroundWithShard(output, base, shimmer);
        surroundWithShard(output, smooth, smoothShimmer);
        surroundWithShard(output, Items.GLASS, BlockRegistry.STARCONDENSE_GLASS.get());
        surroundWithShard(output, BlockRegistry.STARCONDENSE_GLASS.get(), BlockRegistry.SHIMMER_STARCONDENSE_GLASS.get());
        pane(output, BlockRegistry.STARCONDENSE_GLASS.get(), BlockRegistry.STARCONDENSE_GLASS_PANE.get());
        pane(output, BlockRegistry.SHIMMER_STARCONDENSE_GLASS.get(), BlockRegistry.SHIMMER_STARCONDENSE_GLASS_PANE.get());
        var midnight = BlockRegistry.MIDNIGHT_STARCONDENSE_BLOCK.get();
        var smoothMidnight = BlockRegistry.SMOOTH_MIDNIGHT_STARCONDENSE_BLOCK.get();
        surround(output, base, Ingredient.of(Items.COAL, Items.CHARCOAL), midnight, 8);
        surround(output, smooth, Ingredient.of(Items.COAL, Items.CHARCOAL), smoothMidnight, 8);
        smoothRecipe(output, midnight, smoothMidnight);
        stoneVariants(output, midnight, BlockRegistry.MIDNIGHT_STARCONDENSE_SLAB.get(), BlockRegistry.MIDNIGHT_STARCONDENSE_STAIRS.get());
        stoneVariants(output, smoothMidnight, BlockRegistry.SMOOTH_MIDNIGHT_STARCONDENSE_SLAB.get(), BlockRegistry.SMOOTH_MIDNIGHT_STARCONDENSE_STAIRS.get());
        surround(output, BlockRegistry.STARCONDENSE_GLASS.get(), Ingredient.of(Items.AMETHYST_BLOCK),
                BlockRegistry.MIDNIGHT_STARCONDENSE_GLASS.get(), 9);
    }

    private static void surround(RecipeOutput output, ItemLike surrounding, Ingredient center, ItemLike result, int count) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result, count)
                .pattern("BBB").pattern("BCB").pattern("BBB")
                .define('B', surrounding).define('C', center)
                .unlockedBy("has_base", has(surrounding)).save(output);
    }

    private static void stoneVariants(RecipeOutput output, ItemLike base,
                                      ItemLike slab, ItemLike stairs) {
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, slab, Ingredient.of(base))
                .unlockedBy("has_base", has(base)).save(output);
        stairBuilder(stairs, Ingredient.of(base)).unlockedBy("has_base", has(base)).save(output);
        stonecut(output, base, slab, 2);
        stonecut(output, base, stairs, 1);
    }

    private static void stonecut(RecipeOutput output, ItemLike base, ItemLike result, int count) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(base), RecipeCategory.BUILDING_BLOCKS, result, count)
                .unlockedBy("has_base", has(base))
                .save(output, BuiltInRegistries.ITEM.getKey(result.asItem()).withSuffix("_from_stonecutting"));
    }

    private static void surroundWithShard(RecipeOutput output, ItemLike surrounding,
                                          ItemLike result) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result, 8)
                .pattern("BBB").pattern("BSB").pattern("BBB")
                .define('B', surrounding).define('S', ItemRegistry.STARDUST_SHARD.get())
                .unlockedBy("has_base", has(surrounding)).save(output);
    }

    private static void smoothRecipe(RecipeOutput output, ItemLike base,
                                     ItemLike result) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(base), RecipeCategory.BUILDING_BLOCKS, result, 0.1F, 200)
                .unlockedBy("has_base", has(base))
                .save(output, BuiltInRegistries.ITEM.getKey(result.asItem()).withSuffix("_from_smelting"));
    }

    private static void pane(RecipeOutput output, ItemLike glass,
                             ItemLike pane) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, pane, 16)
                .pattern("GGG").pattern("GGG").define('G', glass)
                .unlockedBy("has_glass", has(glass)).save(output);
    }

    private static TagKey<Item> wizardBase(String slot) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath(
                "irons_spellbooks", "wizard_base_" + slot));
    }

    private static void schoolArmorRecipes(RecipeOutput output, Item rune, TagKey<Item> base, Item resultItem) {
        var id = BuiltInRegistries.ITEM.getKey(resultItem);
        var result = resultItem.getDefaultInstance();
        // 空コンテナを含む完成品を出力し、専用レシピで Imbue 以外の装備データだけを引き継ぐ.
        output.accept(id.withSuffix("_smithing"), new ResetImbueSmithingRecipe(
                Ingredient.of(rune), Ingredient.of(base), Ingredient.of(ARCANE_ESSENCE.get()), result), null);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, result)
                .requires(base)
                .requires(rune)
                .requires(ARCANE_ESSENCE.get())
                .unlockedBy("has_base_armor", has(base))
                .save(output, id.withSuffix("_crafting"));
    }
}
