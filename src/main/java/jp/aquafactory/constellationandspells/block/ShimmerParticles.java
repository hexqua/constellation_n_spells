package jp.aquafactory.constellationandspells.block;

import jp.aquafactory.constellationandspells.particle.AdditiveGlowParticleOptions;
import jp.aquafactory.constellationandspells.registry.ParticleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

final class ShimmerParticles {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final float SPARK_CHANCE = 0.60F;
    private static final double SURFACE_OFFSET = 0.0625;

    private ShimmerParticles() {}

    static void animate(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() >= SPARK_CHANCE) {
            return;
        }
        var shape = state.getShape(level, pos);
        var boxes = shape.toAabbs();
        if (boxes.isEmpty()) {
            return;
        }
        var box = boxes.get(random.nextInt(boxes.size()));
        Vec3 selected = null;
        int exposedFaces = 0;
        for (var direction : DIRECTIONS) {
            var axis = direction.getAxis();
            double x = Mth.lerp(random.nextDouble(), box.minX, box.maxX);
            double y = Mth.lerp(random.nextDouble(), box.minY, box.maxY);
            double z = Mth.lerp(random.nextDouble(), box.minZ, box.maxZ);
            var center = new Vec3(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
            double start = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 2 : -1;
            var from = new Vec3(axis == Direction.Axis.X ? pos.getX() + start : center.x,
                    axis == Direction.Axis.Y ? pos.getY() + start : center.y,
                    axis == Direction.Axis.Z ? pos.getZ() + start : center.z);
            // 実際の形状へ外側からレイを当て、板の接続状態に応じた表面上へ粒子を置く。
            var hit = shape.clip(from, center, pos);
            if (hit == null) {
                continue;
            }
            var point = hit.getLocation();
            double coordinate = axis == Direction.Axis.X ? point.x - pos.getX()
                    : axis == Direction.Axis.Y ? point.y - pos.getY() : point.z - pos.getZ();
            boolean boundary = coordinate < 0.00001 || coordinate > 0.99999;
            if (boundary && !Block.shouldRenderFace(state, level, pos, direction, pos.relative(direction))) {
                continue;
            }
            if (random.nextInt(++exposedFaces) == 0) {
                selected = point.add(direction.getStepX() * SURFACE_OFFSET,
                        direction.getStepY() * SURFACE_OFFSET, direction.getStepZ() * SURFACE_OFFSET);
            }
        }
        if (selected != null) {
            spawn(level, random, selected);
        }
    }

    static void spawn(Level level, RandomSource random, Vec3 point) {
        boolean gold = random.nextFloat() < 0.35F;
        var options = new AdditiveGlowParticleOptions(ParticleRegistry.ADDITIVE_GLOWING_SPARK.get(),
                0.10F, gold ? 1.0F : 0.27F, gold ? 0xDB / 255F : 0.42F,
                gold ? 0x57 / 255F : 0.82F, 2, 12, 4,
                0.85F, 1.25F, 0.7F, 0.95F, 0.05F, 0.7F, 0.65F, true);
        // 鉱石と同じ通常の距離・パーティクル設定に従い、光源レベルは変更しない。
        level.addParticle(options, point.x, point.y, point.z, 0, 0, 0);
    }
}
