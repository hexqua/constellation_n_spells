package jp.aquafactory.constellationandspells.datagen;

import io.redspace.ironsspellbooks.util.ModTags;
import jp.aquafactory.constellationandspells.registry.SchoolTags;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
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
        tag(SchoolTags.ZODIAC_FOCUS).add(ItemRegistry.STARDUST_SHARD.get());
        tag(ModTags.SCHOOL_FOCUS).addTag(SchoolTags.ZODIAC_FOCUS);
        tag(ModTags.INSCRIBED_RUNES).add(ItemRegistry.ZODIAC_RUNE.get());
    }
}
