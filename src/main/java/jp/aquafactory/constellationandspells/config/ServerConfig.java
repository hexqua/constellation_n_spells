package jp.aquafactory.constellationandspells.config;

import jp.aquafactory.constellationandspells.magic.SchoolPowerSharingRules;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ServerConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue ENABLED;
    private static final ModConfigSpec.BooleanValue AUTO_DETECT_ASTRAL;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ADDITIONAL_SCHOOLS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> EXCLUDED_SCHOOLS;

    static {
        var builder = new ModConfigSpec.Builder();
        builder.push("schoolPowerSharing");
        ENABLED = builder.comment("Allow players' Zodiac spells to use the highest raw power of linked schools.")
                .define("enabled", true);
        AUTO_DETECT_ASTRAL = builder.comment("Link schools whose registry ID path is exactly 'astral'.")
                .define("autoDetectAstral", true);
        ADDITIONAL_SCHOOLS = builder.comment("Additional linked school IDs (namespace:path). Missing mods are allowed.")
                .defineListAllowEmpty("additionalSchools", List.of(), () -> "irons_spellbooks:fire", ServerConfig::isSchoolId);
        EXCLUDED_SCHOOLS = builder.comment("Excluded school IDs (namespace:path). Exclusions override all inclusions.")
                .defineListAllowEmpty("excludedSchools", List.of(), () -> "asterismarcanum:astral", ServerConfig::isSchoolId);
        builder.pop();
        SPEC = builder.build();
    }

    private ServerConfig() {
    }

    public static boolean isSchoolId(Object value) {
        return value instanceof String id && id.indexOf(':') > 0 && !id.endsWith(":")
                && ResourceLocation.tryParse(id) != null;
    }

    public static SchoolPowerSharingRules snapshot() {
        return new SchoolPowerSharingRules(ENABLED.get(), AUTO_DETECT_ASTRAL.get(),
                parseIds(ADDITIONAL_SCHOOLS.get()), parseIds(EXCLUDED_SCHOOLS.get()));
    }

    private static Set<ResourceLocation> parseIds(List<? extends String> ids) {
        return ids.stream().map(ResourceLocation::parse).collect(Collectors.toUnmodifiableSet());
    }
}
