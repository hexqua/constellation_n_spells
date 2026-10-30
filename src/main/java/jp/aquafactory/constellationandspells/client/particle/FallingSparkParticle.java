package jp.aquafactory.constellationandspells.client.particle;

import jp.aquafactory.constellationandspells.client.render.AdditiveParticleRenderType;
import jp.aquafactory.constellationandspells.particle.AdditiveGlowParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class FallingSparkParticle extends TextureSheetParticle {
    private final float startSize;
    private final int initialLifetime;
    private boolean bounced;

    private FallingSparkParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
                                 AdditiveGlowParticleOptions options, SpriteSet sprites) {
        super(level, x, y, z);
        startSize = options.size() * Mth.lerp(random.nextFloat(), 0.8f, 1.2f);
        quadSize = startSize;
        lifetime = 24 + random.nextInt(11);
        initialLifetime = lifetime;
        gravity = 0.6f;
        friction = 0.96f;
        hasPhysics = true;
        setSize(startSize * 2, startSize * 2);
        setParticleSpeed(xd, yd, zd);
        setColor(options.red(), options.green(), options.blue());
        pickSprite(sprites);
        alpha = 0.8f;
    }

    @Override
    public void move(double x, double y, double z) {
        var requested = new Vec3(x, y, z);
        var actual = Entity.collideBoundingBox(null, requested, getBoundingBox(), level, List.of());
        setBoundingBox(getBoundingBox().move(actual));
        setLocationFromBoundingbox();
        double vx = x == actual.x ? x : 0;
        double vz = z == actual.z ? z : 0;
        // vanilla の接地による恒久停止を避け、参考 MOD と同じ一度だけの反発を行う。
        if (y < 0 && y != actual.y) {
            if (bounced) { remove(); return; }
            bounced = true;
            lifetime = Math.min(lifetime, age + 12);
            setParticleSpeed(vx * 0.5, -y * 0.25, vz * 0.5);
        } else {
            setParticleSpeed(vx, y == actual.y ? y : 0, vz);
        }
    }

    @Override
    public void tick() {
        super.tick();
        alpha = 0.8f * Mth.clamp((lifetime - age) / (bounced ? 12.0f : initialLifetime / 3.0f), 0, 1);
    }

    @Override
    public float getQuadSize(float partialTick) {
        float fade = (lifetime - age - partialTick) / (bounced ? 12.0f : initialLifetime / 3.0f);
        return startSize * Mth.lerp(Mth.clamp(fade, 0, 1), 0.25f, 1);
    }

    @Override
    public int getLightColor(float partialTick) { return LightTexture.FULL_BRIGHT; }

    @Override
    public @NotNull ParticleRenderType getRenderType() { return AdditiveParticleRenderType.PARTICLE_SHEET_ADDITIVE; }

    public static final class Provider implements ParticleProvider<AdditiveGlowParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(@NotNull AdditiveGlowParticleOptions options, @NotNull ClientLevel level,
                                       double x, double y, double z, double xd, double yd, double zd) {
            return new FallingSparkParticle(level, x, y, z, xd, yd, zd, options, sprites);
        }
    }
}
