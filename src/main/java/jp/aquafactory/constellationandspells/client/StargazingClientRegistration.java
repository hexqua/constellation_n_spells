package jp.aquafactory.constellationandspells.client;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.EntityRegistry;
import jp.aquafactory.constellationandspells.spell.stargazing.client.StargazingCharts;
import jp.aquafactory.constellationandspells.spell.stargazing.client.StargazingRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(modid = ConstellationnSpells.MODID, value = Dist.CLIENT)
public final class StargazingClientRegistration {
    private StargazingClientRegistration() {}

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.STARGAZING_FIELD.get(), StargazingRenderer::new);
    }

    @SubscribeEvent
    public static void reloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(StargazingCharts.INSTANCE);
    }
}
