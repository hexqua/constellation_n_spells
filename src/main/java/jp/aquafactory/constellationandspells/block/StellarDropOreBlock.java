package jp.aquafactory.constellationandspells.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public final class StellarDropOreBlock extends DropExperienceBlock {
    private static final float SPARK_CHANCE = 0.30F;
    private static final double SURFACE_OFFSET = 0.0625;
    private static final Direction[] DIRECTIONS = Direction.values();

    public StellarDropOreBlock(IntProvider xpRange, Properties properties) {
        super(xpRange, properties);
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level,
                            @NotNull BlockPos pos, @NotNull RandomSource random) {
        // 抽選を先に済ませ、外れた呼び出しでは近傍参照や一時オブジェクト生成を行わない。
        if (random.nextFloat() >= SPARK_CHANCE) {
            return;
        }

        Direction face = null;
        int exposedFaces = 0;
        var neighborPos = new BlockPos.MutableBlockPos();
        for (var direction : DIRECTIONS) {
            neighborPos.setWithOffset(pos, direction);
            if (!level.getBlockState(neighborPos).isSolidRender(level, neighborPos)) {
                // 露出面から等確率で1面を選び、露出面の数によって発生数が増えないようにする。
                if (random.nextInt(++exposedFaces) == 0) {
                    face = direction;
                }
            }
        }
        if (face == null) {
            return;
        }

        var axis = face.getAxis();
        double x = pos.getX() + (axis == Direction.Axis.X
                ? 0.5 + face.getStepX() * (0.5 + SURFACE_OFFSET) : random.nextDouble());
        double y = pos.getY() + (axis == Direction.Axis.Y
                ? 0.5 + face.getStepY() * (0.5 + SURFACE_OFFSET) : random.nextDouble());
        double z = pos.getZ() + (axis == Direction.Axis.Z
                ? 0.5 + face.getStepZ() * (0.5 + SURFACE_OFFSET) : random.nextDouble());
        ShimmerParticles.spawn(level, random, new Vec3(x, y, z));
    }
}
