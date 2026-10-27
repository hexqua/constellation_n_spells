package jp.aquafactory.constellationandspells.gametest;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
// GameTest がアノテーションから探索して呼び出す。
@SuppressWarnings("unused")
public class RecipeGameTests {
    private static List<Block> ores() {
        return List.of(BlockRegistry.STELLAR_DROP_ORE.get(), BlockRegistry.DEEPSLATE_STELLAR_DROP_ORE.get());
    }

    @GameTest(template = "gametest/basic_floor")
    public static void bothOresSmeltAndBlastIntoOneShard(GameTestHelper helper) {
        var level = helper.getLevel();
        for (var ore : ores()) {
            var name = ore == BlockRegistry.STELLAR_DROP_ORE.get() ? "stellar_drop_ore" : "deepslate_stellar_drop_ore";
            for (var method : List.of("smelting", "blasting")) {
                var id = ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID,
                        "stardust_shard_from_" + method + "_" + name);
                var recipe = level.getRecipeManager().byKey(id).orElseThrow().value();
                if (!(recipe instanceof AbstractCookingRecipe cooking)) {
                    throw new GameTestAssertException("Ore recipe must be a cooking recipe");
                }
                helper.assertTrue(cooking.getType() == (method.equals("smelting") ? RecipeType.SMELTING : RecipeType.BLASTING),
                        "Ore recipe must use the expected furnace type");
                helper.assertTrue(cooking.getIngredients().getFirst().test(new ItemStack(ore)), "Recipe must accept its ore");
                var result = cooking.getResultItem(level.registryAccess());
                helper.assertTrue(result.is(ItemRegistry.STARDUST_SHARD.get()) && result.getCount() == 1,
                        "Cooking must produce one stardust shard");
                helper.assertTrue(cooking.getExperience() == 1.5F, "Cooking experience must be 1.5");
                helper.assertTrue(cooking.getCookingTime() == (method.equals("smelting") ? 200 : 100),
                        "Ore cooking time must match vanilla ore recipes");
            }
        }
        helper.succeed();
    }
}
