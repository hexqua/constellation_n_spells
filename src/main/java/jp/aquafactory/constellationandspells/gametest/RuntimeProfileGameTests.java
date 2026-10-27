package jp.aquafactory.constellationandspells.gametest;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
// GameTest がアノテーションから探索して呼び出す。
@SuppressWarnings("unused")
public class RuntimeProfileGameTests {
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
}
