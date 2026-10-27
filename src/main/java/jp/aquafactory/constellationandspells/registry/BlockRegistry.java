package jp.aquafactory.constellationandspells.registry;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BlockRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ConstellationnSpells.MODID);

    public static final DeferredBlock<DropExperienceBlock> STELLAR_DROP_ORE = BLOCKS.register("stellar_drop_ore",
            () -> new DropExperienceBlock(UniformInt.of(4, 8), BlockBehaviour.Properties.of()
                    .strength(3.0F, 3.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_STELLAR_DROP_ORE = BLOCKS.register("deepslate_stellar_drop_ore",
            () -> new DropExperienceBlock(UniformInt.of(4, 8), BlockBehaviour.Properties.of()
                    .strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops()));

    private BlockRegistry() {
    }
}
