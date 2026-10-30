package jp.aquafactory.constellationandspells.datagen;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.MagicRegistry;
import jp.aquafactory.constellationandspells.registry.SchoolTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class DamageTypeTagProvider extends TagsProvider<DamageType> {
    public DamageTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                                 ExistingFileHelper existingFiles) {
        super(output, Registries.DAMAGE_TYPE, lookup, ConstellationnSpells.MODID, existingFiles);
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider lookup) {
        tag(SchoolTags.ZODIAC_MAGIC).add(MagicRegistry.ZODIAC_MAGIC);
        tag(Tags.DamageTypes.IS_MAGIC).addTag(SchoolTags.ZODIAC_MAGIC);
    }
}
