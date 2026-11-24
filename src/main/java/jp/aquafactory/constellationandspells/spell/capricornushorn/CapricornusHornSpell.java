package jp.aquafactory.constellationandspells.spell.capricornushorn;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.magic.ZodiacSchool;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

public final class CapricornusHornSpell extends AbstractSpell {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "capricornus_horn");
    private final DefaultConfig config =
            new DefaultConfig()
                    .setMinRarity(SpellRarity.RARE)
                    .setSchoolResource(ZodiacSchool.SCHOOL_RESOURCE)
                    .setMaxLevel(6)
                    .setCooldownSeconds(30)
                    .build();

    public CapricornusHornSpell() {
        baseSpellPower = 900;
        spellPowerPerLevel = 100;
        baseManaCost = 80;
        manaCostPerLevel = 8;
        castTime = 0;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int level, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(level, caster), 2)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(level), 2))
        );
    }

    private float getDamage(int level, LivingEntity caster){
        return getSpellPower(level, caster) / 100.0f;
    }

    private float getRadius(int level) {
        // 限界突破してもレベル9で範囲は打ち止め.
        return Math.min(16, 8 + (level - 1));
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
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData magicData) {
        // todo:実装.
        super.onCast(level, spellLevel, caster, source, magicData);
    }
}
