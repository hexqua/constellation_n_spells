package jp.aquafactory.constellationandspells.datagen;

import io.redspace.ironsspellbooks.util.ModTags;
import jp.aquafactory.constellationandspells.registry.SchoolTags;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class ItemTagProvider extends ItemTagsProvider {
    public ItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                           ExistingFileHelper existingFiles) {
        super(output, lookup, CompletableFuture.completedFuture(TagLookup.empty()),
                ConstellationnSpells.MODID, existingFiles);
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider lookup) {
        tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "staff")))
                .add(ItemRegistry.CELESTIAL_SURVEY_STAFF.get());
        tag(SchoolTags.ZODIAC_FOCUS).add(ItemRegistry.STARDUST_SHARD.get());
        tag(ModTags.SCHOOL_FOCUS).addTag(SchoolTags.ZODIAC_FOCUS);
        tag(ModTags.INSCRIBED_RUNES).add(ItemRegistry.ZODIAC_RUNE.get());

        tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("curios", "spellbook")))
                .add(ItemRegistry.CONSTELLATION_TABLET.get());

        // バニラの enchantable タグが部位別防具タグを参照するため、付与範囲は標準の定義に任せる。
        tag(ItemTags.HEAD_ARMOR).add(
                ItemRegistry.STAR_GATHERERS_HOOD.get(),
                ItemRegistry.ASTRAL_OBSERVERS_HOOD.get()
        );
        tag(ItemTags.CHEST_ARMOR).add(
                ItemRegistry.STAR_GATHERERS_TUNIC.get(),
                ItemRegistry.ASTRAL_OBSERVERS_COAT.get()
        );
        tag(ItemTags.LEG_ARMOR).add(
                ItemRegistry.STAR_GATHERERS_TROUSERS.get(),
                ItemRegistry.ASTRAL_OBSERVERS_LEGGINGS.get()
        );
        tag(ItemTags.FOOT_ARMOR).add(
                ItemRegistry.STAR_GATHERERS_BOOTS.get(),
                ItemRegistry.ASTRAL_OBSERVERS_BOOTS.get()
        );

        // Trim未対応防具を登録.
        tag(ItemTags.TRIMMABLE_ARMOR).remove(
                ItemRegistry.ASTRAL_OBSERVERS_HOOD.get(),
                ItemRegistry.ASTRAL_OBSERVERS_COAT.get(),
                ItemRegistry.ASTRAL_OBSERVERS_LEGGINGS.get(),
                ItemRegistry.ASTRAL_OBSERVERS_BOOTS.get());
    }
}
