package jp.aquafactory.constellationandspells.registry;

import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class MagicRegistry {
    public static final ResourceKey<DamageType> ZODIAC_MAGIC = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "zodiac_magic"));
    public static final ResourceKey<UpgradeOrbType> ZODIAC_POWER = ResourceKey.create(
            UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY,
            ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "zodiac_power"));

    private MagicRegistry() {
    }

    public static void bootstrapDamageTypes(BootstrapContext<DamageType> context) {
        context.register(ZODIAC_MAGIC,
                new DamageType("zodiac_spell", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0));
    }

    public static void bootstrapUpgradeOrbTypes(BootstrapContext<UpgradeOrbType> context) {
        context.register(ZODIAC_POWER, new UpgradeOrbType(AttributeRegistry.ZODIAC_SPELL_POWER,
                0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, ItemRegistry.ZODIAC_UPGRADE_ORB));
    }
}
