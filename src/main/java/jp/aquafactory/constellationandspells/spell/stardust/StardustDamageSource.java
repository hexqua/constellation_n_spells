package jp.aquafactory.constellationandspells.spell.stardust;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public final class StardustDamageSource extends SpellDamageSource {
    public StardustDamageSource(Entity projectile, Entity attacker, AbstractSpell spell) {
        super(projectile, attacker, null, spell);
    }

    @Override
    public boolean is(@NotNull TagKey<DamageType> tag) {
        // 学校共通の DamageType に付けると、将来の Zodiac 魔法まで無敵時間を無視してしまう。
        return tag.equals(DamageTypeTags.BYPASSES_COOLDOWN) || super.is(tag);
    }
}
