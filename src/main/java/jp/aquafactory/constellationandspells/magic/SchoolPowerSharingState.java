package jp.aquafactory.constellationandspells.magic;

import io.redspace.ironsspellbooks.api.spells.SchoolType;
import jp.aquafactory.constellationandspells.mixin.SchoolTypeAccessor;
import jp.aquafactory.constellationandspells.registry.AttributeRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.OptionalDouble;

public record SchoolPowerSharingState(SchoolPowerSharingRules rules, List<SchoolType> targets) {
    public static final SchoolPowerSharingState INACTIVE =
            new SchoolPowerSharingState(SchoolPowerSharingRules.INACTIVE, List.of());

    public SchoolPowerSharingState {
        targets = List.copyOf(targets);
    }

    public double zodiacPowerFor(LivingEntity entity) {
        var attribute = entity.getAttribute(AttributeRegistry.ZODIAC_SPELL_POWER);
        double power = attribute == null ? 1 : attribute.getValue();
        if (!rules.enabled() || !(entity instanceof Player)) {
            return power;
        }
        for (var school : targets) {
            var candidate = rawPowerFor(school, entity);
            if (candidate.isPresent()) {
                power = Math.max(power, candidate.getAsDouble());
            }
        }
        return power;
    }

    public static OptionalDouble rawPowerFor(SchoolType school, LivingEntity entity) {
        var attribute = ((SchoolTypeAccessor) school).constellationandspells$getPowerAttribute();
        // 欠落属性の仮値 1 を候補にすると、他 school の弱体化を意図せず打ち消してしまう。
        return entity.getAttributes().hasAttribute(attribute)
                ? OptionalDouble.of(entity.getAttributeValue(attribute)) : OptionalDouble.empty();
    }
}
