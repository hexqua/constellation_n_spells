package jp.aquafactory.constellationandspells.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ConnectedPaneCulling {
    private ConnectedPaneCulling() {}

    public static VoxelShape coveringShape(BlockGetter level, BlockPos pos, BlockState state, Direction face) {
        var neighborPos = pos.relative(face);
        var neighbor = level.getBlockState(neighborPos);
        if (neighbor.is(state.getBlock())) {
            return neighbor.getShape(level, neighborPos);
        }
        return neighbor.isSolidRender(level, neighborPos) ? Shapes.block() : Shapes.empty();
    }

    public static boolean covers(VoxelShape shape, double minX, double minZ, double maxX, double maxZ) {
        // 板の腕ごとに判定し、上段が L 字など別の形でも露出した厚み面を残す。
        // モデル回転で生じる微小な頂点誤差を除いて、隣接する板の占有領域と比較する。
        double epsilon = 0.00001;
        var footprint = Shapes.box(minX + epsilon, 0, minZ + epsilon, maxX - epsilon, 1, maxZ - epsilon);
        return !Shapes.joinIsNotEmpty(footprint, shape, BooleanOp.ONLY_FIRST);
    }
}
