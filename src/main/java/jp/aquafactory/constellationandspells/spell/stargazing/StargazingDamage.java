package jp.aquafactory.constellationandspells.spell.stargazing;

import jp.aquafactory.constellationandspells.magic.SpellDamageWithoutKnockback;
import jp.aquafactory.constellationandspells.registry.SpellRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class StargazingDamage {
    private StargazingDamage() {}

    public static boolean apply(Entity direct, LivingEntity caster, LivingEntity target, float damage) {
        if (target == caster) return false;
        return SpellDamageWithoutKnockback.apply(target, damage, SpellRegistry.STARGAZING.get().getDamageSource(direct, caster));
    }
}
