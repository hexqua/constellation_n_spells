package jp.aquafactory.constellationandspells.gametest;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
public class RegistrationGameTests {
    // 初期コードの登録と専用サーバー上の生成経路を確認する。機能追加時に対象を更新する。
    @GameTest(template = "gametest/basic_floor")
    public static void registeredBlockCanBePlaced(GameTestHelper helper) {
        var block = ConstellationnSpells.EXAMPLE_BLOCK.get();
        var item = ConstellationnSpells.EXAMPLE_BLOCK_ITEM.get();
        helper.assertTrue(item.getBlock() == block,
                "Registered block item must refer to the registered block");
        helper.setBlock(new BlockPos(1, 1, 1), block);
        helper.assertBlockPresent(block, new BlockPos(1, 1, 1));
        helper.succeed();
    }
}
