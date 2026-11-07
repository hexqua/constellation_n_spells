package jp.aquafactory.constellationandspells.spell.stargazing;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.magic.ZodiacSchool;
import jp.aquafactory.constellationandspells.registry.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public final class StargazingSpell extends AbstractSpell {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "stargazing");
    private final DefaultConfig config =
            new DefaultConfig()
                    .setMinRarity(SpellRarity.COMMON)
                    .setSchoolResource(ZodiacSchool.SCHOOL_RESOURCE)
                    .setMaxLevel(10)
                    .setCooldownSeconds(16)
                    .build();

    public StargazingSpell() {
        baseSpellPower = 0;
        spellPowerPerLevel = 80;
        baseManaCost = 0;
        manaCostPerLevel = 0;
        castTime = 15;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int level, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(level, caster), 2)),
                Component.translatable("ui.constellationandspells.side_length", Utils.stringTruncation(getHalfExtent(level) * 2, 1)),
                Component.translatable("ui.irons_spellbooks.duration", Utils.timeFromTicks(getDurationTick(), 1))
        );
    }

    private float getDamage(int level, LivingEntity caster){
        return 1 + getSpellPower(level, caster) / 100.0f;
    }

    private float getDistance(){
        return 8;
    }

    private float getHalfExtent(int level){
        return 1.5f + (level - 1) * 0.1f;
    }

    private int getDurationTick(){
        return 20 * 5;
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
        return Optional.of(SoundRegistry.MAP_DEPLOY.get());
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_CAST_ONE_HANDED;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.ANIMATION_LONG_CAST_FINISH;
    }

    @Override
    public ICastDataSerializable getEmptyCastData() { return new StargazingCastData(); }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity caster, MagicData magicData) {
        var target = StargazingTargeting.resolve(level, caster, getDistance());
        if (target.isEmpty()) {
            magicData.resetAdditionalCastData();
            if (caster instanceof ServerPlayer player) player.displayClientMessage(
                    Component.translatable("ui.constellationandspells.no_ground").withStyle(ChatFormatting.RED), true);
            return false;
        }
        magicData.setAdditionalCastData(new StargazingCastData(new StargazingArea(target.get(), getHalfExtent(spellLevel), caster.getYHeadRot())));
        return true;
    }

    @Override
    public void onServerPreCast(Level level, int spellLevel, LivingEntity caster, MagicData magicData) {
        if (level instanceof ServerLevel server && magicData != null && magicData.getAdditionalCastData() instanceof StargazingCastData data && data.area() != null) {
            var preview = StargazingField.preview(server, caster, data.area());
            data.setPreview(preview.getUUID());
        }
        super.onServerPreCast(level, spellLevel, caster, magicData);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData magicData) {
        if (level instanceof ServerLevel server && magicData.getAdditionalCastData() instanceof StargazingCastData data && data.area() != null) {
            // 完了時には照準や距離を再判定せず、開始時の範囲をそのまま展開する。
            var field = data.preview() == null ? null : server.getEntity(data.preview());
            if (field instanceof StargazingField preview && !preview.isRemoved()) preview.activate(getDamage(spellLevel, caster), getDurationTick());
        }
        super.onCast(level, spellLevel, caster, source, magicData);
    }

    @Override
    public void onServerCastComplete(Level level, int spellLevel, LivingEntity caster, MagicData magicData, boolean cancelled) {
        if (level instanceof ServerLevel server && magicData.getAdditionalCastData() instanceof StargazingCastData data && data.preview() != null) {
            var field = server.getEntity(data.preview());
            if (field instanceof StargazingField preview && !preview.active()) preview.discard();
        }
        super.onServerCastComplete(level, spellLevel, caster, magicData, cancelled);
    }
}
