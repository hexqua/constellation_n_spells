package jp.aquafactory.constellationandspells.spell.virgoheal;

import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

final class VirgoHealDamageSource extends SpellDamageSource {
    VirgoHealDamageSource(LivingEntity caster, VirgoHealSpell spell) {
        super(caster, caster, null, spell);
    }

    @Override
    public boolean is(@NotNull TagKey<DamageType> tag) {
        return tag.equals(DamageTypeTags.BYPASSES_COOLDOWN)
                || tag.equals(DamageTypeTags.BYPASSES_ARMOR)
                || super.is(tag);
    }
}
