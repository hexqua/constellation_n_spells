package jp.aquafactory.constellationandspells.spell.virgoheal;

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

    private float getHealing(int level, LivingEntity caster){
        return getSpellPower(level, caster) / 200.0f;
    }

    private float getRadius(int level){
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
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData magicData) {
        // todo:実装.
        // onCastはContinuousだと発動直後のあとは10tick毎になる.
        super.onCast(level, spellLevel, caster, source, magicData);
    }
}
