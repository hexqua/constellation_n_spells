package jp.aquafactory.constellationandspells.spell.horoscope;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.magic.ZodiacSchool;
import jp.aquafactory.constellationandspells.registry.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

public final class HoroscopeSpell extends AbstractSpell {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "horoscope");
    private final DefaultConfig config =
            new DefaultConfig()
                    .setMinRarity(SpellRarity.UNCOMMON)
                    .setSchoolResource(ZodiacSchool.SCHOOL_RESOURCE)
                    .setMaxLevel(8)
                    .setCooldownSeconds(5)
                    .build();

    public HoroscopeSpell() {
        baseSpellPower = 0;
        spellPowerPerLevel = 30;
        baseManaCost = 0;
        manaCostPerLevel = 0;
        castTime = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int level, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.constellationandspells.revover_mana", Utils.stringTruncation(getManaRecovery(level, caster), 0))
        );
    }

    private float getManaRecovery(int level, LivingEntity caster) {
        return 30 + getSpellPower(level, caster);
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
        return CastType.LONG;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.ZODIAC_CAST.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.STAR_MANA.get());
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.BOW_CHARGE_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.ANIMATION_LONG_CAST_FINISH;
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity caster, MagicData magicData) {
        if (level instanceof ServerLevel serverLevel) {
            HoroscopeEffects.casting(serverLevel, caster);
        }
        super.onServerCastTick(level, spellLevel, caster, magicData);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData magicData) {
        if (level instanceof ServerLevel serverLevel) {
            magicData.addMana(getManaRecovery(spellLevel, caster));
            // 消費マナの同期は onCast より前なので、回復後の値を改めて送る。
            if (caster instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new SyncManaPacket(magicData));
            }
            HoroscopeEffects.complete(serverLevel, caster);
        }
        super.onCast(level, spellLevel, caster, source, magicData);
    }
}
