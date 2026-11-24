package jp.aquafactory.constellationandspells.spell.virgoheal;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.EffectRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;

public final class VirgoHealMobility extends MobEffect {
    private static final ResourceLocation MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "virgo_heal_mobility");
    private static final double NORMAL_CASTING_SPEED = 1.8;
    private static final int EFFECT_TICKS = 6;

    public VirgoHealMobility() {
        super(MobEffectCategory.BENEFICIAL, 0x5afa75);
    }

    static void refresh(LivingEntity caster) {
        var effect = caster.getEffect(EffectRegistry.VIRGO_HEAL_MOBILITY);
        if (effect == null || effect.getDuration() <= 2) {
            caster.addEffect(new MobEffectInstance(EffectRegistry.VIRGO_HEAL_MOBILITY, EFFECT_TICKS, 0,
                    false, false, true));
        }
        if (caster.hasEffect(EffectRegistry.VIRGO_HEAL_MOBILITY)) {
            updateModifier(caster);
        }
    }

    private static void updateModifier(LivingEntity caster) {
        var attribute = caster.getAttribute(AttributeRegistry.CASTING_MOVESPEED);
        if (attribute == null) {
            return;
        }

        double base = attribute.getBaseValue();
        double baseMultiplier = 1.0;
        double totalMultiplier = 1.0;
        for (var modifier : attribute.getModifiers()) {
            if (modifier.id().equals(MODIFIER_ID)) {
                continue;
            }
            switch (modifier.operation()) {
                case ADD_VALUE -> base += modifier.amount();
                case ADD_MULTIPLIED_BASE -> baseMultiplier += modifier.amount();
                case ADD_MULTIPLIED_TOTAL -> totalMultiplier *= 1.0 + modifier.amount();
            }
        }

        // ADD_VALUE は後続の倍率も受けるため、等速になるのに必要な加算値へ逆算する.
        double multiplier = baseMultiplier * totalMultiplier;
        double bonus = multiplier > 0.0 ? NORMAL_CASTING_SPEED / multiplier - base : 0.0;
        var previous = attribute.getModifier(MODIFIER_ID);
        if (bonus <= 1.0e-6) {
            if (previous != null) {
                attribute.removeModifier(MODIFIER_ID);
            }
        } else if (previous == null || Math.abs(previous.amount() - bonus) > 1.0e-6) {
            attribute.addOrUpdateTransientModifier(
                    new AttributeModifier(MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Override
    public void removeAttributeModifiers(@NotNull AttributeMap attributes) {
        super.removeAttributeModifiers(attributes);
        var attribute = attributes.getInstance(AttributeRegistry.CASTING_MOVESPEED);
        if (attribute != null) {
            attribute.removeModifier(MODIFIER_ID);
        }
    }
}
