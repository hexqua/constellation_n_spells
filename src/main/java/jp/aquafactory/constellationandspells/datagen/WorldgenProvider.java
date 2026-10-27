package jp.aquafactory.constellationandspells.datagen;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class WorldgenProvider extends DatapackBuiltinEntriesProvider {
    public static final ResourceKey<ConfiguredFeature<?, ?>> STELLAR_DROP_ORE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, id("stellar_drop_ore"));
    public static final ResourceKey<PlacedFeature> STELLAR_DROP_ORE_TRIANGLE = ResourceKey.create(
            Registries.PLACED_FEATURE, id("stellar_drop_ore_triangle"));
    public static final ResourceKey<PlacedFeature> STELLAR_DROP_ORE_UNIFORM = ResourceKey.create(
            Registries.PLACED_FEATURE, id("stellar_drop_ore_uniform"));
    public static final ResourceKey<BiomeModifier> ADD_STELLAR_DROP_ORE = ResourceKey.create(
            NeoForgeRegistries.Keys.BIOME_MODIFIERS, id("add_stellar_drop_ore"));

    public WorldgenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, registryBuilder(), Set.of(ConstellationnSpells.MODID));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, path);
    }

    private static RegistrySetBuilder registryBuilder() {
        return new RegistrySetBuilder()
                .add(Registries.CONFIGURED_FEATURE, WorldgenProvider::configuredFeatures)
                .add(Registries.PLACED_FEATURE, WorldgenProvider::placedFeatures)
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, WorldgenProvider::biomeModifiers);
    }

    private static void configuredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        var targets = List.of(
                OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
                        BlockRegistry.STELLAR_DROP_ORE.get().defaultBlockState()),
                OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
                        BlockRegistry.DEEPSLATE_STELLAR_DROP_ORE.get().defaultBlockState()));
        context.register(STELLAR_DROP_ORE, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(targets, 6, 0.0F)));
    }

    private static void placedFeatures(BootstrapContext<PlacedFeature> context) {
        var ore = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(STELLAR_DROP_ORE);
        context.register(STELLAR_DROP_ORE_TRIANGLE, new PlacedFeature(ore, List.of(
                CountPlacement.of(4), InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(-48), VerticalAnchor.absolute(16)),
                BiomeFilter.biome())));
        context.register(STELLAR_DROP_ORE_UNIFORM, new PlacedFeature(ore, List.of(
                CountPlacement.of(4), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-32), VerticalAnchor.absolute(0)),
                BiomeFilter.biome())));
    }

    private static void biomeModifiers(BootstrapContext<BiomeModifier> context) {
        var biomes = context.lookup(Registries.BIOME).getOrThrow(BiomeTags.IS_OVERWORLD);
        var features = context.lookup(Registries.PLACED_FEATURE);
        context.register(ADD_STELLAR_DROP_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(biomes,
                HolderSet.direct(features.getOrThrow(STELLAR_DROP_ORE_TRIANGLE), features.getOrThrow(STELLAR_DROP_ORE_UNIFORM)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
    }
}
