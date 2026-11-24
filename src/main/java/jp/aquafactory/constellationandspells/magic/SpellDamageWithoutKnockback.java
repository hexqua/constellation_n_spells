package jp.aquafactory.constellationandspells.magic;

import io.redspace.ironsspellbooks.damage.DamageSources;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

@EventBusSubscriber(modid = ConstellationnSpells.MODID)
public final class SpellDamageWithoutKnockback {
    private static final ThreadLocal<Set<LivingEntity>> SUPPRESSED =
            ThreadLocal.withInitial(() -> Collections.newSetFromMap(new IdentityHashMap<>()));

    private SpellDamageWithoutKnockback() {}

    public static boolean apply(LivingEntity target, float damage, DamageSource source) {
        var suppressed = SUPPRESSED.get();
        boolean added = suppressed.add(target);
        try {
            return DamageSources.applyDamage(target, damage, source);
        } finally {
            // hurt が無敵時間などで終了しても、次の攻撃まで抑制を残さない。
            if (added) suppressed.remove(target);
        }
    }

    @SubscribeEvent
    public static void knockback(LivingKnockBackEvent event) {
        if (SUPPRESSED.get().contains(event.getEntity())) event.setCanceled(true);
    }
}
