package jp.aquafactory.constellationandspells.datagen;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class BlockModelProvider extends BlockStateProvider {
    public BlockModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, ConstellationnSpells.MODID, existingFiles);
    }

    @Override
    protected void registerStatesAndModels() {
        cube(BlockRegistry.STELLAR_DROP_ORE.get());
        cube(BlockRegistry.DEEPSLATE_STELLAR_DROP_ORE.get());
        cube(BlockRegistry.BLOCK_OF_STARDUST_SHARD.get());
        cube(BlockRegistry.SMOOTH_STARCONDENSE_BLOCK.get());
        cube(BlockRegistry.SHIMMER_STARCONDENSE_BLOCK.get());
        cube(BlockRegistry.SMOOTH_SHIMMER_STARCONDENSE_BLOCK.get());
        variants(BlockRegistry.BLOCK_OF_STARDUST_SHARD.get(), BlockRegistry.STARCONDENSE_SLAB.get(), BlockRegistry.STARCONDENSE_STAIRS.get());
        variants(BlockRegistry.SMOOTH_STARCONDENSE_BLOCK.get(), BlockRegistry.SMOOTH_STARCONDENSE_SLAB.get(), BlockRegistry.SMOOTH_STARCONDENSE_STAIRS.get());
        glass(BlockRegistry.STARCONDENSE_GLASS.get(), BlockRegistry.STARCONDENSE_GLASS_PANE.get(), "starcondense_glass");
        glass(BlockRegistry.SHIMMER_STARCONDENSE_GLASS.get(), BlockRegistry.SHIMMER_STARCONDENSE_GLASS_PANE.get(), "shimmer_starcondense_glass");
    }

    private void cube(Block block) {
        simpleBlockWithItem(block, cubeAll(block));
    }

    private void variants(Block base, SlabBlock slab, StairBlock stairs) {
        slabBlock(slab, blockTexture(base), blockTexture(base));
        stairsBlock(stairs, blockTexture(base));
        simpleBlockItem(slab, models().getExistingFile(blockTexture(slab)));
        simpleBlockItem(stairs, models().getExistingFile(blockTexture(stairs)));
    }

    private void glass(Block glass, IronBarsBlock pane, String texture) {
        simpleBlockWithItem(glass, models().cubeAll(texture, modLoc("block/" + texture)).renderType("cutout"));
        paneBlockWithRenderType(pane,
                modLoc("block/" + texture), modLoc("block/" + texture + "_pane_top"), "cutout");
        itemModels().singleTexture(texture + "_pane", mcLoc("item/generated"), "layer0", modLoc("block/" + texture));
    }
}
