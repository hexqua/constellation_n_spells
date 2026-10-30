package jp.aquafactory.constellationandspells.gametest;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import jp.aquafactory.constellationandspells.registry.CreativeTabRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
// GameTest がアノテーションから探索して呼び出す。
@SuppressWarnings("unused")
public class RegistrationGameTests {
    @GameTest(template = "gametest/basic_floor")
    public static void materialsAndOresAreRegistered(GameTestHelper helper) {
        var itemId = ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "stardust_shard");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(itemId), "Stardust shard must be registered");
        var item = ItemRegistry.STARDUST_SHARD.get();
        helper.assertTrue(BuiltInRegistries.ITEM.get(itemId) == item,
                "Stardust shard must resolve to the registered item");
        var itemCount = BuiltInRegistries.ITEM.keySet().stream()
                .filter(id -> id.getNamespace().equals(ConstellationnSpells.MODID)).count();
        helper.assertTrue(itemCount == 5, "The mod must register three materials and two ore items");
        helper.assertTrue(BuiltInRegistries.BLOCK.keySet().stream()
                        .filter(id -> id.getNamespace().equals(ConstellationnSpells.MODID)).count() == 2,
                "The mod must register exactly two ore blocks");
        for (var ore : new Block[]{
                BlockRegistry.STELLAR_DROP_ORE.get(), BlockRegistry.DEEPSLATE_STELLAR_DROP_ORE.get()}) {
            helper.assertTrue(BuiltInRegistries.ITEM.get(BuiltInRegistries.BLOCK.getKey(ore)) == ore.asItem(),
                    "Each ore must have a matching registered block item");
        }
        helper.assertTrue(CreativeTabRegistry.MAIN_TAB.get().getIconItem().is(item),
                "Creative tab icon must be the stardust shard");
        helper.succeed();
    }
}
