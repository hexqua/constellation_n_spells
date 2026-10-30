package jp.aquafactory.constellationandspells.client;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.magic.SchoolPowerSharing;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = ConstellationnSpells.MODID, value = Dist.CLIENT)
public final class SchoolPowerSharingClientEvents {
    private SchoolPowerSharingClientEvents() {
    }

    @SubscribeEvent
    public static void onDisconnected(ClientPlayerNetworkEvent.LoggingOut event) {
        SchoolPowerSharing.resetClient();
    }
}
