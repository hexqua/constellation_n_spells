package jp.aquafactory.constellationandspells.spell.stardust;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = ConstellationnSpells.MODID)
public final class StardustBursts {
    public static final int SHOT_COUNT = 5;
    public static final int SHOT_INTERVAL = 3;
    public static final double HALF_ANGLE = Math.toRadians(10);
    private static final Map<ServerLevel, List<Burst>> BURSTS = new IdentityHashMap<>();

    private StardustBursts() {}

    public static void start(ServerLevel level, LivingEntity caster, float damage) {
        var position = caster.getEyePosition().add(0, -0.075, 0);
        var burst = new Burst(caster, position, caster.getLookAngle().normalize(), damage, level.getGameTime());
        if (burst.valid(level)) {
            burst.fire(level);
            BURSTS.computeIfAbsent(level, ignored -> new ArrayList<>()).add(burst);
        }
    }

    public static Vec3 sampleDirection(Vec3 forward, RandomSource random) {
        var right = forward.cross(Math.abs(forward.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0)).normalize();
        var up = right.cross(forward).normalize();
        // cos(theta) を一様にすることで、極や中心へ密集しない立体角一様の分布になる。
        double cosine = 1 - random.nextDouble() * (1 - Math.cos(HALF_ANGLE));
        double sine = Math.sqrt(1 - cosine * cosine);
        double azimuth = random.nextDouble() * Math.PI * 2;
        return forward.scale(cosine).add(right.scale(sine * Math.cos(azimuth))).add(up.scale(sine * Math.sin(azimuth))).normalize();
    }

    @SubscribeEvent
    public static void tick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        var bursts = BURSTS.get(level);
        if (bursts == null) return;
        bursts.removeIf(burst -> {
            if (!burst.valid(level)) return true;
            if (level.getGameTime() >= burst.startedAt + (long) burst.shots * SHOT_INTERVAL) burst.fire(level);
            return burst.shots == SHOT_COUNT;
        });
        if (bursts.isEmpty()) BURSTS.remove(level);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) BURSTS.remove(level);
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event) { BURSTS.clear(); }

    private static final class Burst {
        private final LivingEntity caster;
        private final Vec3 position;
        private final Vec3 forward;
        private final float damage;
        private final long startedAt;
        private int shots;

        private Burst(LivingEntity caster, Vec3 position, Vec3 forward, float damage, long startedAt) {
            this.caster = caster;
            this.position = position;
            this.forward = forward;
            this.damage = damage;
            this.startedAt = startedAt;
        }

        private boolean valid(ServerLevel level) {
            return caster.isAlive() && !caster.isRemoved() && caster.level() == level
                    && level.isPositionEntityTicking(BlockPos.containing(position));
        }

        private void fire(ServerLevel level) {
            var projectile = new StardustProjectile(EntityRegistry.STARDUST.get(), level);
            projectile.setOwner(caster);
            projectile.setPos(position);
            projectile.setDamage(damage);
            projectile.shoot(sampleDirection(forward, level.random));
            level.addFreshEntity(projectile);
            shots++;
        }
    }
}
