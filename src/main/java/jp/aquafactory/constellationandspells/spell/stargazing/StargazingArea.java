package jp.aquafactory.constellationandspells.spell.stargazing;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public record StargazingArea(Vec3 base, float halfExtent, float yaw) {
    public Vec3 center() { return base.add(0, 0.5, 0); }

    public AABB bounds() {
        double angle = Math.toRadians(yaw);
        double extent = halfExtent * (Math.abs(Math.cos(angle)) + Math.abs(Math.sin(angle)));
        return new AABB(base.x - extent, base.y, base.z - extent, base.x + extent, base.y + 1, base.z + extent);
    }

    public boolean intersects(AABB box) {
        if (box.maxY <= base.y || box.minY >= base.y + 1) return false;
        double angle = Math.toRadians(yaw);
        double c = Math.cos(angle), s = Math.sin(angle);
        double x = (box.minX + box.maxX) * 0.5 - base.x;
        double z = (box.minZ + box.maxZ) * 0.5 - base.z;
        double hx = box.getXsize() * 0.5, hz = box.getZsize() * 0.5;
        double extent = halfExtent * (Math.abs(c) + Math.abs(s));
        // ワールド軸と正方形のローカル軸の両方を調べ、外接 AABB の角への誤命中を防ぐ。
        return Math.abs(x) < hx + extent && Math.abs(z) < hz + extent
                && Math.abs(c * x + s * z) < halfExtent + Math.abs(c) * hx + Math.abs(s) * hz
                && Math.abs(-s * x + c * z) < halfExtent + Math.abs(s) * hx + Math.abs(c) * hz;
    }

    public boolean canReach(Level level, Entity target) {
        return intersects(target.getBoundingBox()) && level.clip(new ClipContext(center(), target.getBoundingBox().getCenter(),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, target)).getType() == HitResult.Type.MISS;
    }
}
