package jp.aquafactory.constellationandspells.spell.capricornushorn;

import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class CapricornusHornTargeting {
    private static final double MAX_DISTANCE = 2;
    private static final double SEARCH_STEP = 0.125;
    private static final double HALF_SIZE = 0.375;

    private CapricornusHornTargeting() {}

    static Vec3 resolve(ServerLevel level, LivingEntity caster) {
        Vec3 eye = caster.getEyePosition();
        Vec3 aimed = eye.add(caster.getLookAngle().scale(MAX_DISTANCE));
        if (canPlace(level, caster, eye, aimed)) return aimed;

        Vec3 horizontal = Vec3.directionFromRotation(0, caster.getYRot());
        for (double distance = MAX_DISTANCE; distance > 0; distance -= SEARCH_STEP) {
            Vec3 candidate = eye.add(horizontal.scale(distance));
            if (canPlace(level, caster, eye, candidate)) return candidate;
        }
        return caster.position();
    }

    private static boolean canPlace(ServerLevel level, LivingEntity caster, Vec3 eye, Vec3 candidate) {
        var box = AABB.ofSize(candidate, HALF_SIZE * 2, HALF_SIZE * 2, HALF_SIZE * 2);
        return level.isPositionEntityTicking(BlockPos.containing(candidate))
                && isLoaded(level, box)
                && !level.isOutsideBuildHeight(BlockPos.containing(box.minX, box.minY, box.minZ))
                && !level.isOutsideBuildHeight(BlockPos.containing(box.maxX, box.maxY, box.maxZ))
                && level.noBlockCollision(caster, box)
                && isClear(level, eye, candidate);
    }

    static boolean isClear(ServerLevel level, Vec3 start, Vec3 end) {
        // 性能の担保のためにチャンクロードは仰々しいので無し(チャンクロード外は障害物扱い)
        return isLoaded(level, new AABB(start, end)) && Utils.hasLineOfSight(level, start, end, false);
    }

    private static boolean isLoaded(ServerLevel level, AABB bounds) {
        return level.hasChunksAt(Mth.floor(bounds.minX), Mth.floor(bounds.minZ),
                Mth.floor(bounds.maxX), Mth.floor(bounds.maxZ));
    }

    static boolean intersects(Vec3 center, double radius, AABB box) {
        var closest = new Vec3(
                Mth.clamp(center.x, box.minX, box.maxX),
                Mth.clamp(center.y, box.minY, box.maxY),
                Mth.clamp(center.z, box.minZ, box.maxZ));
        return center.distanceToSqr(closest) <= radius * radius;
    }
}
