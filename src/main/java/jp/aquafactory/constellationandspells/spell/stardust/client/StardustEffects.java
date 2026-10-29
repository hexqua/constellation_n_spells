package jp.aquafactory.constellationandspells.spell.stardust.client;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.particle.AdditiveGlowParticleOptions;
import jp.aquafactory.constellationandspells.registry.ParticleRegistry;
import jp.aquafactory.constellationandspells.spell.stardust.StardustImpactPayload;
import jp.aquafactory.constellationandspells.spell.stardust.StardustProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.joml.Vector3f;

@EventBusSubscriber(modid = ConstellationnSpells.MODID, value = Dist.CLIENT)
public final class StardustEffects {
    private static final DustParticleOptions NIGHT = new DustParticleOptions(new Vector3f(0x1F / 255f, 0x2E / 255f, 0x6E / 255f), 0.6f);
    private static final double RENDER_DISTANCE_SQUARED = 48 * 48;

    private StardustEffects() {}

    @SubscribeEvent
    public static void tick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof StardustProjectile projectile) || !(projectile.level() instanceof ClientLevel level)) return;
        var start = new Vec3(projectile.xo, projectile.yo, projectile.zo);
        var end = projectile.position();
        if (start.distanceToSqr(end) > 16) return;
        // client の予測位置が壁の向こうへ進んでも、軌跡は最初の接触面までに留める。
        end = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, projectile)).getLocation();
        int count = Mth.clamp((int) Math.ceil(start.distanceTo(end) / 0.4), 1, 8);
        for (int i = 0; i < count; i++) {
            var position = start.lerp(end, (i + 0.5) / count);
            if (outsideRenderDistance(position)) continue;
            var random = level.random;
            var velocity = projectile.getDeltaMovement().scale(-0.025).add(0, 0.006, 0);
            boolean gold = random.nextFloat() < 0.35f;
            // 必須の発光粒だけは最低設定でも生成し、装飾には設定による間引きを適用する。
            boolean force = i == 0 || i == count - 1;
            var options = glow(ParticleRegistry.ADDITIVE_SPARK.get(), gold, 0.10f);
            level.addParticle(options, force, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
            level.addParticle(NIGHT, position.x, position.y, position.z, 0, 0.004, 0);
        }
    }

    public static void impact(StardustImpactPayload payload) {
        var level = Minecraft.getInstance().level;
        var position = payload.position();
        if (level == null || outsideRenderDistance(position)) return;
        var incoming = payload.incoming();
        for (int i = 0; i < 14; i++) {
            var random = level.random;
            boolean falling = i >= 6;
            var type = falling ? ParticleRegistry.FALLING_SPARK.get()
                    : i >= 4 ? ParticleRegistry.ADDITIVE_RHOMBUS.get() : ParticleRegistry.ADDITIVE_SPARK.get();
            var velocity = incoming.scale(-0.06).add((random.nextDouble() - 0.5) * 0.4,
                    falling ? 0.1 + random.nextDouble() * 0.3 : (random.nextDouble() - 0.5) * 0.3,
                    (random.nextDouble() - 0.5) * 0.4);
            var options = glow(type, random.nextFloat() < 0.7f, falling ? 0.065f : 0.13f);
            level.addParticle(options, i < 2, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
        }
    }

    private static AdditiveGlowParticleOptions glow(ParticleType<AdditiveGlowParticleOptions> type, boolean gold, float size) {
        return new AdditiveGlowParticleOptions(type, size, gold ? 1 : 0.27f, gold ? 0xDB / 255f : 0.42f,
                gold ? 0x57 / 255f : 0.82f, 2, 12, 4, 0.85f, 1.25f, 0.7f, 0.95f, 0.05f, 0.7f, 0.65f, true);
    }

    private static boolean outsideRenderDistance(Vec3 position) {
        return Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceToSqr(position) > RENDER_DISTANCE_SQUARED;
    }
}
