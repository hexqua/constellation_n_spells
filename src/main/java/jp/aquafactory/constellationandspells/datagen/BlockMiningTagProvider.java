package jp.aquafactory.constellationandspells.datagen;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class BlockMiningTagProvider extends BlockTagsProvider {
    public BlockMiningTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper existingFiles) {
        super(output, lookup, ConstellationnSpells.MODID, existingFiles);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        var ores = new Block[]{BlockRegistry.STELLAR_DROP_ORE.get(), BlockRegistry.DEEPSLATE_STELLAR_DROP_ORE.get()};
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ores);
        tag(BlockTags.NEEDS_STONE_TOOL).add(ores);
        for (var holder : BlockRegistry.BLOCKS.getEntries()) {
            var block = holder.get();
            if (block instanceof SlabBlock) {
                tag(BlockTags.SLABS).add(block);
            } else if (block instanceof StairBlock) {
                tag(BlockTags.STAIRS).add(block);
            }
            if (block == BlockRegistry.STARCONDENSE_GLASS.get() || block == BlockRegistry.SHIMMER_STARCONDENSE_GLASS.get()) {
                tag(Tags.Blocks.GLASS_BLOCKS).add(block);
                tag(BlockTags.IMPERMEABLE).add(block);
            } else if (block == BlockRegistry.STARCONDENSE_GLASS_PANE.get() || block == BlockRegistry.SHIMMER_STARCONDENSE_GLASS_PANE.get()) {
                tag(Tags.Blocks.GLASS_PANES).add(block);
            } else {
                tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block);
            }
        }
    }
}
