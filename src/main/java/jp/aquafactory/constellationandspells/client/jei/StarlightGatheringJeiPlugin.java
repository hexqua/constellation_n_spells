package jp.aquafactory.constellationandspells.client.jei;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.recipe.StarlightGatheringRecipe;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import jp.aquafactory.constellationandspells.registry.RecipeSerializerRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@JeiPlugin
public final class StarlightGatheringJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "starlight_gathering");
    private static final RecipeType<RecipeHolder<StarlightGatheringRecipe>> TYPE = RecipeType.createRecipeHolderType(ID);

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new Category(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            registration.addRecipes(TYPE, connection.getRecipeManager()
                    .getAllRecipesFor(RecipeSerializerRegistry.STARLIGHT_GATHERING_TYPE.get()));
        }
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalysts(TYPE, ItemRegistry.STARLIGHT_GATHERING_PLATE.get());
    }

    private static final class Category implements IRecipeCategory<RecipeHolder<StarlightGatheringRecipe>> {
        private final IDrawable icon;
        private final IDrawable arrow;
        private final IDrawable plus;

        private Category(IGuiHelper gui) {
            icon = gui.createDrawableItemLike(ItemRegistry.STARLIGHT_GATHERING_PLATE.get());
            arrow = gui.getRecipeArrow();
            plus = gui.getRecipePlusSign();
        }

        @Override
        public @NotNull RecipeType<RecipeHolder<StarlightGatheringRecipe>> getRecipeType() {
            return TYPE;
        }

        @Override
        public @NotNull Component getTitle() {
            return ItemRegistry.STARLIGHT_GATHERING_PLATE.get().getDescription();
        }

        @Override
        public int getWidth() {
            return 112;
        }

        @Override
        public int getHeight() {
            return 26;
        }

        @Override
        public @NotNull IDrawable getIcon() {
            return icon;
        }

        @Override
        public @NotNull ResourceLocation getRegistryName(@NotNull RecipeHolder<StarlightGatheringRecipe> recipe) {
            return recipe.id();
        }

        @Override
        public void setRecipe(@NotNull IRecipeLayoutBuilder layout, @NotNull RecipeHolder<StarlightGatheringRecipe> recipe,
                              @NotNull IFocusGroup focuses) {
            layout.addInputSlot(4, 5).setStandardSlotBackground().addIngredients(recipe.value().ingredient());
            layout.addInputSlot(38, 5).setStandardSlotBackground().addItemStack(new ItemStack(ItemRegistry.STARDUST_SHARD.get()));
            layout.addOutputSlot(88, 5).setOutputSlotBackground().addItemStack(recipe.value().result().copy());
        }

        @Override
        public void draw(@NotNull RecipeHolder<StarlightGatheringRecipe> recipe, @NotNull IRecipeSlotsView slots, @NotNull GuiGraphics graphics,
                         double mouseX, double mouseY) {
            plus.draw(graphics, 24, 8);
            arrow.draw(graphics, 59, 4);
        }
    }
}
