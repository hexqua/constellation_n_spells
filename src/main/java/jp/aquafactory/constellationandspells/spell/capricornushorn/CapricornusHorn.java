package jp.aquafactory.constellationandspells.spell.capricornushorn;

import jp.aquafactory.constellationandspells.magic.SpellDamageWithoutKnockback;
import jp.aquafactory.constellationandspells.registry.EntityRegistry;
import jp.aquafactory.constellationandspells.registry.SpellRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class CapricornusHorn extends Entity {
    private static final EntityDataAccessor<Long> STARTED = SynchedEntityData.defineId(CapricornusHorn.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(CapricornusHorn.class, EntityDataSerializers.FLOAT);
    private static final int HIT_TICKS = 10;
    public static final int LIFETIME = 40;

    private final Set<UUID> attemptedTargets = new HashSet<>();
    @Nullable
    private LivingEntity caster;
    private float damage;
    private long lastHitAge = -1;

    public CapricornusHorn(EntityType<? extends CapricornusHorn> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    static void summon(ServerLevel level, LivingEntity caster, float radius, float damage) {
        var horn = new CapricornusHorn(EntityRegistry.CAPRICORNUS_HORN.get(), level);
        horn.setPos(CapricornusHornTargeting.resolve(level, caster));
        horn.caster = caster;
        horn.damage = damage;
        horn.entityData.set(STARTED, level.getGameTime());
        horn.entityData.set(RADIUS, radius);
        if (level.addFreshEntity(horn)) {
            CapricornusHornEffects.onSummon(level, horn.position());
            horn.hitTargets(level);
        }
    }

    public long age() {
        return level().getGameTime() - entityData.get(STARTED);
    }

    public float radius() {
        return entityData.get(RADIUS);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 64 * 64;
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server) {
            if (age() >= LIFETIME) {
                discard();
            } else {
                hitTargets(server);
            }
        }
    }

    private void hitTargets(ServerLevel level) {
        long elapsed = age();
        if (caster == null || elapsed >= HIT_TICKS || elapsed <= lastHitAge) return;
        lastHitAge = elapsed;

        var center = position();
        float radius = radius();
        // Entity 検索は AABB の接触だけでは拾わないため、球の境界は最近点の判定で確定する。
        var bounds = new AABB(center, center).inflate(radius + 1.0e-6);
        var spell = SpellRegistry.CAPRICORNUS_HORN.get();
        for (var target : level.getEntitiesOfClass(LivingEntity.class, bounds,
                entity -> entity != caster && entity.isAlive() && !entity.isSpectator())) {
            var targetBox = target.getBoundingBox();
            if (!CapricornusHornTargeting.intersects(center, radius, targetBox) || !attemptedTargets.add(target.getUUID())) {
                continue;
            }
            boolean direct = CapricornusHornTargeting.isClear(level, center, targetBox.getCenter());
            var source = spell.getDamageSource(this, caster);
            if (SpellDamageWithoutKnockback.apply(target, direct ? damage : damage * 0.5f, source) && target.isAlive()) {
                launch(target, direct ? 1.0f : 0.5f);
            }
        }
    }

    private static void launch(LivingEntity target, float strength) {
        // NeoForge のイベントは水平成分のみを表せるため、強度とキャンセルを垂直方向へ反映する。
        var event = NeoForge.EVENT_BUS.post(new LivingKnockBackEvent(target, strength, 0, 0));
        if (event.isCanceled()) return;

        double verticalSpeed = event.getStrength() * (1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        var movement = target.getDeltaMovement();
        if (verticalSpeed > 0 && verticalSpeed > movement.y) {
            target.setDeltaMovement(movement.x, verticalSpeed, movement.z);
            target.hasImpulse = true;
            target.hurtMarked = true;
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        builder.define(STARTED, 0L);
        builder.define(RADIUS, 0f);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {}
}
