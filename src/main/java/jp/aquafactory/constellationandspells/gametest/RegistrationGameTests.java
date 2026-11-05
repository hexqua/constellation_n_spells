package jp.aquafactory.constellationandspells.gametest;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
// GameTest がアノテーションから探索して呼び出す。
@SuppressWarnings("unused")
public final class RegistrationGameTests {
    @GameTest(template = "gametest/basic_floor")
    public static void oresHaveMatchingBlockItems(GameTestHelper helper) {
        for (var ore : new Block[]{
                BlockRegistry.STELLAR_DROP_ORE.get(), BlockRegistry.DEEPSLATE_STELLAR_DROP_ORE.get()}) {
            helper.assertTrue(BuiltInRegistries.ITEM.get(BuiltInRegistries.BLOCK.getKey(ore)) == ore.asItem(),
                    "Each ore must have a matching registered block item");
        }
        helper.succeed();
    }
}
