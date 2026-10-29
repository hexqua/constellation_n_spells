package jp.aquafactory.constellationandspells.spell.stardust;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public final class StardustSpell extends AbstractSpell {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "stardust");
    private final DefaultConfig config = new DefaultConfig().setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "zodiac"))
            .setMaxLevel(10).setCooldownSeconds(0.5).build();

    public StardustSpell() {
        baseSpellPower = 80;
        spellPowerPerLevel = 10;
        baseManaCost = 10;
        manaCostPerLevel = 2;
        castTime = 20;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int level, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getSpellPower(level, caster) / 50.0f, 2)),
                Component.translatable("ui.constellationandspells.projectile_count", StardustBursts.SHOT_COUNT));
    }

    @Override
    public ResourceLocation getSpellResource() { return ID; }

    @Override
    public DefaultConfig getDefaultConfig() { return config; }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public Optional<SoundEvent> getCastStartSound() { return Optional.of(SoundRegistry.ZODIAC_CAST.get()); }

    @Override
    public Optional<SoundEvent> getCastFinishSound() { return Optional.of(SoundRegistry.STELLAR_LAUNCH.get()); }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_CAST;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.none();
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData magicData) {
        if (level instanceof ServerLevel serverLevel) {
            StardustBursts.start(serverLevel, caster, getSpellPower(spellLevel, caster) / 50.0f);
        }
        super.onCast(level, spellLevel, caster, source, magicData);
    }

    @Override
    public SpellDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return new StardustDamageSource(projectile, attacker, this);
    }
}
