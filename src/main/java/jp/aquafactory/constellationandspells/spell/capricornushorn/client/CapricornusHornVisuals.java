package jp.aquafactory.constellationandspells.spell.capricornushorn.client;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.particle.ZodiacParticleOptions;
import jp.aquafactory.constellationandspells.registry.ParticleRegistry;
import jp.aquafactory.constellationandspells.spell.capricornushorn.CapricornusHorn;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = ConstellationnSpells.MODID, value = Dist.CLIENT)
public final class CapricornusHornVisuals {
    private static final int APPEAR_TICKS = 5;
    private static final int SHRINK_TICKS = 6;
    private static final int SCATTER_TICK = CapricornusHorn.LIFETIME - 3;
    private static final Map<UUID, Long> LAST_AGE = new WeakHashMap<>();

    private CapricornusHornVisuals() {}

    static float scale(float age) {
        if (age < APPEAR_TICKS) {
            float remaining = 1 - age / APPEAR_TICKS;
            return 1 + 0.18f * remaining * remaining;
        }

        float progress = Mth.clamp((age - (CapricornusHorn.LIFETIME - SHRINK_TICKS)) / SHRINK_TICKS, 0, 1);
        return 1 - progress * progress * (3 - 2 * progress);
    }

    static float rotation(float age) {
        return age * 18;
    }

    @SubscribeEvent
    public static void tick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof CapricornusHorn horn) || !(horn.level() instanceof ClientLevel level)) return;

        long age = horn.age();
        if (horn.radius() <= 0 || age < 0 || age >= CapricornusHorn.LIFETIME) return;

        // 時刻同期を挟んでも、一つの角笛から同じ粒子を二度出さない.
        var id = horn.getUUID();
        long previous = LAST_AGE.getOrDefault(id, -1L);
        if (age <= previous) return;
        LAST_AGE.put(id, age);

        if (previous < 0 && age < APPEAR_TICKS) {
            flash(level, horn.position(), age);
        }
        if (previous < SCATTER_TICK && age >= SCATTER_TICK) {
            scatter(level, horn.position());
        }
    }

    private static void flash(ClientLevel level, Vec3 center, float age) {
        // バニラの固定表示モデルで細くなっている端に、角笛の回転と拡縮を合わせる.
        Vec3 mouth = new Vec3(-0.35, -0.1, 0).scale(scale(age)).yRot(rotation(age) * Mth.DEG_TO_RAD);
        Vec3 position = center.add(mouth);
        var particle = ZodiacParticleOptions.glow(ParticleRegistry.ADDITIVE_GLOWING_SPARK.get(), true, 0.16f, 6, 1);
        level.addParticle(particle, position.x, position.y, position.z, mouth.x * 0.12, 0.02, mouth.z * 0.12);
    }

    private static void scatter(ClientLevel level, Vec3 center) {
        var spark = ZodiacParticleOptions.glow(ParticleRegistry.ADDITIVE_SPARK.get(), true, 0.085f);
        var rhombus = ZodiacParticleOptions.glow(ParticleRegistry.ADDITIVE_RHOMBUS.get(), true, 0.068f);
        float phase = level.random.nextFloat() * Mth.TWO_PI;
        for (int i = 0; i < 6; i++) {
            float angle = phase + i * (Mth.TWO_PI / 6);
            double x = Mth.cos(angle);
            double z = Mth.sin(angle);
            float speed = 0.025f + level.random.nextFloat() * 0.035f;
            double ySpeed = 0.025 + level.random.nextDouble() * 0.045;
            level.addParticle(i % 3 == 0 ? rhombus : spark,
                    center.x + x * 0.12, center.y, center.z + z * 0.12, x * speed, ySpeed, z * speed);
        }
    }
}
