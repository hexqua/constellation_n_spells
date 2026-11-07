package jp.aquafactory.constellationandspells.spell.horoscope;

import jp.aquafactory.constellationandspells.particle.AdditiveGlowParticleOptions;
import jp.aquafactory.constellationandspells.particle.ZodiacParticleOptions;
import jp.aquafactory.constellationandspells.registry.ParticleRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

final class HoroscopeEffects {
    private static final double TAU = Math.PI * 2;
    private static final int CASTING_INTERVAL = 2;
    private static final int SPIRAL_PARTICLES = 3;
    private static final int COMPLETION_PARTICLES = 16;

    private HoroscopeEffects() {}

    static void casting(ServerLevel level, LivingEntity caster) {
        if (caster.tickCount % CASTING_INTERVAL != 0) return;
        double radius = caster.getBbWidth() * 0.5 + 0.35;
        double phase = caster.tickCount * 0.24;
        for (int i = 0; i < SPIRAL_PARTICLES; i++) {
            double height = (i + 0.5) / SPIRAL_PARTICLES;
            double angle = phase + height * TAU;
            var offset = new Vec3(Math.cos(angle) * radius, caster.getBbHeight() * height, Math.sin(angle) * radius);
            var velocity = new Vec3(-Math.sin(angle) * 0.025, 0.025, Math.cos(angle) * 0.025);
            var options = ZodiacParticleOptions.glow(ParticleRegistry.ADDITIVE_GLOWING_SPARK.get(), i % 2 == 0, 0.09f);
            send(level, options, caster.position().add(offset), velocity);
        }
    }

    static void complete(ServerLevel level, LivingEntity caster) {
        var center = caster.position().add(0, caster.getBbHeight() * 0.65, 0);
        double radius = caster.getBbWidth() * 0.5 + 0.55;
        for (int i = 0; i < COMPLETION_PARTICLES; i++) {
            double angle = TAU * i / COMPLETION_PARTICLES;
            var offset = new Vec3(Math.cos(angle) * radius, (i % 3 - 1) * caster.getBbHeight() * 0.25,
                    Math.sin(angle) * radius);
            var type = i % 4 == 0 ? ParticleRegistry.ADDITIVE_RHOMBUS.get() : ParticleRegistry.ADDITIVE_GLOWING_SPARK.get();
            var options = ZodiacParticleOptions.glow(type, i % 3 != 0, 0.12f);
            // 既存粒子の減衰を考慮し、寿命の前半で胸元へ近づく初速にする。
            send(level, options, center.add(offset), offset.scale(-0.16));
        }
    }

    private static void send(ServerLevel level, AdditiveGlowParticleOptions options, Vec3 position, Vec3 velocity) {
        // count=0 は offset を初速として送るため、収束方向に乱数が加わらない。
        level.sendParticles(options, position.x, position.y, position.z, 0, velocity.x, velocity.y, velocity.z, 1);
    }
}
