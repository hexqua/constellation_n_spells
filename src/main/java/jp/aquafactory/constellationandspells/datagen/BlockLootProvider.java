package jp.aquafactory.constellationandspells.datagen;

import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public final class BlockLootProvider extends BlockLootSubProvider {
    public BlockLootProvider(HolderLookup.Provider lookup) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), lookup);
    }

    @Override
    protected void generate() {
        for (var block : getKnownBlocks()) {
            add(block, createOreDrop(block, ItemRegistry.STARDUST_SHARD.get()));
        }
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return List.of(BlockRegistry.STELLAR_DROP_ORE.get(), BlockRegistry.DEEPSLATE_STELLAR_DROP_ORE.get());
    }
}
