package jp.aquafactory.constellationandspells.datagen;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
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
    }
}
