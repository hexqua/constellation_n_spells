package jp.aquafactory.constellationandspells.registry;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.spell.horoscope.HoroscopeSpell;
import jp.aquafactory.constellationandspells.spell.stardust.StardustSpell;
import jp.aquafactory.constellationandspells.spell.stargazing.StargazingSpell;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SpellRegistry {
    public static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(
            io.redspace.ironsspellbooks.api.registry.SpellRegistry.SPELL_REGISTRY_KEY,
            ConstellationnSpells.MODID
    );

    public static final DeferredHolder<AbstractSpell, StardustSpell> STARDUST = SPELLS.register("stardust", StardustSpell::new);
    public static final DeferredHolder<AbstractSpell, HoroscopeSpell> HOROSCOPE = SPELLS.register("horoscope", HoroscopeSpell::new);
    public static final DeferredHolder<AbstractSpell, StargazingSpell> STARGAZING = SPELLS.register("stargazing", StargazingSpell::new);

    private SpellRegistry() {}
}
