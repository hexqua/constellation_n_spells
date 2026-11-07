package jp.aquafactory.constellationandspells.spell.stargazing;

import io.redspace.ironsspellbooks.api.magic.MagicData;
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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public final class StargazingField extends Entity {
    private static final EntityDataAccessor<Float> HALF_EXTENT = SynchedEntityData.defineId(StargazingField.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> YAW = SynchedEntityData.defineId(StargazingField.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> OWNER = SynchedEntityData.defineId(StargazingField.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ACTIVE = SynchedEntityData.defineId(StargazingField.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Long> STARTED = SynchedEntityData.defineId(StargazingField.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> SEED = SynchedEntityData.defineId(StargazingField.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(StargazingField.class, EntityDataSerializers.INT);
    public static final int FIRST_PULSE = 20;
    public static final int PULSE_INTERVAL = 15;
    private LivingEntity caster;
    private float damage;
    private long lastPulse = Long.MIN_VALUE;

    public StargazingField(EntityType<? extends StargazingField> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public static StargazingField preview(ServerLevel level, LivingEntity caster, StargazingArea area) {
        var field = new StargazingField(EntityRegistry.STARGAZING_FIELD.get(), level);
        field.caster = caster;
        field.setPos(area.base());
        field.entityData.set(HALF_EXTENT, area.halfExtent());
        field.entityData.set(YAW, area.yaw());
        field.entityData.set(OWNER, caster.getId());
        field.entityData.set(SEED, level.random.nextLong());
        if (caster.isAlive() && !caster.isRemoved() && caster.level() == level && level.isPositionEntityTicking(field.blockPosition())) {
            level.addFreshEntity(field);
        } else {
            field.discard();
        }
        return field;
    }

    public void activate(float damage, int duration) {
        this.damage = damage;
        entityData.set(STARTED, level().getGameTime());
        entityData.set(DURATION, duration);
        entityData.set(ACTIVE, true);
    }

    public StargazingArea area() { return new StargazingArea(position(), entityData.get(HALF_EXTENT), entityData.get(YAW)); }
    public boolean active() { return entityData.get(ACTIVE); }
    public int ownerId() { return entityData.get(OWNER); }
    public long seed() { return entityData.get(SEED); }
    public long age() { return level().getGameTime() - entityData.get(STARTED); }
    public int duration() { return entityData.get(DURATION); }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        // 本体は小さな同期用 entity なので、既定の本体寸法から求める描画距離では星図がすぐ消えてしまう。
        return distance < 128 * 128;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        builder.define(HALF_EXTENT, 1.5f);
        builder.define(YAW, 0f);
        builder.define(OWNER, -1);
        builder.define(ACTIVE, false);
        builder.define(STARTED, 0L);
        builder.define(SEED, 0L);
        builder.define(DURATION, 100);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) return;
        if (caster == null || !caster.isAlive() || caster.isRemoved() || caster.level() != server
                || !server.isPositionEntityTicking(blockPosition())) {
            discard();
            return;
        }
        if (!active()) {
            var magic = MagicData.getPlayerMagicData(caster);
            if (!magic.isCasting() || !magic.getCastingSpellId().equals(SpellRegistry.STARGAZING.get().getSpellId())
                    || !(magic.getAdditionalCastData() instanceof StargazingCastData data) || !getUUID().equals(data.preview())) discard();
            return;
        }
        long elapsed = age();
        if (elapsed >= duration()) {
            discard();
        } else if (elapsed >= FIRST_PULSE && (elapsed - FIRST_PULSE) % PULSE_INTERVAL == 0 && lastPulse != elapsed) {
            lastPulse = elapsed;
            var area = area();
            for (var target : server.getEntitiesOfClass(LivingEntity.class, area.bounds(), entity -> entity != caster && entity.isAlive() && !entity.isSpectator())) {
                if (area.canReach(server, target)) StargazingDamage.apply(this, caster, target, damage);
            }
        }
    }

    @Override
    public boolean shouldBeSaved() { return false; }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {}
}
