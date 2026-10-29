package jp.aquafactory.constellationandspells.registry;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CreativeTabRegistry {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ConstellationnSpells.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB =
            CREATIVE_MODE_TABS.register(ConstellationnSpells.MODID, () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.constellationandspells"))
                    .icon(() -> ItemRegistry.STARDUST_SHARD.get().getDefaultInstance())
                    .displayItems(CreativeTabRegistry::displayItems)
                    .build());

    private CreativeTabRegistry() {
    }

    private static void displayItems(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        addBlocks(output);
        addMaterials(output);
    }

    private static void addMaterials(CreativeModeTab.Output output) {
        output.accept(ItemRegistry.STARDUST_SHARD.get());
        output.accept(ItemRegistry.ZODIAC_RUNE.get());
        output.accept(ItemRegistry.ZODIAC_UPGRADE_ORB.get());
    }

    private static void addBlocks(CreativeModeTab.Output output) {
        output.accept(ItemRegistry.STELLAR_DROP_ORE.get());
        output.accept(ItemRegistry.DEEPSLATE_STELLAR_DROP_ORE.get());
    }
}
