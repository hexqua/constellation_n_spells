package jp.aquafactory.constellationandspells.spell.stargazing;

import io.redspace.ironsspellbooks.damage.DamageSources;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.SpellRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

@EventBusSubscriber(modid = ConstellationnSpells.MODID)
public final class StargazingDamage {
    private static final Set<LivingEntity> SUPPRESSED = Collections.newSetFromMap(new IdentityHashMap<>());

    private StargazingDamage() {}

    public static boolean apply(Entity direct, LivingEntity caster, LivingEntity target, float damage) {
        if (target == caster) return false;
        boolean added = SUPPRESSED.add(target);
        try {
            return DamageSources.applyDamage(target, damage, SpellRegistry.STARGAZING.get().getDamageSource(direct, caster));
        } finally {
            // 無敵時間やイベントキャンセルで hurt が早期終了しても、別の攻撃へ抑制を残さない。
            if (added) SUPPRESSED.remove(target);
        }
    }

    @SubscribeEvent
    public static void knockback(LivingKnockBackEvent event) {
        if (SUPPRESSED.contains(event.getEntity())) event.setCanceled(true);
    }
}
