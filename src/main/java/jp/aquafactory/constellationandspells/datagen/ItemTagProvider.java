package jp.aquafactory.constellationandspells.datagen;

import io.redspace.ironsspellbooks.util.ModTags;
import jp.aquafactory.constellationandspells.registry.SchoolTags;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import net.neoforged.neoforge.common.Tags;
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
        tag(Tags.Items.GLASS_BLOCKS).add(BlockRegistry.STARCONDENSE_GLASS.get().asItem(),
                BlockRegistry.SHIMMER_STARCONDENSE_GLASS.get().asItem());
        tag(Tags.Items.GLASS_PANES).add(BlockRegistry.STARCONDENSE_GLASS_PANE.get().asItem(),
                BlockRegistry.SHIMMER_STARCONDENSE_GLASS_PANE.get().asItem());
        tag(ItemTags.SLABS).add(BlockRegistry.STARCONDENSE_SLAB.get().asItem(), BlockRegistry.SMOOTH_STARCONDENSE_SLAB.get().asItem());
        tag(ItemTags.STAIRS).add(BlockRegistry.STARCONDENSE_STAIRS.get().asItem(), BlockRegistry.SMOOTH_STARCONDENSE_STAIRS.get().asItem());
        tag(Tags.Items.GLASS_BLOCKS_TINTED).add(BlockRegistry.MIDNIGHT_STARCONDENSE_GLASS.get().asItem());
        tag(ItemTags.SLABS).add(BlockRegistry.MIDNIGHT_STARCONDENSE_SLAB.get().asItem(), BlockRegistry.SMOOTH_MIDNIGHT_STARCONDENSE_SLAB.get().asItem());
        tag(ItemTags.STAIRS).add(BlockRegistry.MIDNIGHT_STARCONDENSE_STAIRS.get().asItem(), BlockRegistry.SMOOTH_MIDNIGHT_STARCONDENSE_STAIRS.get().asItem());
        tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "staff")))
                .add(ItemRegistry.CELESTIAL_SURVEY_STAFF.get());
        tag(SchoolTags.ZODIAC_FOCUS).add(ItemRegistry.STARDUST_SHARD.get());
        tag(ModTags.SCHOOL_FOCUS).addTag(SchoolTags.ZODIAC_FOCUS);
        tag(ModTags.INSCRIBED_RUNES).add(ItemRegistry.ZODIAC_RUNE.get());

        // Wisdom は付与対象と効果を有効にする装備場所を別々のタグで許可する.
        // Apprentice's Codex PR #1047 の対応版から有効になる.
        // c.f. https://github.com/hexqua/apprentice_codex/pull/1047
        var wisdomHeld = ItemTags.create(ResourceLocation.fromNamespaceAndPath("apprenticecodex", "wisdom/held"));
        var wisdomArmor = ItemTags.create(ResourceLocation.fromNamespaceAndPath("apprenticecodex", "wisdom/armor"));
        tag(wisdomHeld).add(ItemRegistry.CELESTIAL_SURVEY_STAFF.get());
        tag(wisdomArmor).add(
                ItemRegistry.STAR_GATHERERS_HOOD.get(),
                ItemRegistry.STAR_GATHERERS_TUNIC.get(),
                ItemRegistry.STAR_GATHERERS_TROUSERS.get(),
                ItemRegistry.STAR_GATHERERS_BOOTS.get(),
                ItemRegistry.ASTRAL_OBSERVERS_HOOD.get(),
                ItemRegistry.ASTRAL_OBSERVERS_COAT.get(),
                ItemRegistry.ASTRAL_OBSERVERS_LEGGINGS.get(),
                ItemRegistry.ASTRAL_OBSERVERS_BOOTS.get()
        );
        tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("apprenticecodex", "wisdom_enchantable")))
                .add(
                        ItemRegistry.CELESTIAL_SURVEY_STAFF.get(),
                        ItemRegistry.STAR_GATHERERS_HOOD.get(),
                        ItemRegistry.STAR_GATHERERS_TUNIC.get(),
                        ItemRegistry.STAR_GATHERERS_TROUSERS.get(),
                        ItemRegistry.STAR_GATHERERS_BOOTS.get(),
                        ItemRegistry.ASTRAL_OBSERVERS_HOOD.get(),
                        ItemRegistry.ASTRAL_OBSERVERS_COAT.get(),
                        ItemRegistry.ASTRAL_OBSERVERS_LEGGINGS.get(),
                        ItemRegistry.ASTRAL_OBSERVERS_BOOTS.get()
                );

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
