package jp.aquafactory.constellationandspells.gametest;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
// GameTest がアノテーションから探索して呼び出す。
@SuppressWarnings("unused")
public final class StarcondenseGameTests {
    @GameTest(template = "gametest/basic_floor")
    public static void onlyUnprocessedBlockReturnsShards(GameTestHelper helper) {
        var shard = ItemRegistry.STARDUST_SHARD.get();
        var base = BlockRegistry.BLOCK_OF_STARDUST_SHARD.get();
        check(helper, craft(helper, 2, 2, List.of(shard, shard, shard, shard)), base, 1);
        check(helper, craft(helper, 1, 1, List.of(base)), shard, 4);
        for (var holder : BlockRegistry.BLOCKS.getEntries()) {
            var block = holder.get();
            if (block == base || block == BlockRegistry.STELLAR_DROP_ORE.get()
                    || block == BlockRegistry.DEEPSLATE_STELLAR_DROP_ORE.get()) {
                continue;
            }
            helper.assertTrue(craft(helper, 1, 1, List.of(block)).isEmpty(),
                    "Processed building blocks must not unpack into shards: " + holder.getId());
            var input = new SingleRecipeInput(new ItemStack(block));
            for (var type : List.of(RecipeType.SMELTING, RecipeType.BLASTING)) {
                var match = helper.getLevel().getRecipeManager().getRecipeFor(type, input, helper.getLevel());
                helper.assertTrue(match.isEmpty() || !match.orElseThrow().value()
                                .assemble(input, helper.getLevel().registryAccess()).is(shard),
                        "Building blocks must not inherit ore cooking recipes: " + holder.getId());
            }
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void shimmerAndSmeltingCommuteAndPreserveCounts(GameTestHelper helper) {
        var base = BlockRegistry.BLOCK_OF_STARDUST_SHARD.get();
        var shimmer = BlockRegistry.SHIMMER_STARCONDENSE_BLOCK.get();
        var smooth = BlockRegistry.SMOOTH_STARCONDENSE_BLOCK.get();
        var smoothShimmer = BlockRegistry.SMOOTH_SHIMMER_STARCONDENSE_BLOCK.get();
        check(helper, surround(helper, base), shimmer, 8);
        check(helper, smelt(helper, shimmer), smoothShimmer, 1);
        check(helper, smelt(helper, base), smooth, 1);
        check(helper, surround(helper, smooth), smoothShimmer, 8);
        var glass = BlockRegistry.STARCONDENSE_GLASS.get();
        var shimmerGlass = BlockRegistry.SHIMMER_STARCONDENSE_GLASS.get();
        check(helper, surround(helper, Items.GLASS), glass, 8);
        check(helper, surround(helper, glass), shimmerGlass, 8);
        for (var pair : List.of(List.of(glass, BlockRegistry.STARCONDENSE_GLASS_PANE.get()),
                List.of(shimmerGlass, BlockRegistry.SHIMMER_STARCONDENSE_GLASS_PANE.get()))) {
            var source = pair.getFirst();
            check(helper, craft(helper, 3, 2, List.of(source, source, source, source, source, source)), pair.get(1), 16);
            helper.assertTrue(new ItemStack(source).is(Tags.Items.GLASS_BLOCKS)
                            && source.defaultBlockState().is(Tags.Blocks.GLASS_BLOCKS)
                            && new ItemStack(pair.get(1)).is(Tags.Items.GLASS_PANES)
                            && pair.get(1).defaultBlockState().is(Tags.Blocks.GLASS_PANES),
                    "Glass and panes must resolve through common glass tags on both registries");
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void stoneShapesCraftAndStonecutWithExpectedYield(GameTestHelper helper) {
        for (var family : List.of(
                List.of(BlockRegistry.BLOCK_OF_STARDUST_SHARD.get(), BlockRegistry.STARCONDENSE_SLAB.get(), BlockRegistry.STARCONDENSE_STAIRS.get()),
                List.of(BlockRegistry.SMOOTH_STARCONDENSE_BLOCK.get(), BlockRegistry.SMOOTH_STARCONDENSE_SLAB.get(), BlockRegistry.SMOOTH_STARCONDENSE_STAIRS.get()),
                List.of(BlockRegistry.MIDNIGHT_STARCONDENSE_BLOCK.get(), BlockRegistry.MIDNIGHT_STARCONDENSE_SLAB.get(), BlockRegistry.MIDNIGHT_STARCONDENSE_STAIRS.get()),
                List.of(BlockRegistry.SMOOTH_MIDNIGHT_STARCONDENSE_BLOCK.get(), BlockRegistry.SMOOTH_MIDNIGHT_STARCONDENSE_SLAB.get(), BlockRegistry.SMOOTH_MIDNIGHT_STARCONDENSE_STAIRS.get()))) {
            var base = family.getFirst();
            check(helper, craft(helper, 3, 1, List.of(base, base, base)), family.get(1), 6);
            var grid = new ArrayList<ItemStack>();
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    grid.add(column <= row ? new ItemStack(base) : ItemStack.EMPTY);
                }
            }
            check(helper, craftInput(helper, CraftingInput.of(3, 3, grid)), family.get(2), 4);
            var input = new SingleRecipeInput(new ItemStack(base));
            var recipes = helper.getLevel().getRecipeManager().getRecipesFor(RecipeType.STONECUTTING, input, helper.getLevel());
            for (int index = 1; index <= 2; index++) {
                var expected = family.get(index);
                var result = recipes.stream().map(recipe -> recipe.value().assemble(input, helper.getLevel().registryAccess()))
                        .filter(stack -> stack.is(expected.asItem())).findFirst().orElseThrow();
                check(helper, result, expected, index == 1 ? 2 : 1);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void glassDropsWithoutSilkAndStoneNeedsPickaxe(GameTestHelper helper) {
        for (var glass : List.of(BlockRegistry.STARCONDENSE_GLASS.get(), BlockRegistry.SHIMMER_STARCONDENSE_GLASS.get(),
                BlockRegistry.STARCONDENSE_GLASS_PANE.get(), BlockRegistry.SHIMMER_STARCONDENSE_GLASS_PANE.get(),
                BlockRegistry.MIDNIGHT_STARCONDENSE_GLASS.get())) {
            mine(helper, glass.defaultBlockState(), ItemStack.EMPTY, 1);
        }
        for (var block : List.of(BlockRegistry.BLOCK_OF_STARDUST_SHARD.get(), BlockRegistry.SMOOTH_STARCONDENSE_BLOCK.get(),
                BlockRegistry.SHIMMER_STARCONDENSE_BLOCK.get(), BlockRegistry.SMOOTH_SHIMMER_STARCONDENSE_BLOCK.get(),
                BlockRegistry.STARCONDENSE_STAIRS.get(), BlockRegistry.SMOOTH_STARCONDENSE_STAIRS.get(),
                BlockRegistry.MIDNIGHT_STARCONDENSE_BLOCK.get(), BlockRegistry.SMOOTH_MIDNIGHT_STARCONDENSE_BLOCK.get(),
                BlockRegistry.MIDNIGHT_STARCONDENSE_STAIRS.get(), BlockRegistry.SMOOTH_MIDNIGHT_STARCONDENSE_STAIRS.get())) {
            mine(helper, block.defaultBlockState(), ItemStack.EMPTY, 0);
            mine(helper, block.defaultBlockState(), new ItemStack(Items.WOODEN_PICKAXE), 1);
        }
        for (var slab : List.of(BlockRegistry.STARCONDENSE_SLAB.get(), BlockRegistry.SMOOTH_STARCONDENSE_SLAB.get(),
                BlockRegistry.MIDNIGHT_STARCONDENSE_SLAB.get(), BlockRegistry.SMOOTH_MIDNIGHT_STARCONDENSE_SLAB.get())) {
            mine(helper, slab.defaultBlockState(), new ItemStack(Items.WOODEN_PICKAXE), 1);
            mine(helper, slab.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE), new ItemStack(Items.WOODEN_PICKAXE), 2);
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void midnightStoneAcceptsBothCoalsAndEitherSmoothingOrder(GameTestHelper helper) {
        var midnight = BlockRegistry.MIDNIGHT_STARCONDENSE_BLOCK.get();
        var smoothMidnight = BlockRegistry.SMOOTH_MIDNIGHT_STARCONDENSE_BLOCK.get();
        for (var coal : List.of(Items.COAL, Items.CHARCOAL)) {
            check(helper, surround(helper, BlockRegistry.BLOCK_OF_STARDUST_SHARD.get(), coal), midnight, 8);
            check(helper, surround(helper, BlockRegistry.SMOOTH_STARCONDENSE_BLOCK.get(), coal), smoothMidnight, 8);
        }
        check(helper, smelt(helper, midnight), smoothMidnight, 1);
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void midnightGlassRequiresAmethystBlockAndProducesNine(GameTestHelper helper) {
        var glass = BlockRegistry.STARCONDENSE_GLASS.get();
        var midnight = BlockRegistry.MIDNIGHT_STARCONDENSE_GLASS.get();
        check(helper, surround(helper, glass, Items.AMETHYST_BLOCK), midnight, 9);
        helper.assertTrue(surround(helper, glass, Items.AMETHYST_SHARD).isEmpty(),
                "Midnight glass must require an amethyst block, not shards");
        helper.assertTrue(surround(helper, Items.GLASS, Items.AMETHYST_BLOCK).isEmpty(),
                "Midnight glass must require starcondense glass around the amethyst block");
        var stack = new ItemStack(midnight);
        helper.assertTrue(stack.is(Tags.Items.GLASS_BLOCKS) && stack.is(Tags.Items.GLASS_BLOCKS_TINTED)
                        && midnight.defaultBlockState().is(Tags.Blocks.GLASS_BLOCKS_TINTED),
                "Midnight glass must resolve through common tinted glass tags");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void midnightGlassBlocksLightAndRestoresItWhenReplaced(GameTestHelper helper) {
        var level = helper.getLevel();
        var center = helper.absolutePos(new BlockPos(1, 1, 1));
        for (var pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        }
        level.setBlockAndUpdate(center, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(center.above(), BlockRegistry.STARCONDENSE_GLASS.get().defaultBlockState());
        level.setBlockAndUpdate(center.above(2), Blocks.GLOWSTONE.defaultBlockState());
        // 他の経路を石で塞ぎ、実際の光伝播がガラスの交換で遮断・復帰することを確認する。
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(level.getBrightness(LightLayer.BLOCK, center) > 0,
                        "Normal starcondense glass must transmit the light source"))
                .thenExecute(() -> level.setBlockAndUpdate(center.above(), BlockRegistry.MIDNIGHT_STARCONDENSE_GLASS.get().defaultBlockState()))
                .thenWaitUntil(() -> helper.assertTrue(level.getBrightness(LightLayer.BLOCK, center) == 0,
                        "Midnight glass must block incoming block light"))
                .thenExecute(() -> level.setBlockAndUpdate(center.above(), BlockRegistry.STARCONDENSE_GLASS.get().defaultBlockState()))
                .thenWaitUntil(() -> helper.assertTrue(level.getBrightness(LightLayer.BLOCK, center) > 0,
                        "Replacing midnight glass must restore light propagation"))
                .thenSucceed();
    }

    private static void mine(GameTestHelper helper, BlockState state, ItemStack tool, int count) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        var player = FakePlayerFactory.getMinecraft(level);
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
        try {
            level.setBlockAndUpdate(pos, state);
            helper.assertTrue(player.gameMode.destroyBlock(pos), "Survival mining must destroy building blocks");
            var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(0.5));
            helper.assertTrue(drops.stream().allMatch(drop -> drop.getItem().is(state.getBlock().asItem()))
                            && drops.stream().mapToInt(drop -> drop.getItem().getCount()).sum() == count,
                    "Mining must preserve the expected building item and count: " + state);
            drops.forEach(ItemEntity::discard);
        } finally {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    private static ItemStack surround(GameTestHelper helper, ItemLike base) {
        return surround(helper, base, ItemRegistry.STARDUST_SHARD.get());
    }

    private static ItemStack surround(GameTestHelper helper, ItemLike base, ItemLike center) {
        var items = new ArrayList<ItemLike>();
        for (int i = 0; i < 9; i++) {
            items.add(i == 4 ? center : base);
        }
        return craft(helper, 3, 3, items);
    }

    private static ItemStack smelt(GameTestHelper helper, ItemLike base) {
        var input = new SingleRecipeInput(new ItemStack(base));
        return helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, helper.getLevel())
                .orElseThrow().value().assemble(input, helper.getLevel().registryAccess());
    }

    private static ItemStack craft(GameTestHelper helper, int width, int height, List<? extends ItemLike> items) {
        return craftInput(helper, CraftingInput.of(width, height, items.stream().map(ItemStack::new).toList()));
    }

    private static ItemStack craftInput(GameTestHelper helper, CraftingInput input) {
        return helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel())
                .map(recipe -> recipe.value().assemble(input, helper.getLevel().registryAccess())).orElse(ItemStack.EMPTY);
    }

    private static void check(GameTestHelper helper, ItemStack result, ItemLike expected, int count) {
        helper.assertTrue(result.is(expected.asItem()) && result.getCount() == count,
                "Recipe must produce " + count + " of " + expected + ", got " + result);
    }
}
