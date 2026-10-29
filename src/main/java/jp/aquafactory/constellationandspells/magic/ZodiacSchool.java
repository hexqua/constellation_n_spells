package jp.aquafactory.constellationandspells.magic;

import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import jp.aquafactory.constellationandspells.registry.AttributeRegistry;
import jp.aquafactory.constellationandspells.registry.MagicRegistry;
import jp.aquafactory.constellationandspells.registry.SchoolTags;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public final class ZodiacSchool extends SchoolType {
    public ZodiacSchool() {
        super(SchoolPowerSharingRules.ZODIAC_ID, SchoolTags.ZODIAC_FOCUS,
                Component.translatable("school.constellationandspells.zodiac").withColor(0x191970),
                AttributeRegistry.ZODIAC_SPELL_POWER, AttributeRegistry.ZODIAC_MAGIC_RESIST,
                // TODO: 星座術専用の詠唱音を制作し、暫定の Evocation 音から差し替える。
                SoundRegistry.EVOCATION_CAST, MagicRegistry.ZODIAC_MAGIC);
    }

    @Override
    public double getPowerFor(LivingEntity entity) {
        return SchoolPowerSharing.stateFor(entity).zodiacPowerFor(entity);
    }
}
