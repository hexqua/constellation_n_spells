package jp.aquafactory.constellationandspells.spell.virgoheal;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.SpellHealEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.magic.SpellDamageWithoutKnockback;
import jp.aquafactory.constellationandspells.magic.ZodiacSchool;
import jp.aquafactory.constellationandspells.registry.EffectRegistry;
import jp.aquafactory.constellationandspells.registry.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class VirgoHealSpell extends AbstractSpell {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "virgo_heal");
    private final DefaultConfig config =
            new DefaultConfig()
                    .setMinRarity(SpellRarity.RARE)
                    .setSchoolResource(ZodiacSchool.SCHOOL_RESOURCE)
                    .setMaxLevel(8)
                    .setCooldownSeconds(20)
                    .build();

    public VirgoHealSpell() {
        baseSpellPower = 100;
        spellPowerPerLevel = 50;
        baseManaCost = 10;
        manaCostPerLevel = 1;
        castTime = 100;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int level, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.aoe_healing", Utils.stringTruncation(getHealing(level, caster), 2)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(level), 2))
        );
    }

    private float getHealing(int level, LivingEntity caster) {
        return getSpellPower(level, caster) / 200.0f;
    }

    private float getRadius(int level) {
        // Lv2ごとに増えるようにする(意図的に小数点を落とす)
        var stepLevel = ((level - 1) / 2);
        return 3 + stepLevel * 0.5f;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return ID;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return config;
    }

    @Override
    public CastType getCastType() {
        return CastType.CONTINUOUS;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_RAISED_HAND;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.ANIMATION_LONG_CAST_FINISH;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.VIRGO_START.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        // 効果側で鳴らすため、Finishは無し(CONTINUOUSの場合はループ再生されるため使わないようにする)
        return Optional.empty();
    }

    @Override
    public void onServerPreCast(Level level, int spellLevel, LivingEntity caster, @Nullable MagicData magicData) {
        super.onServerPreCast(level, spellLevel, caster, magicData);
        if (magicData != null && magicData.isCasting() && getSpellId().equals(magicData.getCastingSpellId())) {
            VirgoHealMobility.refresh(caster);
        }
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity caster, @Nullable MagicData magicData) {
        if (magicData != null && magicData.isCasting() && getSpellId().equals(magicData.getCastingSpellId())) {
            VirgoHealMobility.refresh(caster);
        }
    }

    @Override
    public void onServerCastComplete(Level level, int spellLevel, LivingEntity caster, MagicData magicData,
                                     boolean cancelled) {
        caster.removeEffect(EffectRegistry.VIRGO_HEAL_MOBILITY);
        super.onServerCastComplete(level, spellLevel, caster, magicData, cancelled);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData magicData) {
        if (level instanceof ServerLevel serverLevel) {
            var casterBox = caster.getBoundingBox();
            var center = new Vec3(caster.getX(), casterBox.maxY + 0.25, caster.getZ());
            float radius = getRadius(spellLevel);
            float amount = getHealing(spellLevel, caster);
            // 頭上の輪を基準にしつつ、同じ床にいる背の低い対象にも届かせる。
            var bounds = new AABB(center.x - radius, casterBox.minY - 0.5, center.z - radius,
                    center.x + radius, center.y + 1, center.z + radius);
            var healedEntityIds = new ArrayList<Integer>();
            for (var target : level.getEntitiesOfClass(LivingEntity.class, bounds,
                    entity -> entity.isAlive() && !entity.isSpectator() && entity.isPickable())) {
                var box = target.getBoundingBox();
                // 大型の対象も、中心ではなく当たり判定が波の円柱に触れれば含める.
                double dx = center.x - Mth.clamp(center.x, box.minX, box.maxX);
                double dz = center.z - Mth.clamp(center.z, box.minZ, box.maxZ);
                if (dx * dx + dz * dz > radius * radius) {
                    continue;
                }
                if (target.isInvertedHealAndHarm()) {
                    SpellDamageWithoutKnockback.apply(target, amount, new VirgoHealDamageSource(caster, this));
                } else if (Utils.shouldHealEntity((Entity) caster, target)) {
                    NeoForge.EVENT_BUS.post(new SpellHealEvent(caster, target, amount, getSchoolType()));
                    float healthBeforeHeal = target.getHealth();
                    target.heal(amount);
                    if (target.getHealth() > healthBeforeHeal) {
                        healedEntityIds.add(target.getId());
                    }
                }
            }

            PacketDistributor.sendToPlayersNear(serverLevel, null, center.x, center.y, center.z, 32,
                    new VirgoHealPulsePayload(center, radius, healedEntityIds));
            float pitch = 0.96f + serverLevel.random.nextFloat() * 0.08f;
            serverLevel.playSound(null, center.x, center.y, center.z,
                    SoundRegistry.VIRGO_PULSE.get(), caster.getSoundSource(), 0.25f, pitch);
        }
        super.onCast(level, spellLevel, caster, source, magicData);
    }
}
