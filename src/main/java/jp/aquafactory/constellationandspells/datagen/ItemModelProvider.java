package jp.aquafactory.constellationandspells.datagen;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class ItemModelProvider extends net.neoforged.neoforge.client.model.generators.ItemModelProvider {
    public ItemModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, ConstellationnSpells.MODID, existingFiles);
    }

    @Override
    protected void registerModels() {
        basicItem(ItemRegistry.STARDUST_SHARD.get());
    }
}
