package jp.aquafactory.constellationandspells.registry;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.spell.stardust.StardustSpell;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SpellRegistry {
    public static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(
            io.redspace.ironsspellbooks.api.registry.SpellRegistry.SPELL_REGISTRY_KEY, ConstellationnSpells.MODID);
    public static final DeferredHolder<AbstractSpell, StardustSpell> STARDUST = SPELLS.register("stardust", StardustSpell::new);

    private SpellRegistry() {}
}
