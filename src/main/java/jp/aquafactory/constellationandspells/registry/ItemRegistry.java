package jp.aquafactory.constellationandspells.registry;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ConstellationnSpells.MODID);

    public static final DeferredItem<Item> STARDUST_SHARD = ITEMS.registerSimpleItem("stardust_shard");
    public static final DeferredItem<BlockItem> STELLAR_DROP_ORE = ITEMS.registerSimpleBlockItem(BlockRegistry.STELLAR_DROP_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_STELLAR_DROP_ORE = ITEMS.registerSimpleBlockItem(BlockRegistry.DEEPSLATE_STELLAR_DROP_ORE);

    private ItemRegistry() {
    }
}
