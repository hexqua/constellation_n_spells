package jp.aquafactory.constellationandspells;

import jp.aquafactory.constellationandspells.datagen.DataGenerators;
import jp.aquafactory.constellationandspells.config.ServerConfig;
import jp.aquafactory.constellationandspells.network.SchoolPowerSharingSync;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import jp.aquafactory.constellationandspells.registry.CreativeTabRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import jp.aquafactory.constellationandspells.registry.AttributeRegistry;
import jp.aquafactory.constellationandspells.registry.ZodiacSchoolRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;

@Mod(ConstellationnSpells.MODID)
public class ConstellationnSpells {
    public static final String MODID = "constellationandspells";

    public ConstellationnSpells(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        SchoolPowerSharingSync.register(modEventBus);
        BlockRegistry.BLOCKS.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        AttributeRegistry.ATTRIBUTES.register(modEventBus);
        ZodiacSchoolRegistry.SCHOOLS.register(modEventBus);
        modEventBus.addListener(AttributeRegistry::modifyEntityAttributes);
        CreativeTabRegistry.CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(DataGenerators::gatherData);
    }
}
