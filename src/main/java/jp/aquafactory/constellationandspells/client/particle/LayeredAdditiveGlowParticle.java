package jp.aquafactory.constellationandspells.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.aquafactory.constellationandspells.particle.AdditiveGlowParticleOptions;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.NotNull;

public final class LayeredAdditiveGlowParticle extends AdditiveGlowParticle {
    private static final float HALO_BRIGHTNESS = 0.25F;

    private final TextureAtlasSprite coreSprite;
    private final TextureAtlasSprite haloSprite;

    private LayeredAdditiveGlowParticle(ClientLevel level, double x, double y, double z,
                                       double xd, double yd, double zd,
                                       AdditiveGlowParticleOptions options, SpriteSet sprites, Preset preset) {
        super(level, x, y, z, xd, yd, zd, options, sprites, preset);
        // JSON の2枚はアニメーションではなく、同じ粒子で位置・回転・寿命を共有する描画層。
        coreSprite = sprites.get(0, 1);
        haloSprite = sprites.get(1, 1);
        setSprite(coreSprite);
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTicks) {
        super.render(buffer, camera, partialTicks);

        float red = rCol;
        float green = gCol;
        float blue = bCol;
        try {
            setSprite(haloSprite);
            // alpha を弱めると shader の破棄閾値へ先に達するため、光量は RGB で抑える。
            rCol = red * HALO_BRIGHTNESS;
            gCol = green * HALO_BRIGHTNESS;
            bCol = blue * HALO_BRIGHTNESS;
            super.render(buffer, camera, partialTicks);
        } finally {
            setSprite(coreSprite);
            rCol = red;
            gCol = green;
            bCol = blue;
        }
    }

    public static final class Provider implements ParticleProvider<AdditiveGlowParticleOptions> {
        private final SpriteSet sprites;
        private final Preset preset;

        public Provider(SpriteSet sprites, Preset preset) {
            this.sprites = sprites;
            this.preset = preset;
        }

        @Override
        public Particle createParticle(@NotNull AdditiveGlowParticleOptions options, @NotNull ClientLevel level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd) {
            return new LayeredAdditiveGlowParticle(level, x, y, z, xd, yd, zd, options, sprites, preset);
        }
    }
}
