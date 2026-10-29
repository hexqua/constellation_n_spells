package jp.aquafactory.constellationandspells.registry;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ConstellationnSpells.MODID);
    public static final DeferredHolder<SoundEvent, SoundEvent> ZODIAC_CAST = register("zodiac_cast");
    public static final DeferredHolder<SoundEvent, SoundEvent> STELLAR_LAUNCH = register("stellar_launch");
    public static final DeferredHolder<SoundEvent, SoundEvent> STELLAR_IMPACT = register("stellar_impact");

    private SoundRegistry() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, name)));
    }
}
