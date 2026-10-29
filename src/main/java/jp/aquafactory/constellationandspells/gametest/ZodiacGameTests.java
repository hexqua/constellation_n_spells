package jp.aquafactory.constellationandspells.gametest;

import com.mojang.authlib.GameProfile;
import io.redspace.ironsspellbooks.api.item.UpgradeData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilMenu;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import jp.aquafactory.constellationandspells.registry.MagicRegistry;
import jp.aquafactory.constellationandspells.registry.AttributeRegistry;
import jp.aquafactory.constellationandspells.registry.SchoolTags;
import jp.aquafactory.constellationandspells.registry.ZodiacSchoolRegistry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.UUID;

import static io.redspace.ironsspellbooks.registries.ItemRegistry.BLANK_RUNE;
import static io.redspace.ironsspellbooks.registries.ItemRegistry.UPGRADE_ORB;
import static io.redspace.ironsspellbooks.util.ModTags.INSCRIBED_RUNES;
import static io.redspace.ironsspellbooks.util.ModTags.SCHOOL_FOCUS;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
// GameTest がアノテーションから探索して呼び出す。
@SuppressWarnings("unused")
public class ZodiacGameTests {
    @GameTest(template = "gametest/basic_floor")
    public static void schoolResolvesStardustAndFocus(GameTestHelper helper) {
        var school = ZodiacSchoolRegistry.ZODIAC.get();
        helper.assertTrue(SchoolRegistry.getSchool(id("zodiac")) == school, "Zodiac school must resolve");
        helper.assertTrue(SpellRegistry.getSpellsForSchool(school).contains(SpellRegistry.REGISTRY.get(id("stardust"))),
                "Zodiac must contain Stardust");
        var focus = new ItemStack(ItemRegistry.STARDUST_SHARD.get());
        helper.assertTrue(focus.is(SchoolTags.ZODIAC_FOCUS) && focus.is(SCHOOL_FOCUS),
                "Stardust shard must be accepted by school and scroll forge focus tags");
        helper.assertTrue(school.isFocus(focus) && SchoolRegistry.getSchoolsFromFocus(focus).contains(school),
                "The focus must resolve to Zodiac");
        helper.assertTrue(!school.isFocus(new ItemStack(ItemRegistry.ZODIAC_RUNE.get())),
                "Zodiac rune must not become an additional focus");
        helper.assertTrue(new ItemStack(ItemRegistry.ZODIAC_RUNE.get()).is(INSCRIBED_RUNES),
                "Zodiac rune must belong to the inscribed rune tag");

        var damage = helper.getLevel().registryAccess().holderOrThrow(MagicRegistry.ZODIAC_MAGIC);
        helper.assertTrue(school.getDamageType().equals(MagicRegistry.ZODIAC_MAGIC),
                "School must reference Zodiac damage");
        helper.assertTrue(damage.is(SchoolTags.ZODIAC_MAGIC) && damage.is(Tags.DamageTypes.IS_MAGIC),
                "Zodiac damage must belong to its school and common magic tags");
        helper.assertTrue(damage.value().msgId().equals("zodiac_spell"),
                "Damage message id must match the existing translation");

        var player = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.fromString("a1020584-0c30-4439-9246-11da20769df1"), "ZodiacAttributes"));
        var zombie = EntityType.ZOMBIE.create(helper.getLevel());
        helper.assertTrue(zombie != null, "Zombie must be created");
        for (var entity : new LivingEntity[]{player, zombie}) {
            helper.assertTrue(entity.getAttribute(AttributeRegistry.ZODIAC_SPELL_POWER) != null
                            && entity.getAttribute(AttributeRegistry.ZODIAC_MAGIC_RESIST) != null,
                    "Players and mobs must have both Zodiac attributes");
            helper.assertTrue(school.getPowerFor(entity) == 1 && school.getResistanceFor(entity) == 1,
                    "Zodiac attributes must default to one");
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void runeAndOrbCanBeCrafted(GameTestHelper helper) {
        var rune = craft(helper, "zodiac_rune", BLANK_RUNE.get(), ItemRegistry.STARDUST_SHARD.get());
        helper.assertTrue(rune.is(ItemRegistry.ZODIAC_RUNE.get()) && rune.getCount() == 1,
                "Rune recipe must produce one Zodiac rune");
        var orb = craft(helper, "zodiac_upgrade_orb", UPGRADE_ORB.get(), ItemRegistry.ZODIAC_RUNE.get());
        helper.assertTrue(orb.is(ItemRegistry.ZODIAC_UPGRADE_ORB.get()) && orb.getCount() == 1,
                "Orb recipe must produce one Zodiac upgrade orb");
        helper.assertTrue(MagicRegistry.ZODIAC_POWER.equals(orb.get(ComponentRegistry.UPGRADE_ORB_TYPE)),
                "Crafted orb must retain its upgrade type component");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void orbUpgradesArmorThroughArcaneAnvil(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.fromString("61861c18-01b0-4718-98f8-49f311da3cc3"), "ZodiacUpgrades"));
        var menu = new ArcaneAnvilMenu(0, player.getInventory(), ContainerLevelAccess.NULL);
        var armor = new ItemStack(Items.IRON_CHESTPLATE);
        for (int count = 1; count <= 2; count++) {
            menu.getSlot(0).set(armor);
            menu.getSlot(1).set(new ItemStack(ItemRegistry.ZODIAC_UPGRADE_ORB.get()));
            menu.createResult();
            armor = menu.getSlot(2).getItem().copy();
            helper.assertTrue(!armor.isEmpty(), "Arcane anvil must accept the Zodiac orb");
            var upgrade = UpgradeData.getUpgradeData(armor);
            var type = helper.getLevel().registryAccess().holderOrThrow(MagicRegistry.ZODIAC_POWER);
            helper.assertTrue(upgrade.upgrades().getOrDefault(type, 0) == count,
                    "Arcane anvil must accumulate Zodiac upgrades");
            double[] amount = {0};
            armor.forEachModifier(EquipmentSlot.CHEST, (attribute, modifier) -> {
                if (attribute.equals(AttributeRegistry.ZODIAC_SPELL_POWER)) {
                    helper.assertTrue(modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                            "Zodiac upgrades must multiply base power");
                    amount[0] += modifier.amount();
                }
            });
            helper.assertTrue(Math.abs(amount[0] - 0.05 * count) < 0.000001,
                    "Each Zodiac upgrade must add five percent base spell power");
            helper.assertTrue(type.value().containerItem().orElseThrow().is(ItemRegistry.ZODIAC_UPGRADE_ORB.get()),
                    "Removing upgrades must return the Zodiac orb");
        }
        helper.succeed();
    }

    private static ItemStack craft(GameTestHelper helper, String recipeName, Item center, Item surrounding) {
        var items = new ArrayList<ItemStack>();
        for (int i = 0; i < 9; i++) {
            items.add(new ItemStack(i == 4 ? center : surrounding));
        }
        var input = CraftingInput.of(3, 3, items);
        var level = helper.getLevel();
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).orElseThrow();
        helper.assertTrue(recipe.id().equals(id(recipeName)), "Crafting grid must select the Zodiac recipe");
        return recipe.value().assemble(input, level.registryAccess());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, path);
    }
}
