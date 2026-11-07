package jp.aquafactory.constellationandspells.client;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.client.particle.AdditiveGlowParticle;
import jp.aquafactory.constellationandspells.client.particle.AdditiveRhombusParticle;
import jp.aquafactory.constellationandspells.client.particle.FallingSparkParticle;
import jp.aquafactory.constellationandspells.client.particle.LayeredAdditiveGlowParticle;
import jp.aquafactory.constellationandspells.registry.ParticleRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = ConstellationnSpells.MODID, value = Dist.CLIENT)
public final class ParticleClientRegistration {
    private ParticleClientRegistration() {}

    @SubscribeEvent
    public static void particles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegistry.ADDITIVE_SPARK.get(), sprites -> new AdditiveGlowParticle.Provider(sprites, AdditiveGlowParticle.Preset.SPARK));
        event.registerSpriteSet(ParticleRegistry.ADDITIVE_GLOWING_SPARK.get(), sprites -> new LayeredAdditiveGlowParticle.Provider(sprites, AdditiveGlowParticle.Preset.SPARK));
        event.registerSpriteSet(ParticleRegistry.ADDITIVE_RHOMBUS.get(), sprites -> new AdditiveRhombusParticle.Provider(sprites, AdditiveRhombusParticle.Preset.RHOMBUS));
        event.registerSpriteSet(ParticleRegistry.FALLING_SPARK.get(), FallingSparkParticle.Provider::new);
    }
}
