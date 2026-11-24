package jp.aquafactory.constellationandspells.spell.capricornushorn.client;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.particle.AdditiveGlowParticleOptions;
import jp.aquafactory.constellationandspells.registry.ParticleRegistry;
import jp.aquafactory.constellationandspells.spell.capricornushorn.CapricornusHorn;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.util.FastColor.ARGB32;
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
    static final int SILVER = 0xE4EDF5;
    static final int EARTH = 0xAE8058;
    private static final int APPEAR_TICKS = 5;
    private static final int SHRINK_TICKS = 6;
    private static final int RIM_SPARK_INTERVAL = 2;
    private static final int LAST_RIM_SPARK_TICK = 8;
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
        float remaining = 1 - Mth.clamp(age / CapricornusHorn.LIFETIME, 0.0f, 1.0f);
        // 出現の勢いを回転に乗せ、縮んで消える直前には角笛の向きが落ち着くようにする。
        return 360.0f * (1 - remaining * remaining * remaining);
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
        // 時刻の補正で複数の発生時刻をまたいでも、直近の波の位置から一度だけ散らす。
        long rimTick = age - age % RIM_SPARK_INTERVAL;
        if (rimTick >= RIM_SPARK_INTERVAL && age <= LAST_RIM_SPARK_TICK + 1 && previous < rimTick) {
            float radius = CapricornusHornRenderer.firstWaveRadius(rimTick, horn.radius());
            scatterAlongWave(level, horn.position(), radius);
        }
        if (previous < SCATTER_TICK && age >= SCATTER_TICK) {
            scatter(level, horn.position());
        }
    }

    private static void flash(ClientLevel level, Vec3 center, float age) {
        float yaw = rotation(age) * Mth.DEG_TO_RAD;
        float scale = scale(age);
        var forward = new Vec3(1, 0, 0).yRot(yaw);
        var side = new Vec3(0, 0, 1).yRot(yaw);

        // 吹き口より外側から閃光を上へ逃がし、正面からも見えるよう両脇に火花を出す。
        var position = center.add(forward.scale(0.56 * scale)).add(0, 0.17 * scale, 0);
        var flash = particle(ParticleRegistry.ADDITIVE_GLOWING_SPARK.get(), SILVER, 0.24f, 6, 1, 2);
        level.addParticle(flash, position.x, position.y, position.z, forward.x * 0.09, 0.065, forward.z * 0.09);

        var spark = particle(ParticleRegistry.ADDITIVE_SPARK.get(), SILVER, 0.09f, 8, 2, 1);
        for (int direction = -1; direction <= 1; direction += 2) {
            var flank = position.add(side.scale(direction * 0.20 * scale)).add(0, -0.12 * scale, 0);
            var velocity = forward.scale(0.06).add(side.scale(direction * 0.085));
            level.addParticle(spark, flank.x, flank.y, flank.z, velocity.x, 0.035, velocity.z);
        }
    }

    private static void scatterAlongWave(ClientLevel level, Vec3 center, float rim) {
        var silver = particle(ParticleRegistry.ADDITIVE_SPARK.get(), SILVER, 0.075f, 22, 4, 0);
        var earth = particle(ParticleRegistry.ADDITIVE_SPARK.get(), EARTH, 0.095f, 13, 3, 0);
        int count = 5;
        float phase = level.random.nextFloat() * Mth.TWO_PI;
        for (int i = 0; i < count; i++) {
            float angle = phase + i * Mth.TWO_PI / count;
            float x = Mth.cos(angle);
            float z = Mth.sin(angle);
            double distance = rim * (0.70 + level.random.nextDouble() * 0.27);
            boolean isSilver = i % 2 == 0;
            double height = isSilver ? (level.random.nextDouble() - 0.35) * 0.7 : -0.25;
            double outward = isSilver ? 0.025 : 0.06;
            double vertical = isSilver ? 0.015 : -0.065;
            level.addParticle(isSilver ? silver : earth, center.x + x * distance, center.y + height,
                    center.z + z * distance, x * outward, vertical, z * outward);
        }
    }

    private static void scatter(ClientLevel level, Vec3 center) {
        var spark = particle(ParticleRegistry.ADDITIVE_SPARK.get(), SILVER, 0.085f, 12, 4, 1);
        var rhombus = particle(ParticleRegistry.ADDITIVE_RHOMBUS.get(), EARTH, 0.068f, 12, 4, 1);
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

    private static AdditiveGlowParticleOptions particle(ParticleType<AdditiveGlowParticleOptions> type, int color,
                                                       float size, int lifetime, int variance, int whitenTicks) {
        return new AdditiveGlowParticleOptions(type, size, ARGB32.red(color) / 255f, ARGB32.green(color) / 255f,
                ARGB32.blue(color) / 255f, whitenTicks, lifetime, variance,
                0.85f, 1.15f, 0.65f, 0.90f, 0.05f, 0.50f, 0.55f, true);
    }
}
