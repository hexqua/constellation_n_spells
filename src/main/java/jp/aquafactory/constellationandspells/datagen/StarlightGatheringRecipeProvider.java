package jp.aquafactory.constellationandspells.datagen;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.recipe.StarlightGatheringRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class StarlightGatheringRecipeProvider implements DataProvider {
    private final PackOutput.PathProvider recipes;
    private final CompletableFuture<HolderLookup.Provider> lookup;

    public StarlightGatheringRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        recipes = output.createRegistryElementsPathProvider(Registries.RECIPE);
        this.lookup = lookup;
    }

    @Override
    public @NotNull String getName() {
        return "Starlight Gathering Recipes";
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        var conversions = List.of(
                new Conversion("rotten_flesh_to_leather", Items.ROTTEN_FLESH, Items.LEATHER)
        );
        return lookup.thenCompose(registries -> CompletableFuture.allOf(conversions.stream()
                .map(conversion -> saveRecipe(output, registries, conversion))
                .toArray(CompletableFuture<?>[]::new)));
    }

    private CompletableFuture<?> saveRecipe(CachedOutput output, HolderLookup.Provider registries, Conversion conversion) {
        var id = ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "starlight_gathering/" + conversion.id());
        var recipe = new StarlightGatheringRecipe(Ingredient.of(conversion.input()), new ItemStack(conversion.result()));
        return DataProvider.saveStable(output, registries, Recipe.CODEC, recipe, recipes.json(id));
    }

    private record Conversion(String id, ItemLike input, ItemLike result) {}
}
