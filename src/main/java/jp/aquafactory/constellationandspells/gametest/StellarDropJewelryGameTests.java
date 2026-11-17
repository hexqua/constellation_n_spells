package jp.aquafactory.constellationandspells.gametest;

import com.mojang.authlib.GameProfile;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PartDefinition;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;
import io.redspace.ironsjewelry.utils.Utils;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.COOLDOWN_REDUCTION;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
@SuppressWarnings("unused") // GameTestがアノテーションから探索する。
public final class StellarDropJewelryGameTests {
    @GameTest(template = "gametest/basic_floor")
    public static void gemEquipmentAppliesCooldownAndSlowedImmunity(GameTestHelper helper) {
        if (ModList.get().isLoaded("irons_jewelry")) {
            JewelryScenario.verifyEquipment(helper);
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void actionDurationScalesWithoutOverwritingStrongerHastened(GameTestHelper helper) {
        if (ModList.get().isLoaded("irons_jewelry")) {
            JewelryScenario.verifyHastened(helper);
        }
        helper.succeed();
    }

    // Jewelry のない標準構成では、依存型を使う処理をロードしない。
    private static final class JewelryScenario {
        private static void verifyEquipment(GameTestHelper helper) {
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            var cooldown = Objects.requireNonNull(player.getAttribute(COOLDOWN_REDUCTION));
            cooldown.setBaseValue(2);
            cooldown.addTransientModifier(new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "stellar_drop_test_base"),
                    0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            var slots = CuriosApi.getCuriosInventory(player).orElseThrow()
                    .getStacksHandler("ring").orElseThrow().getStacks();
            var ring = jewelry(helper, "gemset_ring", "gold", "gem_round");
            helper.assertTrue(slots.isItemValid(0, ring), "The ring slot must accept stellar drop jewelry");
            slots.setStackInSlot(0, ring);
            tickEquipment(player);
            helper.assertTrue(Math.abs(player.getAttributeValue(COOLDOWN_REDUCTION) - 2.49) < 0.000001,
                    "A gold gemset ring must multiply the wearer's cooldown attribute by 1.0375");
            slots.setStackInSlot(0, ring.copy());
            tickEquipment(player);
            helper.assertTrue(Math.abs(player.getAttributeValue(COOLDOWN_REDUCTION) - 2.49) < 0.000001,
                    "Re-equipping the ring must not duplicate its cooldown modifier");
            slots.setStackInSlot(0, jewelry(helper, "tearstone_ring", "gold", "gem_tearstone"));
            tickEquipment(player);
            helper.assertTrue(Math.abs(player.getAttributeValue(COOLDOWN_REDUCTION) - 2.4) < 0.000001,
                    "A tearstone ring must remove the gemset ring's cooldown modifier");
            helper.assertTrue(!player.addEffect(new MobEffectInstance(MobEffectRegistry.SLOWED, 100, 3))
                            && !player.hasEffect(MobEffectRegistry.SLOWED),
                    "The stellar drop tearstone ring must prevent Spellbooks Slowed");
            helper.assertTrue(player.addEffect(new MobEffectInstance(MobEffectRegistry.CHILLED, 100)),
                    "Slowed immunity must not grant immunity to unrelated cold effects");
            slots.setStackInSlot(0, ItemStack.EMPTY);
            tickEquipment(player);
            helper.assertTrue(player.addEffect(new MobEffectInstance(MobEffectRegistry.SLOWED, 100, 3)),
                    "Removing the tearstone ring must restore susceptibility to Slowed");
            player.removeAllEffects();

            double originalArmor = player.getAttributeValue(Attributes.ARMOR);
            slots.setStackInSlot(0, jewelry(helper, "barbed_band", "iron", "gem_band_barbed"));
            tickEquipment(player);
            helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ARMOR) - originalArmor - 2.5) < 0.000001,
                    "A stellar drop barbed band must scale its iron attribute using the gem's low quality");
            slots.setStackInSlot(0, ItemStack.EMPTY);
            tickEquipment(player);
            helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ARMOR) - originalArmor) < 0.000001,
                    "Removing the barbed band must restore the original armor");
        }

        private static void verifyHastened(GameTestHelper helper) {
            var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(
                    UUID.fromString("78b922a4-fd6b-4f8b-89c7-f3e5eaf4e47c"), "StellarJewelry"));
            try {
                for (String metal : new String[]{"copper", "silver", "gold", "netherite", "mithril"}) {
                    player.removeAllEffects();
                    var data = JewelryData.get(jewelry(helper, "sharpshooter_loop", metal, "gem_pointy"));
                    var bonus = data.getBonuses().getFirst();
                    var action = ParameterTypeRegistry.ACTION_PARAMETER.get().resolve(bonus).orElseThrow();
                    action.action().apply(helper.getLevel(), bonus.quality(), action.targetSelf(), player, player);
                    var effect = Objects.requireNonNull(player.getEffect(MobEffectRegistry.HASTENED));
                    int expectedTicks = switch (metal) {
                        case "gold" -> 60;
                        case "netherite" -> 100;
                        case "mithril" -> 120;
                        default -> 40;
                    };
                    helper.assertTrue(effect.getAmplifier() == 1 && effect.getDuration() == expectedTicks,
                            "Sharpshooter action must apply Hastened II for clamped quality * 2 seconds: " + metal);
                }
                player.removeAllEffects();
                var data = JewelryData.get(jewelry(helper, "amulet_of_protection", "mithril", "gem_amulet_of_protection"));
                var bonus = data.getBonuses().getFirst();
                var action = ParameterTypeRegistry.ACTION_PARAMETER.get().resolve(bonus).orElseThrow();
                action.action().apply(helper.getLevel(), bonus.quality(), action.targetSelf(), player, player);
                helper.assertTrue(Objects.requireNonNull(player.getEffect(MobEffectRegistry.HASTENED)).getDuration() == 120,
                        "A high-quality protection amulet must cap Hastened at six seconds");

                player.removeAllEffects();
                player.addEffect(new MobEffectInstance(MobEffectRegistry.HASTENED, 40, 7));
                action.action().apply(helper.getLevel(), bonus.quality(), action.targetSelf(), player, player);
                var effect = Objects.requireNonNull(player.getEffect(MobEffectRegistry.HASTENED));
                helper.assertTrue(effect.getAmplifier() == 7 && effect.getDuration() == 40,
                        "The jewelry action must neither weaken nor extend stronger Hastened");
                // FakePlayerは効果をtickしないため、保存された低レベル効果への切替だけ直接進める。
                for (int tick = 0; tick < 40; tick++) {
                    effect.tick(player, () -> {});
                }
                helper.assertTrue(effect.getAmplifier() == 1 && effect.getDuration() == 80,
                        "After stronger Hastened expires, only the remaining four seconds of Hastened II may resume");
            } finally {
                player.removeAllEffects();
            }
        }

        private static ItemStack jewelry(GameTestHelper helper, String patternName, String baseMaterial, String stellarPart) {
            var access = helper.getLevel().registryAccess();
            var material = Utils.getMaterialForIngredient(access, new ItemStack(ItemRegistry.STARDUST_SHARD.get())).orElseThrow();
            helper.assertTrue(material.unwrapKey().orElseThrow().location().equals(
                            ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "stellar_drop")),
                    "Stardust shards must resolve to the stellar_drop material");
            var base = IronsJewelryRegistries.materialRegistry(access).getHolder(
                    ResourceLocation.fromNamespaceAndPath(baseMaterial.equals("mithril") ? "irons_spellbooks" : "irons_jewelry", baseMaterial)).orElseThrow();
            var pattern = IronsJewelryRegistries.patternRegistry(access).getHolder(
                    ResourceLocation.fromNamespaceAndPath("irons_jewelry", patternName)).orElseThrow();
            var parts = new HashMap<Holder<PartDefinition>, Holder<MaterialDefinition>>();
            for (var ingredient : pattern.value().partTemplate()) {
                var part = ingredient.part();
                var selected = part.unwrapKey().orElseThrow().location().getPath().equals(stellarPart) ? material : base;
                helper.assertTrue(part.value().canUseMaterial(selected),
                        "The jewelry part must accept its selected material through generated tags: " + stellarPart);
                parts.put(part, selected);
            }
            var stack = new ItemStack(pattern.value().jewelryType().item());
            var data = new JewelryData(pattern, parts);
            helper.assertTrue(data.isValid() && !data.getBonuses().isEmpty(),
                    "Stellar drop jewelry must resolve to usable bonuses: " + patternName);
            JewelryData.set(stack, data);
            return stack;
        }

        private static void tickEquipment(Player player) {
            player.tick();
            NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(player));
        }
    }
}
