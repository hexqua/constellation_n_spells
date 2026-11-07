package jp.aquafactory.constellationandspells.spell.stargazing;

import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class StargazingTargeting {
    private StargazingTargeting() {}

    public static Optional<Vec3> resolve(Level level, LivingEntity caster, float distance) {
        var start = caster.getEyePosition();
        var hit = level.clip(new ClipContext(start, start.add(caster.getLookAngle().scale(distance)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        if (hit.getType() == HitResult.Type.BLOCK) {
            var block = hit.getBlockPos();
            var candidate = level.getBlockState(block).canBeReplaced() ? block : block.relative(hit.getDirection());
            var ground = groundBelow(level, candidate, hit.getDirection() == Direction.UP ? 0 : 3);
            if (ground.isPresent()) return ground;
        }
        // 複合レイキャストだけでは手前のモブが地面より優先されるため、地面の失敗後にだけ使う。
        var fallback = RaycastBuilder.begin(level, caster).range(distance).checkForBlocks(true)
                .filter(entity -> entity != caster && entity instanceof LivingEntity && entity.isAlive() && !entity.isSpectator())
                .build();
        if (fallback instanceof EntityHitResult entityHit) {
            return groundBelow(level, entityHit.getEntity().blockPosition(), 3);
        }
        return Optional.empty();
    }

    public static Optional<Vec3> groundBelow(Level level, BlockPos candidate, int depth) {
        for (int offset = 0; offset <= depth; offset++) {
            var support = candidate.below(offset + 1);
            if (!level.isInWorldBounds(support) || !level.getChunkSource().hasChunk(support.getX() >> 4, support.getZ() >> 4)) return Optional.empty();
            var shape = level.getBlockState(support).getCollisionShape(level, support);
            if (!shape.isEmpty()) {
                return Optional.of(new Vec3(candidate.getX() + 0.5, support.getY() + shape.max(Direction.Axis.Y), candidate.getZ() + 0.5));
            }
        }
        return Optional.empty();
    }
}
