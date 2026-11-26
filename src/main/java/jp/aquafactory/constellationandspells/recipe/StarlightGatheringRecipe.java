package jp.aquafactory.constellationandspells.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import jp.aquafactory.constellationandspells.registry.RecipeSerializerRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Optional;

public record StarlightGatheringRecipe(Ingredient ingredient, ItemStack result) implements Recipe<SingleRecipeInput> {
    public static Optional<RecipeHolder<StarlightGatheringRecipe>> find(Level level, ItemStack stack) {
        var input = new SingleRecipeInput(stack.copyWithCount(1));
        return level.getRecipeManager().getAllRecipesFor(RecipeSerializerRegistry.STARLIGHT_GATHERING_TYPE.get()).stream()
                .filter(recipe -> recipe.value().matches(input, level))
                .min(Comparator.comparing(RecipeHolder::id));
    }

    @Override
    public boolean matches(@NotNull SingleRecipeInput input, @NotNull Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SingleRecipeInput input, HolderLookup.@NotNull Provider registries) {
        return getResultItem(registries);
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width > 0 && height > 0;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return RecipeSerializerRegistry.STARLIGHT_GATHERING.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return RecipeSerializerRegistry.STARLIGHT_GATHERING_TYPE.get();
    }

    public static final class Serializer implements RecipeSerializer<StarlightGatheringRecipe> {
        private static final MapCodec<StarlightGatheringRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(StarlightGatheringRecipe::ingredient),
                ItemStack.STRICT_SINGLE_ITEM_CODEC.fieldOf("result").forGetter(StarlightGatheringRecipe::result)
        ).apply(builder, StarlightGatheringRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, StarlightGatheringRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, StarlightGatheringRecipe::ingredient,
                ItemStack.STREAM_CODEC, StarlightGatheringRecipe::result,
                StarlightGatheringRecipe::new);

        @Override
        public @NotNull MapCodec<StarlightGatheringRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, StarlightGatheringRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
