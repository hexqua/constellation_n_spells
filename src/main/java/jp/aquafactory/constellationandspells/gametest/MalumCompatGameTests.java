package jp.aquafactory.constellationandspells.gametest;

import com.sammy.malum.common.data.component.pouch.SoulwovenPouchContentsComponent;
import com.sammy.malum.registry.common.MalumAttachmentTypes;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
@SuppressWarnings("unused")
public final class MalumCompatGameTests {
    @GameTest(template = "gametest/basic_floor")
    public static void staffMeleeExposesSoul(GameTestHelper helper) {
        if (ModList.get().isLoaded("malum")) Scenarios.melee(helper);
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void staffAcceptsAndAppliesMagicWeaponEnchantments(GameTestHelper helper) {
        if (ModList.get().isLoaded("malum")) Scenarios.enchantments(helper);
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void pouchStoresStardustShardsEfficiently(GameTestHelper helper) {
        if (ModList.get().isLoaded("malum")) Scenarios.pouch(helper);
        helper.succeed();
    }

    // Malum 不在の標準構成でも GameTest の探索を成立させるため、依存型のロードを遅延する。
    private static final class Scenarios {
        private static void melee(GameTestHelper helper) {
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            var target = helper.spawn(EntityType.COW, 2, 1, 2);
            var control = helper.spawn(EntityType.COW, 4, 1, 2);
            target.setNoAi(true);
            control.setNoAi(true);
            try {
                helper.assertTrue(!target.getData(MalumAttachmentTypes.LIVING_SOUL_INFO).shouldDropSpirits(),
                        "The target must start with an unexposed soul");
                player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STICK));
                player.tick();
                float controlHealth = control.getHealth();
                player.attack(control);
                helper.assertTrue(control.getHealth() < controlHealth
                                && !control.getData(MalumAttachmentTypes.LIVING_SOUL_INFO).shouldDropSpirits(),
                        "An ordinary melee hit must damage the control without exposing its soul");

                player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemRegistry.CELESTIAL_SURVEY_STAFF.get()));
                player.tick();
                float health = target.getHealth();
                player.attack(target);
                helper.assertTrue(target.isAlive() && target.getHealth() < health
                                && target.getData(MalumAttachmentTypes.LIVING_SOUL_INFO).shouldDropSpirits(),
                        "A nonlethal staff melee hit must expose the soul through Malum's damage listener");
            } finally {
                target.discard();
                control.discard();
                player.discard();
            }
        }

        private static void enchantments(GameTestHelper helper) {
            var registry = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            var magicDamage = helper.getLevel().registryAccess().registryOrThrow(Registries.ATTRIBUTE)
                    .getHolderOrThrow(ResourceKey.create(Registries.ATTRIBUTE,
                            ResourceLocation.fromNamespaceAndPath("lodestone", "magic_damage")));
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            try {
                player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemRegistry.CELESTIAL_SURVEY_STAFF.get()));
                player.tick();
                double speed = player.getAttributeValue(Attributes.ATTACK_SPEED);
                double magic = player.getAttributeValue(magicDamage);
                for (var name : new String[]{"animated", "haunted"}) {
                    var enchantment = registry.getHolderOrThrow(ResourceKey.create(Registries.ENCHANTMENT,
                            ResourceLocation.fromNamespaceAndPath("malum", name)));
                    var staff = new ItemStack(ItemRegistry.CELESTIAL_SURVEY_STAFF.get());
                    helper.assertTrue(staff.supportsEnchantment(enchantment) && staff.isPrimaryItemFor(enchantment),
                            "The staff must accept Malum " + name + " through both anvil and enchanting table eligibility");
                    staff.enchant(enchantment, 2);
                    player.setItemSlot(EquipmentSlot.MAINHAND, staff);
                    player.tick();
                    helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_SPEED)
                                    - speed - (name.equals("animated") ? 0.3 : 0)) < 0.000001
                                    && Math.abs(player.getAttributeValue(magicDamage)
                                    - magic - (name.equals("haunted") ? 2 : 0)) < 0.000001,
                            "Equipping the enchanted staff must apply only the expected Malum attribute bonus: " + name);
                }
                player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemRegistry.CELESTIAL_SURVEY_STAFF.get()));
                player.tick();
                helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_SPEED) - speed) < 0.000001
                                && Math.abs(player.getAttributeValue(magicDamage) - magic) < 0.000001,
                        "Replacing the enchanted staff must remove Malum's attribute bonuses");
            } finally {
                player.discard();
            }
        }

        private static void pouch(GameTestHelper helper) {
            var shards = SoulwovenPouchContentsComponent.EMPTY.mutable();
            var ordinary = SoulwovenPouchContentsComponent.EMPTY.mutable();
            int storedShards = 0;
            int storedOrdinary = 0;
            // 満杯まで挿入し、タグ所属ではなく収納可能数と保持された内容を検証する。
            for (int i = 0; i < 65; i++) {
                storedShards += shards.tryInsert(new ItemStack(ItemRegistry.STARDUST_SHARD.get(), 64));
                storedOrdinary += ordinary.tryInsert(new ItemStack(Items.COBBLESTONE, 64));
            }
            helper.assertTrue(storedOrdinary > 0 && storedShards == storedOrdinary * 8,
                    "A Soulwoven Pouch must store eight times as many stardust shards as ordinary stackable items");
            var contents = shards.immutable();
            helper.assertTrue(contents.getItems().stream().allMatch(stack -> stack.is(ItemRegistry.STARDUST_SHARD.get()))
                            && contents.getItems().stream().mapToInt(ItemStack::getCount).sum() == storedShards,
                    "The pouch must retain every accepted shard when its contents are committed");
            helper.assertTrue(contents.mutable().tryInsert(new ItemStack(ItemRegistry.STARDUST_SHARD.get())) == 0,
                    "The committed full pouch must reject additional shards");
        }
    }
}
