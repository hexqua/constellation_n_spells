package jp.aquafactory.constellationandspells.gametest;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.datagen.WorldgenProvider;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
// GameTest がアノテーションから探索して呼び出す。
@SuppressWarnings("unused")
public final class WorldgenGameTests {
    @GameTest(template = "gametest/basic_floor")
    public static void worldgenUsesBothDistributionsInOverworldBiomes(GameTestHelper helper) {
        var access = helper.getLevel().registryAccess();
        var configured = access.registryOrThrow(Registries.CONFIGURED_FEATURE).getOrThrow(WorldgenProvider.STELLAR_DROP_ORE);
        helper.assertTrue(configured.feature() == Feature.ORE, "Configured feature must generate ore");
        var config = (OreConfiguration) configured.config();
        var random = RandomSource.create(7351L);
        for (var base : List.of(Blocks.STONE, Blocks.DEEPSLATE)) {
            var expected = base == Blocks.STONE ? BlockRegistry.STELLAR_DROP_ORE.get() : BlockRegistry.DEEPSLATE_STELLAR_DROP_ORE.get();
            helper.assertTrue(config.targetStates.stream().anyMatch(target -> target.target.test(base.defaultBlockState(), random)
                            && target.state.is(expected)), "Ore replacement must match its host stone");
        }
        var placed = access.registryOrThrow(Registries.PLACED_FEATURE);
        for (var key : List.of(WorldgenProvider.STELLAR_DROP_ORE_TRIANGLE, WorldgenProvider.STELLAR_DROP_ORE_UNIFORM)) {
            var feature = placed.getOrThrow(key);
            helper.assertTrue(feature.feature().is(WorldgenProvider.STELLAR_DROP_ORE), "Both placements must use the ore feature");
            for (var biome : access.registryOrThrow(Registries.BIOME).holders().toList()) {
                var steps = biome.value().getGenerationSettings().features();
                int step = GenerationStep.Decoration.UNDERGROUND_ORES.ordinal();
                boolean contains = steps.size() > step && steps.get(step).stream().anyMatch(holder -> holder.is(key));
                helper.assertTrue(contains == biome.is(BiomeTags.IS_OVERWORLD),
                        "Ore must generate in Overworld biomes only: " + biome.key().location());
            }
        }
        helper.succeed();
    }
}
