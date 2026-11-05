package jp.aquafactory.constellationandspells.gametest;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.item.armor.StarGatherersArmorItem;
import jp.aquafactory.constellationandspells.registry.AttributeRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MAX_MANA;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
// GameTest がアノテーションから探索して呼び出す。
@SuppressWarnings("unused")
public final class ArmorGameTests {
    @GameTest(template = "gametest/basic_floor")
    public static void armorAcceptsWoolRepairIngredients(GameTestHelper helper) {
        for (var item : armor()) {
            var stack = new ItemStack(item);
            int woolCount = 0;
            for (var entry : BuiltInRegistries.ITEM.getTagOrEmpty(ItemTags.WOOL)) {
                helper.assertTrue(item.isValidRepairItem(stack, new ItemStack(entry)),
                        "Every wool color must repair Star Gatherer's armor");
                woolCount++;
            }
            helper.assertTrue(woolCount >= 16, "The wool repair tag must contain all sixteen vanilla colors");
            helper.assertTrue(!item.isValidRepairItem(stack, new ItemStack(Items.IRON_INGOT))
                            && !item.isValidRepairItem(stack, new ItemStack(Items.LEATHER)),
                    "Iron and leather must not repair Star Gatherer's armor");
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void armorSupportsVanillaEnchantmentsForItsSlot(GameTestHelper helper) {
        var armor = armor();
        Item[] reference = {Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS};
        var enchantments = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        for (int i = 0; i < armor.length; i++) {
            var stack = new ItemStack(armor[i]);
            var vanilla = new ItemStack(reference[i]);
            for (var enchantment : enchantments.holders().toList()) {
                if (!enchantment.key().location().getNamespace().equals("minecraft")) {
                    continue;
                }
                helper.assertTrue(stack.supportsEnchantment(enchantment) == vanilla.supportsEnchantment(enchantment),
                        "Anvil enchantment eligibility must match the same vanilla armor slot: " + enchantment.key().location());
                helper.assertTrue(stack.isPrimaryItemFor(enchantment) == vanilla.isPrimaryItemFor(enchantment),
                        "Enchanting table eligibility must match the same vanilla armor slot: " + enchantment.key().location());
            }
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void armorAttributesFollowEquipmentChanges(GameTestHelper helper) {
        // FakePlayer は tick を空実装にするため、標準の装備更新を通せる mock player を使う。
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        player.setPos(pos.getX(), pos.getY(), pos.getZ());
        double initialMana = player.getAttributeValue(MAX_MANA);
        double initialPower = player.getAttributeValue(AttributeRegistry.ZODIAC_SPELL_POWER);
        double initialDefense = player.getAttributeValue(Attributes.ARMOR);
        var armor = armor();

        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(armor[0]));
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(armor[3]));
        player.tick();
        assertAttributes(helper, player, initialMana, initialPower, initialDefense);
        player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);

        for (var item : armor) {
            player.setItemSlot(item.getEquipmentSlot(), new ItemStack(item));
            player.tick();
            assertAttributes(helper, player, initialMana + 50, initialPower * 1.05,
                    initialDefense + item.getDefense());
            player.setItemSlot(item.getEquipmentSlot(), ItemStack.EMPTY);
            player.tick();
            assertAttributes(helper, player, initialMana, initialPower, initialDefense);
        }

        for (var item : armor) {
            player.setItemSlot(item.getEquipmentSlot(), new ItemStack(item));
        }
        player.tick();
        assertAttributes(helper, player, initialMana + 200, initialPower * 1.2, initialDefense + 10);
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemRegistry.STAR_GATHERERS_TUNIC.get()));
        player.tick();
        assertAttributes(helper, player, initialMana + 200, initialPower * 1.2, initialDefense + 10);

        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        player.tick();
        assertAttributes(helper, player, initialMana + 150, initialPower * 1.15, initialDefense + 12);
        for (var item : armor) {
            player.setItemSlot(item.getEquipmentSlot(), ItemStack.EMPTY);
        }
        player.tick();
        assertAttributes(helper, player, initialMana, initialPower, initialDefense);
        helper.succeed();
    }

    private static void assertAttributes(GameTestHelper helper, Player player, double mana, double power, double defense) {
        helper.assertTrue(Math.abs(player.getAttributeValue(MAX_MANA) - mana) < 0.000001,
                "Equipped armor must add exactly fifty max mana per piece");
        helper.assertTrue(Math.abs(player.getAttributeValue(AttributeRegistry.ZODIAC_SPELL_POWER) - power) < 0.000001,
                "Equipped armor must add five percent base Zodiac power per piece");
        helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ARMOR) - defense) < 0.000001,
                "Armor defense must survive attribute merging and equipment replacement");
        helper.assertTrue(player.getAttributeValue(Attributes.ARMOR_TOUGHNESS) == 0
                        && player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) == 0,
                "Armor must not add toughness or knockback resistance");
    }

    private static StarGatherersArmorItem[] armor() {
        return new StarGatherersArmorItem[]{ItemRegistry.STAR_GATHERERS_HOOD.get(), ItemRegistry.STAR_GATHERERS_TUNIC.get(),
                ItemRegistry.STAR_GATHERERS_TROUSERS.get(), ItemRegistry.STAR_GATHERERS_BOOTS.get()};
    }
}
