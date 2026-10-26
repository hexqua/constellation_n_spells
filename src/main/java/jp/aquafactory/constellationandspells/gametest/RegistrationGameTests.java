package jp.aquafactory.constellationandspells.gametest;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
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

    @GameTest(template = "gametest/basic_floor")
    public static void stardustShardIsTheOnlyRegisteredItem(GameTestHelper helper) {
        var itemId = ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "stardust_shard");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(itemId), "Stardust shard must be registered");
        var item = ConstellationnSpells.STARDUST_SHARD.get();
        helper.assertTrue(BuiltInRegistries.ITEM.get(itemId) == item,
                "Stardust shard must resolve to the registered item");
        var itemCount = BuiltInRegistries.ITEM.keySet().stream()
                .filter(id -> id.getNamespace().equals(ConstellationnSpells.MODID)).count();
        helper.assertTrue(itemCount == 1, "The mod must register exactly one item");
        helper.assertTrue(BuiltInRegistries.BLOCK.keySet().stream()
                        .noneMatch(id -> id.getNamespace().equals(ConstellationnSpells.MODID)),
                "The mod must not register sample blocks");
        helper.assertTrue(ConstellationnSpells.MAIN_TAB.get().getIconItem().is(item),
                "Creative tab icon must be the stardust shard");
        helper.succeed();
    }
}
