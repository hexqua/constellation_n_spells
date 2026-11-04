package jp.aquafactory.constellationandspells.gametest;

import io.redspace.ironsspellbooks.api.item.UpgradeData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilMenu;
import io.netty.buffer.Unpooled;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.item.armor.AstralObserversArmorItem;
import jp.aquafactory.constellationandspells.recipe.ResetImbueSmithingRecipe;
import jp.aquafactory.constellationandspells.registry.AttributeRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import jp.aquafactory.constellationandspells.registry.RecipeSerializerRegistry;
import jp.aquafactory.constellationandspells.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.connection.ConnectionType;

import java.util.List;
import java.util.Objects;

import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MAX_MANA;
import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.SPELL_POWER;
import static io.redspace.ironsspellbooks.registries.ItemRegistry.ARCANE_ESSENCE;
import static io.redspace.ironsspellbooks.registries.ItemRegistry.MAGIC_CLOTH;
import static io.redspace.ironsspellbooks.registries.ItemRegistry.SCROLL;
import static io.redspace.ironsspellbooks.registries.ItemRegistry.SHRIVING_STONE;
import static io.redspace.ironsspellbooks.registries.ItemRegistry.WIZARD_BOOTS;
import static io.redspace.ironsspellbooks.registries.ItemRegistry.WIZARD_CHESTPLATE;
import static io.redspace.ironsspellbooks.registries.ItemRegistry.WIZARD_HELMET;
import static io.redspace.ironsspellbooks.registries.ItemRegistry.WIZARD_LEGGINGS;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
// GameTest がアノテーションから探索して呼び出す。
@SuppressWarnings("unused")
public class AstralObserversArmorGameTests {
    private static final ResourceLocation STARDUST_ID =
            ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "stardust");

    @GameTest(template = "gametest/basic_floor")
    public static void armorHasSchoolStatsAndVanillaEnchantments(GameTestHelper helper) {
        int[] defense = {3, 8, 6, 3};
        int[] durability = {407, 592, 555, 481};
        Item[] reference = {Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS};
        var enchantments = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        var armor = armor();
        for (int i = 0; i < armor.length; i++) {
            var item = armor[i];
            var stack = new ItemStack(item);
            helper.assertTrue(item.getDefense() == defense[i] && stack.getMaxDamage() == durability[i],
                    "Armor must have the declared defense and durability");
            helper.assertTrue(stack.getEnchantmentValue() == 20 && stack.getMaxStackSize() == 1
                            && stack.getRarity() == Rarity.COMMON && !stack.is(ItemTags.TRIMMABLE_ARMOR)
                            && !stack.is(ItemTags.DYEABLE) && !stack.has(DataComponents.FIRE_RESISTANT),
                    "Armor must be common, enchantable, unstackable, untrimmable, undyeable and not fire resistant");
            helper.assertTrue(item.getEquipSound().equals(SoundRegistry.ARMOR_EQUIP_CLOTH)
                            && item.getToughness() == 0 && item.getMaterial().value().knockbackResistance() == 0,
                    "Armor must use the cloth sound without toughness or knockback resistance");
            helper.assertTrue(item.isValidRepairItem(stack, new ItemStack(MAGIC_CLOTH.get()))
                            && !item.isValidRepairItem(stack, new ItemStack(Items.WHITE_WOOL))
                            && !item.isValidRepairItem(stack, new ItemStack(ARCANE_ESSENCE.get()))
                            && !item.isValidRepairItem(stack, new ItemStack(ItemRegistry.ZODIAC_RUNE.get())),
                    "Only magic cloth among the tested materials must repair armor");
            helper.assertTrue(ISpellContainer.isSpellContainer(stack) == (i == 1),
                    "Only the coat must have a spell container");
            for (var enchantment : enchantments.holders().toList()) {
                if (enchantment.key().location().getNamespace().equals("minecraft")) {
                    var vanilla = new ItemStack(reference[i]);
                    helper.assertTrue(stack.supportsEnchantment(enchantment) == vanilla.supportsEnchantment(enchantment)
                                    && stack.isPrimaryItemFor(enchantment) == vanilla.isPrimaryItemFor(enchantment),
                            "Enchantment eligibility must match the vanilla slot: " + enchantment.key().location());
                }
            }
        }
        helper.assertTrue(!ISpellContainer.isSpellContainer(new ItemStack(ItemRegistry.STAR_GATHERERS_TUNIC.get())),
                "Star Gatherer's armor must remain without Imbue slots");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void attributesFollowEquipmentAndMixedSets(GameTestHelper helper) {
        var player = player(helper);
        double mana = player.getAttributeValue(MAX_MANA);
        double power = player.getAttributeValue(SPELL_POWER);
        double zodiac = player.getAttributeValue(AttributeRegistry.ZODIAC_SPELL_POWER);
        double defense = player.getAttributeValue(Attributes.ARMOR);
        for (var item : armor()) {
            player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(item));
            player.tick();
            assertAttributes(helper, player, mana, power, zodiac, defense);
            player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            player.setItemSlot(item.getEquipmentSlot(), new ItemStack(item));
            player.tick();
            assertAttributes(helper, player, mana + 125, power * 1.05, zodiac * 1.10, defense + item.getDefense());
            player.setItemSlot(item.getEquipmentSlot(), ItemStack.EMPTY);
            player.tick();
            assertAttributes(helper, player, mana, power, zodiac, defense);
        }
        for (var item : armor()) {
            player.setItemSlot(item.getEquipmentSlot(), new ItemStack(item));
        }
        player.tick();
        assertAttributes(helper, player, mana + 500, power * 1.20, zodiac * 1.40, defense + 20);
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemRegistry.ASTRAL_OBSERVERS_COAT.get()));
        player.tick();
        assertAttributes(helper, player, mana + 500, power * 1.20, zodiac * 1.40, defense + 20);
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemRegistry.STAR_GATHERERS_TUNIC.get()));
        player.tick();
        assertAttributes(helper, player, mana + 425, power * 1.15, zodiac * 1.35, defense + 16);
        for (var item : armor()) {
            player.setItemSlot(item.getEquipmentSlot(), ItemStack.EMPTY);
        }
        player.tick();
        assertAttributes(helper, player, mana, power, zodiac, defense);
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void imbueSurvivesCopyAndSaveAndCanBeReplacedAndRemoved(GameTestHelper helper) {
        var menu = new ArcaneAnvilMenu(0, player(helper).getInventory(), ContainerLevelAccess.NULL);
        var coat = new ItemStack(ItemRegistry.ASTRAL_OBSERVERS_COAT.get());
        assertEmptyCoat(helper, coat);
        var spellIds = List.of(STARDUST_ID, ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "firebolt"));
        for (var spellId : spellIds) {
            var spell = Objects.requireNonNull(SpellRegistry.REGISTRY.get(spellId), "Imbue test spell must resolve");
            var scroll = new ItemStack(SCROLL.get());
            ISpellContainer.createScrollContainer(spell, 1, scroll);
            coat = combine(menu, coat, scroll);
            var container = ISpellContainer.get(coat);
            helper.assertTrue(container != null && container.getMaxSpellCount() == 1
                            && container.getActiveSpellCount() == 1 && container.mustEquip() && container.isSpellWheel()
                            && container.getSpellAtIndex(0).getSpell().equals(spell),
                    "The coat must accept and replace a spell from any school in its equipped wheel slot");
            var copied = coat.copy();
            var loaded = ItemStack.parse(helper.getLevel().registryAccess(), coat.save(helper.getLevel().registryAccess())).orElseThrow();
            for (var retained : List.of(copied, loaded)) {
                helper.assertTrue(ISpellContainer.get(retained).getSpellAtIndex(0).getSpell().equals(spell),
                        "Copying and saving must preserve the imbued spell");
            }
            coat = loaded;
        }
        assertEmptyCoat(helper, combine(menu, coat, new ItemStack(SHRIVING_STONE.get())));
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void recipesResetImbueAndDifferInEquipmentDataRetention(GameTestHelper helper) {
        var level = helper.getLevel();
        var access = level.registryAccess();
        var protection = access.holderOrThrow(Enchantments.PROTECTION);
        var menu = new ArcaneAnvilMenu(0, player(helper).getInventory(), ContainerLevelAccess.NULL);
        Item[] bases = {WIZARD_HELMET.get(), WIZARD_CHESTPLATE.get(), WIZARD_LEGGINGS.get(), WIZARD_BOOTS.get()};
        var armor = armor();
        for (int i = 0; i < armor.length; i++) {
            var base = combine(menu, new ItemStack(bases[i]), new ItemStack(ItemRegistry.ZODIAC_UPGRADE_ORB.get()));
            base.setDamageValue(37);
            base.set(DataComponents.CUSTOM_NAME, Component.literal("Retained armor"));
            base.enchant(protection, 3);
            // 他 MOD や設定で胴以外に付いた魔法も、変換先に枠がなければ引き継がない。
            ISpellContainer.createImbuedContainer(
                    Objects.requireNonNull(SpellRegistry.REGISTRY.get(STARDUST_ID)), 1, base);
            var rune = new ItemStack(ItemRegistry.ZODIAC_RUNE.get());
            var essence = new ItemStack(ARCANE_ESSENCE.get());
            var smithingInput = new SmithingRecipeInput(rune, base, essence);
            var smithing = level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, smithingInput, level).orElseThrow();
            var itemId = BuiltInRegistries.ITEM.getKey(armor[i]);
            helper.assertTrue(smithing.id().equals(itemId.withSuffix("_smithing")),
                    "Smithing must select the matching armor recipe");
            var upgraded = smithing.value().assemble(smithingInput, access);
            var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), access, ConnectionType.NEOFORGE);
            try {
                var codec = RecipeSerializerRegistry.RESET_IMBUE_SMITHING.get().streamCodec();
                codec.encode(buffer, (ResetImbueSmithingRecipe) smithing.value());
                var syncedRecipe = codec.decode(buffer);
                helper.assertTrue(syncedRecipe.matches(smithingInput, level)
                                && ItemStack.isSameItemSameComponents(upgraded, syncedRecipe.assemble(smithingInput, access)),
                        "Recipe synchronization must preserve ingredients and Imbue reset behavior");
            } finally {
                buffer.release();
            }
            helper.assertTrue(upgraded.is(armor[i]) && upgraded.getCount() == 1 && upgraded.getDamageValue() == 37
                            && upgraded.getEnchantmentLevel(protection) == 3
                            && Component.literal("Retained armor").equals(upgraded.get(DataComponents.CUSTOM_NAME))
                            && UpgradeData.getUpgradeData(upgraded).getTotalUpgrades() == 1,
                    "Smithing must retain damage, enchantments, name and orb upgrades");
            double[] zodiac = {0};
            upgraded.forEachModifier(armor[i].getEquipmentSlot(), (attribute, modifier) -> {
                if (attribute.equals(AttributeRegistry.ZODIAC_SPELL_POWER)) {
                    zodiac[0] += modifier.amount();
                }
            });
            helper.assertTrue(Math.abs(zodiac[0] - 0.15) < 0.000001,
                    "Retained orb upgrade must add to the new armor's school power");

            var input = CraftingInput.of(3, 1, List.of(essence, rune, base));
            var crafting = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).orElseThrow();
            helper.assertTrue(crafting.id().equals(itemId.withSuffix("_crafting")),
                    "Shapeless crafting must select the matching armor recipe");
            var fresh = crafting.value().assemble(input, access);
            helper.assertTrue(fresh.is(armor[i]) && fresh.getCount() == 1 && fresh.getDamageValue() == 0
                            && !fresh.isEnchanted() && !fresh.has(DataComponents.CUSTOM_NAME)
                            && !UpgradeData.hasUpgradeData(fresh),
                    "Shapeless crafting must create fresh armor without inherited equipment data");
            if (i == 1) {
                assertEmptyCoat(helper, upgraded);
                assertEmptyCoat(helper, fresh);
            } else {
                helper.assertTrue(!ISpellContainer.isSpellContainer(upgraded) && !ISpellContainer.isSpellContainer(fresh),
                        "Non-coat recipes must not add Imbue slots");
            }
        }
        helper.succeed();
    }

    private static ItemStack combine(ArcaneAnvilMenu menu, ItemStack base, ItemStack addition) {
        menu.getSlot(0).set(base);
        menu.getSlot(1).set(addition);
        menu.createResult();
        return menu.getSlot(2).getItem().copy();
    }

    private static void assertEmptyCoat(GameTestHelper helper, ItemStack coat) {
        var container = ISpellContainer.get(coat);
        helper.assertTrue(container != null && container.getMaxSpellCount() == 1 && container.isEmpty()
                        && container.mustEquip() && container.isSpellWheel(),
                "The coat must retain one empty equipped spell wheel slot: "
                        + coat.save(helper.getLevel().registryAccess()));
    }

    private static Player player(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        player.setPos(pos.getX(), pos.getY(), pos.getZ());
        return player;
    }

    private static void assertAttributes(GameTestHelper helper, Player player, double mana, double power,
                                         double zodiac, double defense) {
        helper.assertTrue(Math.abs(player.getAttributeValue(MAX_MANA) - mana) < 0.000001
                        && Math.abs(player.getAttributeValue(SPELL_POWER) - power) < 0.000001
                        && Math.abs(player.getAttributeValue(AttributeRegistry.ZODIAC_SPELL_POWER) - zodiac) < 0.000001
                        && Math.abs(player.getAttributeValue(Attributes.ARMOR) - defense) < 0.000001,
                "Armor attributes must follow the equipped pieces without duplication");
        helper.assertTrue(player.getAttributeValue(Attributes.ARMOR_TOUGHNESS) == 0
                        && player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) == 0,
                "Armor must not add toughness or knockback resistance");
    }

    private static AstralObserversArmorItem[] armor() {
        return new AstralObserversArmorItem[]{ItemRegistry.ASTRAL_OBSERVERS_HOOD.get(), ItemRegistry.ASTRAL_OBSERVERS_COAT.get(),
                ItemRegistry.ASTRAL_OBSERVERS_LEGGINGS.get(), ItemRegistry.ASTRAL_OBSERVERS_BOOTS.get()};
    }

}
