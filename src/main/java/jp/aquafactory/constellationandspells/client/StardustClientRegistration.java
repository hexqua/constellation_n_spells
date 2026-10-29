package jp.aquafactory.constellationandspells.client;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.client.particle.AdditiveGlowParticle;
import jp.aquafactory.constellationandspells.client.particle.AdditiveRhombusParticle;
import jp.aquafactory.constellationandspells.client.particle.FallingSparkParticle;
import jp.aquafactory.constellationandspells.registry.EntityRegistry;
import jp.aquafactory.constellationandspells.registry.ParticleRegistry;
import jp.aquafactory.constellationandspells.spell.stardust.StardustImpactPayload;
import jp.aquafactory.constellationandspells.spell.stardust.client.StardustEffects;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = ConstellationnSpells.MODID, value = Dist.CLIENT)
public final class StardustClientRegistration {
    private StardustClientRegistration() {}

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) { event.enqueueWork(() -> StardustImpactPayload.setClientHandler(StardustEffects::impact)); }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) { event.registerEntityRenderer(EntityRegistry.STARDUST.get(), NoopRenderer::new); }

    @SubscribeEvent
    public static void particles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegistry.ADDITIVE_SPARK.get(), sprites -> new AdditiveGlowParticle.Provider(sprites, AdditiveGlowParticle.Preset.SPARK));
        event.registerSpriteSet(ParticleRegistry.ADDITIVE_RHOMBUS.get(), sprites -> new AdditiveRhombusParticle.Provider(sprites, AdditiveRhombusParticle.Preset.RHOMBUS));
        event.registerSpriteSet(ParticleRegistry.FALLING_SPARK.get(), FallingSparkParticle.Provider::new);
    }
}
