package jp.aquafactory.constellationandspells.registry;

import io.redspace.ironsspellbooks.item.UpgradeOrbItem;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ConstellationnSpells.MODID);

    public static final DeferredItem<Item> STARDUST_SHARD = ITEMS.registerSimpleItem("stardust_shard");
    public static final DeferredItem<Item> ZODIAC_RUNE = ITEMS.registerSimpleItem("zodiac_rune");
    public static final DeferredItem<UpgradeOrbItem> ZODIAC_UPGRADE_ORB = ITEMS.registerItem("zodiac_upgrade_orb",
            properties -> new UpgradeOrbItem(properties.rarity(Rarity.UNCOMMON).fireResistant()
                    .component(ComponentRegistry.UPGRADE_ORB_TYPE, MagicRegistry.ZODIAC_POWER)));
    public static final DeferredItem<BlockItem> STELLAR_DROP_ORE = ITEMS.registerSimpleBlockItem(BlockRegistry.STELLAR_DROP_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_STELLAR_DROP_ORE = ITEMS.registerSimpleBlockItem(BlockRegistry.DEEPSLATE_STELLAR_DROP_ORE);

    private ItemRegistry() {
    }
}
