package jp.aquafactory.constellationandspells.particle;

import net.minecraft.core.particles.ParticleType;

public final class ZodiacParticleOptions {
    private static final float GOLD_RED = 1.0f;
    private static final float GOLD_GREEN = 0xDB / 255f;
    private static final float GOLD_BLUE = 0x57 / 255f;
    private static final float BLUE_RED = 0.27f;
    private static final float BLUE_GREEN = 0.42f;
    private static final float BLUE_BLUE = 0.82f;

    private ZodiacParticleOptions() {}

    public static AdditiveGlowParticleOptions glow(ParticleType<AdditiveGlowParticleOptions> type, boolean gold, float size) {
        return new AdditiveGlowParticleOptions(type, size,
                gold ? GOLD_RED : BLUE_RED, gold ? GOLD_GREEN : BLUE_GREEN, gold ? GOLD_BLUE : BLUE_BLUE,
                2, 12, 4, 0.85f, 1.25f, 0.7f, 0.95f, 0.05f, 0.7f, 0.65f, true);
    }
}
