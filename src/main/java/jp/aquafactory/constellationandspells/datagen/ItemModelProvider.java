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
        basicItem(ItemRegistry.ZODIAC_RUNE.get());
        withExistingParent("zodiac_upgrade_orb", "item/generated")
                .texture("layer0", "irons_spellbooks:item/upgrade_orb_base")
                .texture("layer1", "irons_spellbooks:item/upgrade_orb_swirl")
                .texture("layer2", modLoc("item/upgrade_orb_zodiac"));
        withExistingParent("scroll_zodiac", "item/generated").texture("layer0", modLoc("item/scroll_zodiac"));
        withExistingParent("affinity_ring_zodiac", "item/generated")
                .texture("layer0", modLoc("item/affinity_ring_zodiac"));
    }
}
