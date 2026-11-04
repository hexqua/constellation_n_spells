package jp.aquafactory.constellationandspells.item.armor;

import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.function.Supplier;

public abstract class ImbuableGeoArmorItem extends BaseGeoArmorItem implements IPresetSpellContainer {
    private final Type imbueType;
    private final int spellSlots;

    protected ImbuableGeoArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties,
                                  Supplier<ItemAttributeModifiers> additionalModifiers, Type imbueType, int spellSlots) {
        super(material, type, properties, additionalModifiers);
        this.imbueType = imbueType;
        this.spellSlots = spellSlots;
    }

    @Override
    public void initializeSpellContainer(ItemStack stack) {
        if (stack != null && getType() == imbueType && !ISpellContainer.isSpellContainer(stack)) {
            ISpellContainer.set(stack, ISpellContainer.create(spellSlots, true, true));
        }
    }
}
