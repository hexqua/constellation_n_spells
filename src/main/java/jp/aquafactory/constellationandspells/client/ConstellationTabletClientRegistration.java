package jp.aquafactory.constellationandspells.client;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.client.item.ConstellationTabletCurioRenderer;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@EventBusSubscriber(modid = ConstellationnSpells.MODID, value = Dist.CLIENT)
public final class ConstellationTabletClientRegistration {
    private ConstellationTabletClientRegistration() {
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> CuriosRendererRegistry.register(
                ItemRegistry.CONSTELLATION_TABLET.get(), ConstellationTabletCurioRenderer::new));
    }
}
