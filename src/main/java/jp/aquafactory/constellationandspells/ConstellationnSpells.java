package jp.aquafactory.constellationandspells;

import jp.aquafactory.constellationandspells.datagen.DataGenerators;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import jp.aquafactory.constellationandspells.registry.CreativeTabRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ConstellationnSpells.MODID)
public class ConstellationnSpells {
    public static final String MODID = "constellationandspells";

    public ConstellationnSpells(IEventBus modEventBus) {
        BlockRegistry.BLOCKS.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        CreativeTabRegistry.CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(DataGenerators::gatherData);
    }
}
