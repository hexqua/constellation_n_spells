package jp.aquafactory.constellationandspells.gametest;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
@SuppressWarnings("unused") // GameTest がアノテーションから探索する。
public final class EnchantmentTagGameTests {
    private EnchantmentTagGameTests() {}

    @GameTest(template = "gametest/basic_floor")
    public static void supportedVanillaEnchantmentsRemainVisibleToTagBasedViewers(GameTestHelper helper) {
        var enchantments = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .holders().filter(holder -> holder.key().location().getNamespace().equals("minecraft")).toList();
        for (var item : ItemRegistry.ITEMS.getEntries()) {
            var stack = new ItemStack(item.get());
            for (var enchantment : enchantments) {
                if (stack.supportsEnchantment(enchantment)) {
                    // EMI は NeoForge の付与フックとは別に、supported_items のタグ判定も要求する。
                    helper.assertTrue(stack.is(enchantment.value().definition().supportedItems()),
                            "Supported enchantment must also accept the item through supported_items: "
                                    + item.getId() + " / " + enchantment.key().location());
                }
            }
        }
        helper.succeed();
    }
}
