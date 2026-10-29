package jp.aquafactory.constellationandspells.magic;

import io.redspace.ironsspellbooks.api.spells.SchoolType;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record SchoolPowerSharingRules(boolean enabled, boolean autoDetectAstral,
                                      Set<ResourceLocation> additionalSchools, Set<ResourceLocation> excludedSchools) {
    public static final ResourceLocation ZODIAC_ID =
            ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "zodiac");
    public static final SchoolPowerSharingRules DEFAULT = new SchoolPowerSharingRules(true, true, Set.of(), Set.of());
    public static final SchoolPowerSharingRules INACTIVE = new SchoolPowerSharingRules(false, false, Set.of(), Set.of());

    public SchoolPowerSharingRules {
        additionalSchools = Set.copyOf(additionalSchools);
        excludedSchools = Set.copyOf(excludedSchools);
    }

    public List<SchoolType> resolveTargets(Map<ResourceLocation, SchoolType> schools) {
        // enabled は今回の転送方向だけを制御し、将来のアイテムでも使う対象集合には影響させない。
        return schools.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(ZODIAC_ID) && !excludedSchools.contains(entry.getKey()))
                .filter(entry -> additionalSchools.contains(entry.getKey())
                        || autoDetectAstral && entry.getKey().getPath().equals("astral"))
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .distinct()
                .toList();
    }
}
