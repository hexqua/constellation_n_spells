package jp.aquafactory.constellationandspells.registry;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.item.armor.SchoolArmorProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;

import static io.redspace.ironsspellbooks.registries.ItemRegistry.MAGIC_CLOTH;

public final class ArmorMaterialRegistry {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, ConstellationnSpells.MODID);
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> STAR_GATHERERS =
            ARMOR_MATERIALS.register("star_gatherers", () -> new ArmorMaterial(
                    Map.of(ArmorItem.Type.HELMET, 2, ArmorItem.Type.CHESTPLATE, 4,
                            ArmorItem.Type.LEGGINGS, 3, ArmorItem.Type.BOOTS, 1),
                    15, SoundRegistry.ARMOR_EQUIP_CLOTH,
                    () -> Ingredient.of(ItemTags.WOOL), List.of(), 0, 0));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ASTRAL_OBSERVERS =
            ARMOR_MATERIALS.register("astral_observers", () -> SchoolArmorProperties.material(
                    SoundRegistry.ARMOR_EQUIP_CLOTH, () -> Ingredient.of(MAGIC_CLOTH.get())));

    private ArmorMaterialRegistry() {
    }
}
