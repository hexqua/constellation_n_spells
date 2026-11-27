package jp.aquafactory.constellationandspells.registry;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.recipe.ResetImbueSmithingRecipe;
import jp.aquafactory.constellationandspells.recipe.StarlightGatheringRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RecipeSerializerRegistry {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, ConstellationnSpells.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ConstellationnSpells.MODID);
    public static final DeferredHolder<RecipeSerializer<?>, ResetImbueSmithingRecipe.Serializer> RESET_IMBUE_SMITHING =
            RECIPE_SERIALIZERS.register("reset_imbue_smithing", ResetImbueSmithingRecipe.Serializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<StarlightGatheringRecipe>> STARLIGHT_GATHERING_TYPE =
            RECIPE_TYPES.register("starlight_gathering", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "starlight_gathering")));

    public static final DeferredHolder<RecipeSerializer<?>, StarlightGatheringRecipe.Serializer> STARLIGHT_GATHERING =
            RECIPE_SERIALIZERS.register("starlight_gathering", StarlightGatheringRecipe.Serializer::new);

    private RecipeSerializerRegistry() {
    }
}
