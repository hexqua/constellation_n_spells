package jp.aquafactory.constellationandspells.item.armor;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MAX_MANA;
import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.SPELL_POWER;

public final class SchoolArmorProperties {
    private SchoolArmorProperties() {
    }

    public static ArmorMaterial material(Holder<SoundEvent> equipSound, Supplier<Ingredient> repairIngredient) {
        // Iron's 合わせ.
        return new ArmorMaterial(Map.of(
                ArmorItem.Type.HELMET, 3,
                ArmorItem.Type.CHESTPLATE, 8,
                ArmorItem.Type.LEGGINGS, 6,
                ArmorItem.Type.BOOTS, 3
        ),
                20,
                equipSound,
                repairIngredient,
                List.of(),
                0,
                0
        );
    }

    public static int durability(ArmorItem.Type type) {
        if (type == ArmorItem.Type.BODY) {
            throw new IllegalArgumentException("School armor does not support body armor");
        }
        return type.getDurability(37);
    }

    public static ItemAttributeModifiers attributes(ResourceLocation setId, ArmorItem.Type type,
                                                    Holder<Attribute> schoolPower) {
        var slot = EquipmentSlotGroup.bySlot(type.getSlot());
        var id = setId.withSuffix("." + type.getName());
        return ItemAttributeModifiers.builder()
                .add(MAX_MANA, new AttributeModifier(id, 125, AttributeModifier.Operation.ADD_VALUE), slot)
                .add(SPELL_POWER, new AttributeModifier(id, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slot)
                .add(schoolPower, new AttributeModifier(id, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slot)
                .build();
    }
}
