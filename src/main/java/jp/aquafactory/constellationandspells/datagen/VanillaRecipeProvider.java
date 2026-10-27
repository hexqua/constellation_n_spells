package jp.aquafactory.constellationandspells.datagen;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class VanillaRecipeProvider extends RecipeProvider {
    public VanillaRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
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
