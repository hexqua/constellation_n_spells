package jp.aquafactory.constellationandspells.datagen;

import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;

public final class DataGenerators {
    private DataGenerators() {
    }

    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        var lookup = event.getLookupProvider();
        generator.addProvider(event.includeClient(), new BlockModelProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new BlockMiningTagProvider(output, lookup, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(BlockLootProvider::new, LootContextParamSets.BLOCK)), lookup));
        generator.addProvider(event.includeServer(), new VanillaRecipeProvider(output, lookup));
        generator.addProvider(event.includeServer(), new WorldgenProvider(output, lookup));
    }
}
