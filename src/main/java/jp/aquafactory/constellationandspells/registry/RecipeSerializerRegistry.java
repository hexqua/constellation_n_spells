package jp.aquafactory.constellationandspells.registry;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.recipe.ResetImbueSmithingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RecipeSerializerRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ConstellationnSpells.MODID);
    public static final DeferredHolder<RecipeSerializer<?>, ResetImbueSmithingRecipe.Serializer> RESET_IMBUE_SMITHING =
            RECIPE_SERIALIZERS.register("reset_imbue_smithing", ResetImbueSmithingRecipe.Serializer::new);

    private RecipeSerializerRegistry() {
    }
}
