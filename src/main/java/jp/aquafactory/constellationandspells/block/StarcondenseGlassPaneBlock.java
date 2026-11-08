package jp.aquafactory.constellationandspells.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public final class StarcondenseGlassPaneBlock extends IronBarsBlock {
    private final boolean shimmer;

    public StarcondenseGlassPaneBlock(boolean shimmer, Properties properties) {
        super(properties);
        this.shimmer = shimmer;
    }

    @Override
    protected boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacent, @NotNull Direction side) {
        if (adjacent.getBlock() instanceof StarcondenseGlassPaneBlock
                && (!side.getAxis().isHorizontal()
                || state.getValue(PROPERTY_BY_DIRECTION.get(side))
                && adjacent.getValue(PROPERTY_BY_DIRECTION.get(side.getOpposite())))) {
            return true;
        }
        return super.skipRendering(state, adjacent, side);
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level,
                            @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (shimmer) {
            ShimmerParticles.animate(state, level, pos, random);
        }
    }
}
