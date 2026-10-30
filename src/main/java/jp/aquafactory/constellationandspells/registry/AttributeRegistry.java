package jp.aquafactory.constellationandspells.registry;

import io.redspace.ironsspellbooks.api.attribute.MagicPercentAttribute;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AttributeRegistry {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, ConstellationnSpells.MODID);
    public static final DeferredHolder<Attribute, Attribute> ZODIAC_SPELL_POWER = register("zodiac_spell_power");
    public static final DeferredHolder<Attribute, Attribute> ZODIAC_MAGIC_RESIST = register("zodiac_magic_resist");

    private AttributeRegistry() {
    }

    private static DeferredHolder<Attribute, Attribute> register(String name) {
        return ATTRIBUTES.register(name, () -> new MagicPercentAttribute(
                "attribute." + ConstellationnSpells.MODID + "." + name, 1, -100, 100).setSyncable(true));
    }

    public static void modifyEntityAttributes(EntityAttributeModificationEvent event) {
        event.getTypes().forEach(type -> ATTRIBUTES.getEntries().forEach(attribute -> event.add(type, attribute)));
    }
}
