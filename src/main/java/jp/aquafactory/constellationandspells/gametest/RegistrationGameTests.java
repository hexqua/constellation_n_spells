package jp.aquafactory.constellationandspells.gametest;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
// GameTest がアノテーションから探索して呼び出す。
@SuppressWarnings("unused")
public class RegistrationGameTests {
    @GameTest(template = "gametest/basic_floor")
    public static void runtimeProfileModsAreLoaded(GameTestHelper helper) {
        var modList = ModList.get();
        for (var modId : new String[]{"irons_spellbooks", "curios", "geckolib", "irons_lib", "playeranimator"}) {
            helper.assertTrue(modList.isLoaded(modId), "Required runtime mod is missing: " + modId);
        }
        // 起動構成の指定漏れを検出し、optional MOD 不在での成功を防ぐ。
        var profileMods = System.getProperty("constellationandspells.requiredOptionalMods", "");
        helper.assertTrue(!profileMods.isBlank(), "GameTest runtime profile must declare its expected mods");
        for (var modId : profileMods.split(",")) {
            helper.assertTrue(modList.isLoaded(modId), "Profile runtime mod is missing: " + modId);
        }
        helper.succeed();
    }

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
