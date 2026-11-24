package jp.aquafactory.constellationandspells.registry;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.spell.virgoheal.VirgoHealMobility;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class EffectRegistry {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, ConstellationnSpells.MODID);
    public static final DeferredHolder<MobEffect, VirgoHealMobility> VIRGO_HEAL_MOBILITY =
            EFFECTS.register("virgo_heal_mobility", VirgoHealMobility::new);

    private EffectRegistry() {}
}
