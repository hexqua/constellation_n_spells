package jp.aquafactory.constellationandspells.registry;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import jp.aquafactory.constellationandspells.magic.ZodiacSchool;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ZodiacSchoolRegistry {
    public static final DeferredRegister<SchoolType> SCHOOLS =
            DeferredRegister.create(SchoolRegistry.SCHOOL_REGISTRY_KEY, ConstellationnSpells.MODID);
    public static final DeferredHolder<SchoolType, SchoolType> ZODIAC = SCHOOLS.register("zodiac", ZodiacSchool::new);

    private ZodiacSchoolRegistry() {
    }
}
