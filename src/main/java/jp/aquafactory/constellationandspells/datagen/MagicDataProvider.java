package jp.aquafactory.constellationandspells.datagen;

import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.MagicRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class MagicDataProvider extends DatapackBuiltinEntriesProvider {
    @Override
    public @NotNull String getName() {
        return "Zodiac magic registries";
    }

    public MagicDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, new RegistrySetBuilder()
                .add(Registries.DAMAGE_TYPE, MagicRegistry::bootstrapDamageTypes)
                .add(UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY, MagicRegistry::bootstrapUpgradeOrbTypes),
                Set.of(ConstellationnSpells.MODID));
    }
}
