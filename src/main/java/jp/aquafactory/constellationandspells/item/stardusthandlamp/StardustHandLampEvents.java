package jp.aquafactory.constellationandspells.item.stardusthandlamp;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.SpellRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ConstellationnSpells.MODID)
public final class StardustHandLampEvents {
    private StardustHandLampEvents() {}

    @SubscribeEvent
    public static void addSpell(SpellSelectionManager.SpellSelectionEvent event) {
        if (StardustHandLampCasting.find(event.getEntity()) != null) {
            event.addSelectionOption(new SpellData(SpellRegistry.STARDUST.get(), 1),
                    StardustHandLampCasting.SELECTION_SLOT, 0);
        }
    }
}
