package jp.aquafactory.constellationandspells.gametest;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.AttributeRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Objects;

import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.COOLDOWN_REDUCTION;
import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.SPELL_POWER;
import static io.redspace.ironsspellbooks.registries.ItemRegistry.LIGHTNING_ROD_STAFF;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
// GameTestがアノテーションから探索して呼び出す。
@SuppressWarnings("unused")
public final class CelestialSurveyStaffGameTests {
    @GameTest(template = "gametest/basic_floor")
    public static void attributesFollowMainHandEquipment(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var staff = new ItemStack(ItemRegistry.CELESTIAL_SURVEY_STAFF.get());
        player.setItemSlot(EquipmentSlot.MAINHAND, staff);
        player.tick();
        assertAttributes(helper, player, 5, 1, 1.05, 1.15, 1.15);

        // 加算と既存のBASE補正を併用し、単純加算やTOTAL乗算も区別する。
        for (var attribute : List.of(SPELL_POWER, AttributeRegistry.ZODIAC_SPELL_POWER, COOLDOWN_REDUCTION)) {
            var instance = Objects.requireNonNull(player.getAttribute(attribute));
            instance.setBaseValue(2);
            instance.addTransientModifier(new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "staff_test_add"),
                    0.5, AttributeModifier.Operation.ADD_VALUE));
            instance.addTransientModifier(new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "staff_test_base"),
                    0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        assertAttributes(helper, player, 5, 1, 3.125, 3.375, 3.375);
        player.setItemSlot(EquipmentSlot.MAINHAND, staff.copy());
        player.tick();
        assertAttributes(helper, player, 5, 1, 3.125, 3.375, 3.375);

        player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.OFFHAND, staff);
        player.tick();
        assertAttributes(helper, player, 1, 4, 3, 3, 3);
        player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.MAINHAND, staff);
        player.tick();
        assertAttributes(helper, player, 5, 1, 3.125, 3.375, 3.375);
        player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        player.tick();
        assertAttributes(helper, player, 1, 4, 3, 3, 3);
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void enchantmentEligibilityMatchesLightningRod(GameTestHelper helper) {
        var staff = new ItemStack(ItemRegistry.CELESTIAL_SURVEY_STAFF.get());
        var reference = new ItemStack(LIGHTNING_ROD_STAFF.get());
        var enchantments = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        boolean foundSupported = false;
        for (var enchantment : enchantments.holders().toList()) {
            helper.assertTrue(staff.supportsEnchantment(enchantment) == reference.supportsEnchantment(enchantment),
                    "Anvil enchantment eligibility must match Rod o' Lightning: " + enchantment.key().location());
            helper.assertTrue(staff.isPrimaryItemFor(enchantment) == reference.isPrimaryItemFor(enchantment),
                    "Enchanting table eligibility must match Rod o' Lightning: " + enchantment.key().location());
            foundSupported |= reference.isPrimaryItemFor(enchantment);
        }
        helper.assertTrue(foundSupported && staff.isEnchantable(), "The staff must support enchanting table enchantments");
        helper.succeed();
    }

    private static void assertAttributes(GameTestHelper helper, Player player, double damage, double speed,
                                         double power, double zodiac, double cooldown) {
        assertAttribute(helper, player, Attributes.ATTACK_DAMAGE, damage);
        assertAttribute(helper, player, Attributes.ATTACK_SPEED, speed);
        assertAttribute(helper, player, SPELL_POWER, power);
        assertAttribute(helper, player, AttributeRegistry.ZODIAC_SPELL_POWER, zodiac);
        assertAttribute(helper, player, COOLDOWN_REDUCTION, cooldown);
    }

    private static void assertAttribute(GameTestHelper helper, Player player, Holder<Attribute> attribute, double expected) {
        double actual = player.getAttributeValue(attribute);
        helper.assertTrue(Math.abs(actual - expected) < 0.000001,
                "Staff equipment must update " + attribute.getRegisteredName() + ": expected=" + expected + ", actual=" + actual);
    }
}
