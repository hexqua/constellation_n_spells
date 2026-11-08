package jp.aquafactory.constellationandspells.datagen;

import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class BlockLootProvider extends BlockLootSubProvider {
    public BlockLootProvider(HolderLookup.Provider lookup) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), lookup);
    }

    @Override
    protected void generate() {
        for (var holder : BlockRegistry.BLOCKS.getEntries()) {
            var block = holder.get();
            if (block == BlockRegistry.STELLAR_DROP_ORE.get() || block == BlockRegistry.DEEPSLATE_STELLAR_DROP_ORE.get()) {
                add(block, createOreDrop(block, ItemRegistry.STARDUST_SHARD.get()));
            } else if (block instanceof SlabBlock) {
                add(block, createSlabItemTable(block));
            } else {
                dropSelf(block);
            }
        }
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return BlockRegistry.BLOCKS.getEntries().stream().map(holder -> (Block) holder.get()).toList();
    }
}
