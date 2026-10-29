package jp.aquafactory.constellationandspells.registry;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;

public final class SchoolTags {
    public static final TagKey<Item> ZODIAC_FOCUS = ItemTags.create(
            ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "zodiac_focus"));
    public static final TagKey<DamageType> ZODIAC_MAGIC = TagKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "zodiac_magic"));

    private SchoolTags() {
    }
}
