package jp.aquafactory.constellationandspells.spell.stargazing;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.magic.ZodiacSchool;
import jp.aquafactory.constellationandspells.registry.SoundRegistry;
import jp.aquafactory.constellationandspells.spell.stardust.StardustBursts;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
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
                    .setCooldownSeconds(12)
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
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(level, caster), 2))
        );
    }

    private float getDamage(int level, LivingEntity caster){
        return 1 + getSpellPower(level, caster) / 100.0f;
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
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData magicData) {
        // todo:実装
        super.onCast(level, spellLevel, caster, source, magicData);
    }
}
