package jp.aquafactory.constellationandspells.datagen;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import jp.aquafactory.constellationandspells.registry.SchoolTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

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

        for (var holder : BlockRegistry.BLOCKS.getEntries()) {
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
}
