package jp.aquafactory.constellationandspells.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import jp.aquafactory.constellationandspells.registry.RecipeSerializerRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.jetbrains.annotations.NotNull;

public final class ResetImbueSmithingRecipe extends SmithingTransformRecipe {
    private final Ingredient templateIngredient;
    private final Ingredient baseIngredient;
    private final Ingredient additionIngredient;
    private final ItemStack resultStack;

    public ResetImbueSmithingRecipe(Ingredient template, Ingredient base, Ingredient addition, ItemStack result) {
        super(template, base, addition, result);
        templateIngredient = template;
        baseIngredient = base;
        additionIngredient = addition;
        resultStack = result;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SmithingRecipeInput input, @NotNull HolderLookup.Provider registries) {
        var result = super.assemble(input, registries);
        // Iron's は標準鍛冶レシピの戻り際に元の魔法を復元するため、その処理後に完成品の枠へ戻す.
        var container = ISpellContainer.get(resultStack);
        if (container == null) {
            ISpellContainer.remove(result);
        } else {
            ISpellContainer.set(result, container);
        }
        return result;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return RecipeSerializerRegistry.RESET_IMBUE_SMITHING.get();
    }

    public static final class Serializer implements RecipeSerializer<ResetImbueSmithingRecipe> {
        private static final MapCodec<ResetImbueSmithingRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                Ingredient.CODEC.fieldOf("template").forGetter(recipe -> recipe.templateIngredient),
                Ingredient.CODEC.fieldOf("base").forGetter(recipe -> recipe.baseIngredient),
                Ingredient.CODEC.fieldOf("addition").forGetter(recipe -> recipe.additionIngredient),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.resultStack)
        ).apply(builder, ResetImbueSmithingRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, ResetImbueSmithingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.templateIngredient,
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.baseIngredient,
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.additionIngredient,
                ItemStack.STREAM_CODEC, recipe -> recipe.resultStack,
                ResetImbueSmithingRecipe::new);

        @Override
        public @NotNull MapCodec<ResetImbueSmithingRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, ResetImbueSmithingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
