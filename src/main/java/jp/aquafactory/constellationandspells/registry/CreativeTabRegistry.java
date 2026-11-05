package jp.aquafactory.constellationandspells.registry;

import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
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
        addCurios(output);
        addArmor(output);
        addSpellScrolls(output);
    }

    private static void addCurios(CreativeModeTab.Output output) {
        output.accept(ItemRegistry.CONSTELLATION_TABLET.get());
    }

    private static void addArmor(CreativeModeTab.Output output) {
        output.accept(ItemRegistry.STAR_GATHERERS_HOOD.get());
        output.accept(ItemRegistry.STAR_GATHERERS_TUNIC.get());
        output.accept(ItemRegistry.STAR_GATHERERS_TROUSERS.get());
        output.accept(ItemRegistry.STAR_GATHERERS_BOOTS.get());
        output.accept(ItemRegistry.ASTRAL_OBSERVERS_HOOD.get());
        output.accept(ItemRegistry.ASTRAL_OBSERVERS_COAT.get());
        output.accept(ItemRegistry.ASTRAL_OBSERVERS_LEGGINGS.get());
        output.accept(ItemRegistry.ASTRAL_OBSERVERS_BOOTS.get());
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

    private static void addSpellScrolls(CreativeModeTab.Output output) {
        var spells = io.redspace.ironsspellbooks.api.registry.SpellRegistry.getEnabledSpells().stream()
                .filter(spell -> ConstellationnSpells.MODID.equals(spell.getSpellResource().getNamespace()))
                .toList();
        for (var spell : spells) {
            for (var level = spell.getMinLevel(); level <= spell.getMaxLevel(); ++level) {
                var scrollStack = new ItemStack(io.redspace.ironsspellbooks.registries.ItemRegistry.SCROLL.get());
                ISpellContainer.createScrollContainer(spell, level, scrollStack);
                output.accept(scrollStack);
            }
        }
    }
}
