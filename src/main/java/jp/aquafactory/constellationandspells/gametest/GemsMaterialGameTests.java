package jp.aquafactory.constellationandspells.gametest;

import com.mojang.authlib.GameProfile;
import io.redspace.ironsjewelry.core.bonuses.AttributeBonusType;
import io.redspace.ironsjewelry.core.data.AttributeInstance;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PartDefinition;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.registry.BonusTypeRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.AttributeRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.SlotContext;

import java.util.Map;
import java.util.UUID;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
// GameTest がアノテーションから探索して呼び出す。
@SuppressWarnings("unused")
public final class GemsMaterialGameTests {
    @GameTest(template = "gametest/basic_floor")
    public static void zodiacRuneMatchesSchoolRuneQualityScaling(GameTestHelper helper) {
        if (ModList.get().isLoaded("irons_jewelry")) {
            GemsScenario.verify(helper);
        }
        helper.succeed();
    }

    // Jewelry のない標準構成でもテストを探索できるよう、依存型を使う処理のロードを遅延する。
    private static final class GemsScenario {
        private static void verify(GameTestHelper helper) {
            var access = helper.getLevel().registryAccess();
            var materials = IronsJewelryRegistries.materialRegistry(access);
            var parts = IronsJewelryRegistries.partRegistry(access);
            var patterns = IronsJewelryRegistries.patternRegistry(access);
            Holder<MaterialDefinition> zodiac = materials.getHolderOrThrow(ResourceKey.create(
                    IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY,
                    ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "zodiac_rune")));
            Holder<MaterialDefinition> fire = materials.getHolderOrThrow(ResourceKey.create(
                    IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY, id("irons_spellbooks", "rune_fire")));
            Holder<PartDefinition> band = parts.getHolderOrThrow(ResourceKey.create(
                    IronsJewelryRegistries.Keys.PART_REGISTRY_KEY, id("irons_spellbooks", "band_rune_inscribed")));
            Holder<PartDefinition> inscription = parts.getHolderOrThrow(ResourceKey.create(
                    IronsJewelryRegistries.Keys.PART_REGISTRY_KEY, id("irons_spellbooks", "rune_inscription")));
            Holder<PatternDefinition> pattern = patterns.getHolderOrThrow(ResourceKey.create(
                    IronsJewelryRegistries.Keys.PATTERN_REGISTRY_KEY, id("irons_spellbooks", "rune_inscribed_ring")));
            helper.assertTrue(inscription.value().canUseMaterial(zodiac),
                    "Rune inscription must accept the generated Zodiac material tag");
            var ingredient = zodiac.value().ingredient().orElseThrow();
            helper.assertTrue(ingredient.test(new ItemStack(ItemRegistry.ZODIAC_RUNE.get()))
                            && !ingredient.test(new ItemStack(ItemRegistry.STARDUST_SHARD.get())),
                    "Zodiac material must accept only the rune, not stardust shards");
            helper.assertTrue(zodiac.value().quality() == fire.value().quality()
                            && zodiac.value().bonusParameters().size() == 1,
                    "Zodiac rune must match school rune material quality and have only one bonus parameter");
            var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(
                    UUID.fromString("75467478-60cf-4d49-976c-316cb9c2f865"), "ZodiacJewelry"));
            var slot = new SlotContext("ring", player, 0, false, true);
            for (String metal : new String[]{"iron", "gold"}) {
                Holder<MaterialDefinition> metalMaterial = materials.getHolderOrThrow(ResourceKey.create(
                        IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY, id("irons_jewelry", metal)));
                var zodiacRing = new JewelryData(pattern, Map.of(band, metalMaterial, inscription, zodiac));
                var fireRing = new JewelryData(pattern, Map.of(band, metalMaterial, inscription, fire));
                helper.assertTrue(zodiacRing.isValid() && zodiacRing.getBonuses().size() == 1,
                        "Zodiac rune ring must resolve to exactly one bonus");
                var zodiacBonus = zodiacRing.getBonuses().getFirst();
                var fireBonus = fireRing.getBonuses().getFirst();
                var parameterType = ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get();
                AttributeInstance zodiacAttribute = parameterType.resolve(zodiacBonus).orElseThrow();
                AttributeInstance fireAttribute = parameterType.resolve(fireBonus).orElseThrow();
                var bonusType = (AttributeBonusType) BonusTypeRegistry.ATTRIBUTE_BONUS.get();
                var zodiacModifier = bonusType.modifier(zodiacAttribute, slot, zodiacBonus.quality());
                var fireModifier = bonusType.modifier(fireAttribute, slot, fireBonus.quality());
                helper.assertTrue(zodiacAttribute.attribute().equals(AttributeRegistry.ZODIAC_SPELL_POWER)
                                && zodiacModifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                        "Zodiac rune must increase only Zodiac spell power with the school rune operation");
                helper.assertTrue(Math.abs(zodiacModifier.amount() - fireModifier.amount()) < 0.000001
                                && Math.abs(zodiacModifier.amount() - 0.05 * metalMaterial.value().quality()) < 0.000001,
                        "Zodiac spell power must match school rune scaling for each band material quality");
            }
        }

        private static ResourceLocation id(String namespace, String path) {
            return ResourceLocation.fromNamespaceAndPath(namespace, path);
        }
    }
}
