package jp.aquafactory.constellationandspells.spell.stardust;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.item.stardusthandlamp.StardustHandLampCasting;
import jp.aquafactory.constellationandspells.magic.ZodiacSchool;
import jp.aquafactory.constellationandspells.registry.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public final class StardustSpell extends AbstractSpell {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "stardust");
    private final DefaultConfig config =
            new DefaultConfig()
                    .setMinRarity(SpellRarity.COMMON)
                    .setSchoolResource(ZodiacSchool.SCHOOL_RESOURCE)
                    .setMaxLevel(10)
                    .setCooldownSeconds(1)
                    .build();

    public StardustSpell() {
        baseSpellPower = 80;
        spellPowerPerLevel = 10;
        baseManaCost = 10;
        manaCostPerLevel = 2;
        castTime = 0;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int level, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(level, caster), 2)),
                Component.translatable("ui.constellationandspells.projectile_count", StardustBursts.SHOT_COUNT)
        );
    }

    private float getDamage(int level, LivingEntity caster){
        return getSpellPower(level, caster) / 50.0f;
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
        return CastType.INSTANT;
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.STELLAR_LAUNCH.get());
    }

    @Override
    public boolean attemptInitiateCast(ItemStack stack, int spellLevel, Level level, Player player,
                                       CastSource source, boolean triggerCooldown, String castingEquipmentSlot) {
        if (level.isClientSide) {
            return false;
        }
        boolean lampSelection = StardustHandLampCasting.isLampSelection(stack, player, source, castingEquipmentSlot);
        var lamp = lampSelection ? StardustHandLampCasting.find(player) : null;
        if (lampSelection && lamp == null) {
            return false;
        }
        boolean started = super.attemptInitiateCast(stack, spellLevel, level, player, source, triggerCooldown,
                lamp != null ? lamp.slot().getName() : castingEquipmentSlot);
        if (started && lamp != null) {
            MagicData.getPlayerMagicData(player).setAdditionalCastData(new StardustHandLampCasting.CastData(lamp));
        }
        return started;
    }

    @Override
    public void castSpell(Level level, int spellLevel, ServerPlayer player, CastSource source, boolean triggerCooldown) {
        var data = MagicData.getPlayerMagicData(player).getAdditionalCastData();
        // 開始後に手放した個体や交換後の別個体では、魔法もマナ消費も成立させない。
        if (data instanceof StardustHandLampCasting.CastData(StardustHandLampCasting.HeldLamp lamp) && !lamp.isStillHeld(player)) {
            return;
        }
        super.castSpell(level, spellLevel, player, source, triggerCooldown);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData magicData) {
        if (level instanceof ServerLevel serverLevel) {
            if (magicData.getAdditionalCastData() instanceof StardustHandLampCasting.CastData(
                    StardustHandLampCasting.HeldLamp lamp
            )
                    && caster instanceof Player player) {
                if (!lamp.isStillHeld(player)) {
                    return;
                }
                lamp.stack().hurtAndBreak(1, player, lamp.slot());
            }
            StardustBursts.start(serverLevel, caster, getDamage(spellLevel, caster));
        }
        super.onCast(level, spellLevel, caster, source, magicData);
    }

    @Override
    public SpellDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return new StardustDamageSource(projectile, attacker, this);
    }
}
