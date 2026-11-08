package jp.aquafactory.constellationandspells.registry;

import io.redspace.ironsspellbooks.item.UpgradeOrbItem;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.item.ConstellationTabletItem;
import jp.aquafactory.constellationandspells.item.CelestialSurveyStaffItem;
import jp.aquafactory.constellationandspells.item.stardusthandlamp.StardustHandLampItem;
import jp.aquafactory.constellationandspells.item.armor.AstralObserversArmorItem;
import jp.aquafactory.constellationandspells.item.armor.StarGatherersArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ConstellationnSpells.MODID);

    public static final DeferredItem<Item> STARDUST_SHARD = ITEMS.registerSimpleItem("stardust_shard");
    public static final DeferredItem<StardustHandLampItem> STARDUST_HAND_LAMP = ITEMS.registerItem(
            "stardust_hand_lamp", StardustHandLampItem::new);
    public static final DeferredItem<Item> ZODIAC_RUNE = ITEMS.registerSimpleItem("zodiac_rune");
    public static final DeferredItem<CelestialSurveyStaffItem> CELESTIAL_SURVEY_STAFF = ITEMS.registerItem(
            "celestial_survey_staff", CelestialSurveyStaffItem::new);
    public static final DeferredItem<ConstellationTabletItem> CONSTELLATION_TABLET = ITEMS.registerItem(
            "constellation_tablet", ConstellationTabletItem::new);
    public static final DeferredItem<UpgradeOrbItem> ZODIAC_UPGRADE_ORB = ITEMS.registerItem("zodiac_upgrade_orb",
            properties -> new UpgradeOrbItem(properties.rarity(Rarity.UNCOMMON).fireResistant()
                    .component(ComponentRegistry.UPGRADE_ORB_TYPE, MagicRegistry.ZODIAC_POWER)));
    public static final DeferredItem<BlockItem> STELLAR_DROP_ORE = ITEMS.registerSimpleBlockItem(BlockRegistry.STELLAR_DROP_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_STELLAR_DROP_ORE = ITEMS.registerSimpleBlockItem(BlockRegistry.DEEPSLATE_STELLAR_DROP_ORE);
    public static final DeferredItem<BlockItem> BLOCK_OF_STARDUST_SHARD = ITEMS.registerSimpleBlockItem(BlockRegistry.BLOCK_OF_STARDUST_SHARD);
    public static final DeferredItem<BlockItem> STARCONDENSE_SLAB = ITEMS.registerSimpleBlockItem(BlockRegistry.STARCONDENSE_SLAB);
    public static final DeferredItem<BlockItem> STARCONDENSE_STAIRS = ITEMS.registerSimpleBlockItem(BlockRegistry.STARCONDENSE_STAIRS);
    public static final DeferredItem<BlockItem> SMOOTH_STARCONDENSE_BLOCK = ITEMS.registerSimpleBlockItem(BlockRegistry.SMOOTH_STARCONDENSE_BLOCK);
    public static final DeferredItem<BlockItem> SMOOTH_STARCONDENSE_SLAB = ITEMS.registerSimpleBlockItem(BlockRegistry.SMOOTH_STARCONDENSE_SLAB);
    public static final DeferredItem<BlockItem> SMOOTH_STARCONDENSE_STAIRS = ITEMS.registerSimpleBlockItem(BlockRegistry.SMOOTH_STARCONDENSE_STAIRS);
    public static final DeferredItem<BlockItem> SHIMMER_STARCONDENSE_BLOCK = ITEMS.registerSimpleBlockItem(BlockRegistry.SHIMMER_STARCONDENSE_BLOCK);
    public static final DeferredItem<BlockItem> SMOOTH_SHIMMER_STARCONDENSE_BLOCK = ITEMS.registerSimpleBlockItem(BlockRegistry.SMOOTH_SHIMMER_STARCONDENSE_BLOCK);
    public static final DeferredItem<BlockItem> STARCONDENSE_GLASS = ITEMS.registerSimpleBlockItem(BlockRegistry.STARCONDENSE_GLASS);
    public static final DeferredItem<BlockItem> STARCONDENSE_GLASS_PANE = ITEMS.registerSimpleBlockItem(BlockRegistry.STARCONDENSE_GLASS_PANE);
    public static final DeferredItem<BlockItem> SHIMMER_STARCONDENSE_GLASS = ITEMS.registerSimpleBlockItem(BlockRegistry.SHIMMER_STARCONDENSE_GLASS);
    public static final DeferredItem<BlockItem> SHIMMER_STARCONDENSE_GLASS_PANE = ITEMS.registerSimpleBlockItem(BlockRegistry.SHIMMER_STARCONDENSE_GLASS_PANE);
    public static final DeferredItem<BlockItem> MIDNIGHT_STARCONDENSE_BLOCK = ITEMS.registerSimpleBlockItem(BlockRegistry.MIDNIGHT_STARCONDENSE_BLOCK);
    public static final DeferredItem<BlockItem> MIDNIGHT_STARCONDENSE_SLAB = ITEMS.registerSimpleBlockItem(BlockRegistry.MIDNIGHT_STARCONDENSE_SLAB);
    public static final DeferredItem<BlockItem> MIDNIGHT_STARCONDENSE_STAIRS = ITEMS.registerSimpleBlockItem(BlockRegistry.MIDNIGHT_STARCONDENSE_STAIRS);
    public static final DeferredItem<BlockItem> SMOOTH_MIDNIGHT_STARCONDENSE_BLOCK = ITEMS.registerSimpleBlockItem(BlockRegistry.SMOOTH_MIDNIGHT_STARCONDENSE_BLOCK);
    public static final DeferredItem<BlockItem> SMOOTH_MIDNIGHT_STARCONDENSE_SLAB = ITEMS.registerSimpleBlockItem(BlockRegistry.SMOOTH_MIDNIGHT_STARCONDENSE_SLAB);
    public static final DeferredItem<BlockItem> SMOOTH_MIDNIGHT_STARCONDENSE_STAIRS = ITEMS.registerSimpleBlockItem(BlockRegistry.SMOOTH_MIDNIGHT_STARCONDENSE_STAIRS);
    public static final DeferredItem<BlockItem> MIDNIGHT_STARCONDENSE_GLASS = ITEMS.registerSimpleBlockItem(BlockRegistry.MIDNIGHT_STARCONDENSE_GLASS);
    public static final DeferredItem<StarGatherersArmorItem> STAR_GATHERERS_HOOD = ITEMS.registerItem(
            "star_gatherers_hood", properties -> new StarGatherersArmorItem(ArmorItem.Type.HELMET, properties));
    public static final DeferredItem<StarGatherersArmorItem> STAR_GATHERERS_TUNIC = ITEMS.registerItem(
            "star_gatherers_tunic", properties -> new StarGatherersArmorItem(ArmorItem.Type.CHESTPLATE, properties));
    public static final DeferredItem<StarGatherersArmorItem> STAR_GATHERERS_TROUSERS = ITEMS.registerItem(
            "star_gatherers_trousers", properties -> new StarGatherersArmorItem(ArmorItem.Type.LEGGINGS, properties));
    public static final DeferredItem<StarGatherersArmorItem> STAR_GATHERERS_BOOTS = ITEMS.registerItem(
            "star_gatherers_boots", properties -> new StarGatherersArmorItem(ArmorItem.Type.BOOTS, properties));

    public static final DeferredItem<AstralObserversArmorItem> ASTRAL_OBSERVERS_HOOD = ITEMS.registerItem(
            "astral_observers_hood", properties -> new AstralObserversArmorItem(ArmorItem.Type.HELMET, properties));
    public static final DeferredItem<AstralObserversArmorItem> ASTRAL_OBSERVERS_COAT = ITEMS.registerItem(
            "astral_observers_coat", properties -> new AstralObserversArmorItem(ArmorItem.Type.CHESTPLATE, properties));
    public static final DeferredItem<AstralObserversArmorItem> ASTRAL_OBSERVERS_LEGGINGS = ITEMS.registerItem(
            "astral_observers_leggings", properties -> new AstralObserversArmorItem(ArmorItem.Type.LEGGINGS, properties));
    public static final DeferredItem<AstralObserversArmorItem> ASTRAL_OBSERVERS_BOOTS = ITEMS.registerItem(
            "astral_observers_boots", properties -> new AstralObserversArmorItem(ArmorItem.Type.BOOTS, properties));

    private ItemRegistry() {
    }
}
