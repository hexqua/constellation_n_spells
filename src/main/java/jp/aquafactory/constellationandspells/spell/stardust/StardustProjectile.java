package jp.aquafactory.constellationandspells.spell.stardust;

import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import jp.aquafactory.constellationandspells.registry.SoundRegistry;
import jp.aquafactory.constellationandspells.registry.SpellRegistry;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class StardustProjectile extends AbstractMagicProjectile {
    public static final int LIFETIME = 15;
    public static final float SPEED = 2.6f;
    private boolean impacted;
    private Vec3 impactPosition;

    public StardustProjectile(EntityType<? extends StardustProjectile> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    @Override
    public void tick() {
        // 最後の区間も衝突判定してから消すため、15 回目の移動の後で失効させる。
        super.tick();
        if (tickCount >= LIFETIME) discard();
    }

    @Override
    protected void onHit(@NotNull HitResult hit) {
        if (level().isClientSide || impacted || hit.getType() == HitResult.Type.MISS) return;
        impacted = true;
        impactPosition = hit instanceof BlockHitResult block
                ? hit.getLocation().add(Vec3.atLowerCornerOf(block.getDirection().getNormal()).scale(0.02)) : hit.getLocation();
        super.onHit(hit);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult hit) {
        if (level().isClientSide) return;
        super.onHitEntity(hit);
        if (isRemoved()) return;
        var owner = getOwner();
        DamageSources.applyDamage(hit.getEntity(), damage,
                SpellRegistry.STARDUST.get().getDamageSource(this, owner == null ? this : owner));
        discard();
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult hit) {
        super.onHitBlock(hit);
        discard();
    }

    @Override
    public void trailParticles() { /* client の EntityTickEvent で共通コードから描画を分離する。 */ }

    @Override
    public void impactParticles(double x, double y, double z) {
        if (level() instanceof ServerLevel serverLevel) {
            var position = impactPosition == null ? new Vec3(x, y, z) : impactPosition;
            PacketDistributor.sendToPlayersNear(serverLevel, null, position.x, position.y, position.z, 48,
                    new StardustImpactPayload(position, getDeltaMovement().normalize()));
        }
    }

    @Override
    protected void doImpactSound(Holder<SoundEvent> sound) {
        var position = impactPosition == null ? position() : impactPosition;
        level().playSound(null, position.x, position.y, position.z, sound,
                SoundSource.NEUTRAL, 2, 0.9f + random.nextFloat() * 0.2f);
    }

    @Override
    public float getSpeed() { return SPEED; }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() { return Optional.of(SoundRegistry.STELLAR_IMPACT); }
}
