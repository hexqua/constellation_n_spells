package jp.aquafactory.constellationandspells.registry;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.block.StellarDropOreBlock;
import jp.aquafactory.constellationandspells.block.ShimmerBlock;
import jp.aquafactory.constellationandspells.block.StarcondenseGlassBlock;
import jp.aquafactory.constellationandspells.block.StarcondenseGlassPaneBlock;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TintedGlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BlockRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ConstellationnSpells.MODID);

    public static final DeferredBlock<DropExperienceBlock> STELLAR_DROP_ORE = BLOCKS.register("stellar_drop_ore",
            () -> new StellarDropOreBlock(UniformInt.of(4, 8), BlockBehaviour.Properties.of()
                    .strength(3.0F, 3.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_STELLAR_DROP_ORE = BLOCKS.register("deepslate_stellar_drop_ore",
            () -> new StellarDropOreBlock(UniformInt.of(4, 8), BlockBehaviour.Properties.of()
                    .strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> BLOCK_OF_STARDUST_SHARD = BLOCKS.register("block_of_stardust_shard",
            () -> new Block(stoneProperties(0.8F, 0.8F)));
    public static final DeferredBlock<SlabBlock> STARCONDENSE_SLAB = BLOCKS.register("starcondense_slab",
            () -> new SlabBlock(stoneProperties(2.0F, 6.0F)));
    public static final DeferredBlock<StairBlock> STARCONDENSE_STAIRS = BLOCKS.register("starcondense_stairs",
            () -> new StairBlock(BLOCK_OF_STARDUST_SHARD.get().defaultBlockState(), stoneProperties(0.8F, 0.8F)));
    public static final DeferredBlock<Block> SMOOTH_STARCONDENSE_BLOCK = BLOCKS.register("smooth_starcondense_block",
            () -> new Block(stoneProperties(2.0F, 6.0F)));
    public static final DeferredBlock<SlabBlock> SMOOTH_STARCONDENSE_SLAB = BLOCKS.register("smooth_starcondense_slab",
            () -> new SlabBlock(stoneProperties(2.0F, 6.0F)));
    public static final DeferredBlock<StairBlock> SMOOTH_STARCONDENSE_STAIRS = BLOCKS.register("smooth_starcondense_stairs",
            () -> new StairBlock(SMOOTH_STARCONDENSE_BLOCK.get().defaultBlockState(), stoneProperties(2.0F, 6.0F)));
    public static final DeferredBlock<ShimmerBlock> SHIMMER_STARCONDENSE_BLOCK = BLOCKS.register("shimmer_starcondense_block",
            () -> new ShimmerBlock(stoneProperties(0.8F, 0.8F)));
    public static final DeferredBlock<ShimmerBlock> SMOOTH_SHIMMER_STARCONDENSE_BLOCK = BLOCKS.register("smooth_shimmer_starcondense_block",
            () -> new ShimmerBlock(stoneProperties(2.0F, 6.0F)));
    public static final DeferredBlock<StarcondenseGlassBlock> STARCONDENSE_GLASS = BLOCKS.register("starcondense_glass",
            () -> new StarcondenseGlassBlock(false, glassProperties()
                    .isValidSpawn((state, level, pos, entity) -> false)
                    .isRedstoneConductor((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)));
    public static final DeferredBlock<StarcondenseGlassBlock> SHIMMER_STARCONDENSE_GLASS = BLOCKS.register("shimmer_starcondense_glass",
            () -> new StarcondenseGlassBlock(true, glassProperties()
                    .isValidSpawn((state, level, pos, entity) -> false)
                    .isRedstoneConductor((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)));
    public static final DeferredBlock<StarcondenseGlassPaneBlock> STARCONDENSE_GLASS_PANE = BLOCKS.register("starcondense_glass_pane",
            () -> new StarcondenseGlassPaneBlock(false, glassProperties()));
    public static final DeferredBlock<StarcondenseGlassPaneBlock> SHIMMER_STARCONDENSE_GLASS_PANE = BLOCKS.register("shimmer_starcondense_glass_pane",
            () -> new StarcondenseGlassPaneBlock(true, glassProperties()));

    public static final DeferredBlock<Block> MIDNIGHT_STARCONDENSE_BLOCK = BLOCKS.register("midnight_starcondense_block",
            () -> new Block(stoneProperties(0.8F, 0.8F)));
    public static final DeferredBlock<SlabBlock> MIDNIGHT_STARCONDENSE_SLAB = BLOCKS.register("midnight_starcondense_slab",
            () -> new SlabBlock(stoneProperties(2.0F, 6.0F)));
    public static final DeferredBlock<StairBlock> MIDNIGHT_STARCONDENSE_STAIRS = BLOCKS.register("midnight_starcondense_stairs",
            () -> new StairBlock(MIDNIGHT_STARCONDENSE_BLOCK.get().defaultBlockState(), stoneProperties(0.8F, 0.8F)));
    public static final DeferredBlock<Block> SMOOTH_MIDNIGHT_STARCONDENSE_BLOCK = BLOCKS.register("smooth_midnight_starcondense_block",
            () -> new Block(stoneProperties(2.0F, 6.0F)));
    public static final DeferredBlock<SlabBlock> SMOOTH_MIDNIGHT_STARCONDENSE_SLAB = BLOCKS.register("smooth_midnight_starcondense_slab",
            () -> new SlabBlock(stoneProperties(2.0F, 6.0F)));
    public static final DeferredBlock<StairBlock> SMOOTH_MIDNIGHT_STARCONDENSE_STAIRS = BLOCKS.register("smooth_midnight_starcondense_stairs",
            () -> new StairBlock(SMOOTH_MIDNIGHT_STARCONDENSE_BLOCK.get().defaultBlockState(), stoneProperties(2.0F, 6.0F)));
    public static final DeferredBlock<TintedGlassBlock> MIDNIGHT_STARCONDENSE_GLASS = BLOCKS.register("midnight_starcondense_glass",
            () -> new TintedGlassBlock(glassProperties()
                    .isValidSpawn((state, level, pos, entity) -> false)
                    .isRedstoneConductor((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)));

    private static BlockBehaviour.Properties stoneProperties(float hardness, float resistance) {
        // バニラの対応するクォーツ建材の値を保持し、他 MOD によるプロパティ変更から独立させる。
        return BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM)
                .strength(hardness, resistance).sound(SoundType.STONE).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties glassProperties() {
        return BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT)
                .strength(0.3F, 0.3F).sound(SoundType.GLASS).noOcclusion();
    }

    private BlockRegistry() {
    }
}
