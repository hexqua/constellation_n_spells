package jp.aquafactory.constellationandspells.registry;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ZodiacSchoolRegistry {
    public static final DeferredRegister<SchoolType> SCHOOLS =
            DeferredRegister.create(SchoolRegistry.SCHOOL_REGISTRY_KEY, ConstellationnSpells.MODID);
    public static final DeferredHolder<SchoolType, SchoolType> ZODIAC = SCHOOLS.register("zodiac",
            () -> new SchoolType(
                    ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "zodiac"),
                    SchoolTags.ZODIAC_FOCUS,
                    Component.translatable("school.constellationandspells.zodiac").withColor(0x191970),
                    AttributeRegistry.ZODIAC_SPELL_POWER,
                    AttributeRegistry.ZODIAC_MAGIC_RESIST,
                    // TODO: 星座術専用の詠唱音を制作し、暫定の Evocation 音から差し替える。
                    SoundRegistry.EVOCATION_CAST,
                    MagicRegistry.ZODIAC_MAGIC));

    private ZodiacSchoolRegistry() {
    }
}
