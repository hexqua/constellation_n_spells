package jp.aquafactory.constellationandspells.gametest;

import com.mojang.serialization.JsonOps;
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
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
// GameTest がアノテーションから探索して呼び出す。
@SuppressWarnings("unused")
public class WorldgenGameTests {
    @GameTest(template = "gametest/basic_floor")
    public static void worldgenUsesBothDistributionsInOverworldBiomes(GameTestHelper helper) {
        var access = helper.getLevel().registryAccess();
        var configured = access.registryOrThrow(Registries.CONFIGURED_FEATURE).getOrThrow(WorldgenProvider.STELLAR_DROP_ORE);
        helper.assertTrue(configured.feature() == Feature.ORE, "Configured feature must generate ore");
        var config = (OreConfiguration) configured.config();
        helper.assertTrue(config.size == 6 && config.discardChanceOnAirExposure == 0.0F,
                "Ore veins must have size 6 and allow air exposure");
        helper.assertTrue(config.targetStates.size() == 2, "Ore must replace both stone and deepslate");
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
            var modifiers = feature.placement().stream().map(modifier -> PlacementModifier.CODEC
                    .encodeStart(JsonOps.INSTANCE, modifier).getOrThrow().getAsJsonObject()).toList();
            helper.assertTrue(modifiers.size() == 4, "Ore placement must have count, square, height and biome filters");
            helper.assertTrue(modifiers.get(0).get("count").getAsInt() == 4, "Each distribution must attempt four veins per chunk");
            helper.assertTrue(modifiers.get(1).get("type").getAsString().equals("minecraft:in_square")
                            && modifiers.get(3).get("type").getAsString().equals("minecraft:biome"),
                    "Ore placement must spread within chunks and filter biomes");
            var height = modifiers.get(2).getAsJsonObject("height");
            boolean triangle = key == WorldgenProvider.STELLAR_DROP_ORE_TRIANGLE;
            helper.assertTrue(height.get("type").getAsString().equals(triangle ? "minecraft:trapezoid" : "minecraft:uniform"),
                    "Ore height distribution must match its placement");
            helper.assertTrue(height.getAsJsonObject("min_inclusive").get("absolute").getAsInt() == (triangle ? -48 : -32)
                            && height.getAsJsonObject("max_inclusive").get("absolute").getAsInt() == (triangle ? 16 : 0),
                    "Ore placement height bounds must match the requested range");
            if (triangle) {
                helper.assertTrue(!height.has("plateau") || height.get("plateau").getAsInt() == 0,
                        "Triangle distribution must peak at -16 without a plateau");
            }
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
