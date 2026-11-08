package jp.aquafactory.constellationandspells.client;

import io.redspace.ironsspellbooks.player.ClientMagicData;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.item.stardusthandlamp.StardustHandLampCasting;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = ConstellationnSpells.MODID, value = Dist.CLIENT)
public final class StardustHandLampClientEvents {
    private static Player lastPlayer;
    private static int lastHands;

    private StardustHandLampClientEvents() {}

    @SubscribeEvent
    public static void refreshSelection(ClientTickEvent.Post event) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            lastPlayer = null;
            lastHands = 0;
            return;
        }
        int hands = (player.getMainHandItem().is(ItemRegistry.STARDUST_HAND_LAMP.get()) ? 1 : 0)
                | (StardustHandLampCasting.physicalOffhand(player).is(ItemRegistry.STARDUST_HAND_LAMP.get()) ? 2 : 0);
        // 非コンテナのためIron'sの装備通知対象にはならず、物理スロットの状態変化を補足する。
        if ((hands != lastHands || player != lastPlayer) && (hands != 0 || lastHands != 0)) {
            ClientMagicData.updateSpellSelectionManager();
        }
        lastPlayer = player;
        lastHands = hands;
    }
}
