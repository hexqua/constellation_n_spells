package jp.aquafactory.constellationandspells.gametest;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.block.ConnectedGlassTextureLayout;
import jp.aquafactory.constellationandspells.block.ConnectedPaneCulling;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
// GameTest がアノテーションから探索して呼び出す。
@SuppressWarnings("unused")
public final class ConnectedGlassGameTests {
    @GameTest(template = "gametest/basic_floor")
    public static void connectionUpdatesWhenGlassIsPlacedReplacedOrRemoved(GameTestHelper helper) {
        var glass = BlockRegistry.STARCONDENSE_GLASS.get();
        var pos = prepare(helper, glass);
        var level = helper.getLevel();
        check(helper, pos, glass, Direction.NORTH, Direction.EAST, Direction.UP, false, 0);
        level.setBlockAndUpdate(pos.east(), glass.defaultBlockState());
        check(helper, pos, glass, Direction.NORTH, Direction.EAST, Direction.UP, false, 16);
        level.setBlockAndUpdate(pos.east(), BlockRegistry.SHIMMER_STARCONDENSE_GLASS.get().defaultBlockState());
        check(helper, pos, glass, Direction.NORTH, Direction.EAST, Direction.UP, false, 0);
        level.setBlockAndUpdate(pos.east(), BlockRegistry.STARCONDENSE_GLASS_PANE.get().defaultBlockState());
        check(helper, pos, glass, Direction.NORTH, Direction.EAST, Direction.UP, false, 0);
        level.setBlockAndUpdate(pos.east(), glass.defaultBlockState());
        level.removeBlock(pos.east(), false);
        check(helper, pos, glass, Direction.NORTH, Direction.EAST, Direction.UP, false, 0);
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void holesAndCoveredStepsKeepTheirBorders(GameTestHelper helper) {
        var glass = BlockRegistry.SHIMMER_STARCONDENSE_GLASS.get();
        var pos = prepare(helper, glass);
        var level = helper.getLevel();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                level.setBlockAndUpdate(pos.offset(x, y, 0), glass.defaultBlockState());
            }
        }
        check(helper, pos, glass, Direction.NORTH, Direction.EAST, Direction.UP, false, 54);
        level.removeBlock(pos.east().above(), false);
        check(helper, pos, glass, Direction.NORTH, Direction.EAST, Direction.UP, false, 53);
        level.setBlockAndUpdate(pos.east().above(), glass.defaultBlockState());
        level.setBlockAndUpdate(pos.east().north(), glass.defaultBlockState());
        check(helper, pos, glass, Direction.NORTH, Direction.EAST, Direction.UP, false, 34);
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void everyFaceAndMirroredTextureUseTheirOwnAxes(GameTestHelper helper) {
        var glass = BlockRegistry.STARCONDENSE_GLASS.get();
        for (var face : Direction.values()) {
            for (var right : Direction.values()) {
                if (right.getAxis() == face.getAxis()) {
                    continue;
                }
                for (var up : Direction.values()) {
                    if (up.getAxis() == face.getAxis() || up.getAxis() == right.getAxis()) {
                        continue;
                    }
                    var pos = prepare(helper, glass);
                    for (var offset : List.of(pos.relative(right), pos.relative(up), pos.relative(right).relative(up))) {
                        helper.getLevel().setBlockAndUpdate(offset, glass.defaultBlockState());
                    }
                    check(helper, pos, glass, face, right, up, false, 20);
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void panesConnectAcrossWaterloggingButNotDifferentBlocks(GameTestHelper helper) {
        var pane = BlockRegistry.STARCONDENSE_GLASS_PANE.get();
        var pos = prepare(helper, pane);
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos.east(), pane.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true));
        check(helper, pos, pane, Direction.NORTH, Direction.EAST, Direction.UP, true, 16);
        level.setBlockAndUpdate(pos.east().north(), pane.defaultBlockState());
        check(helper, pos, pane, Direction.NORTH, Direction.EAST, Direction.UP, true, 16);
        level.setBlockAndUpdate(pos.east(), BlockRegistry.SHIMMER_STARCONDENSE_GLASS_PANE.get().defaultBlockState());
        check(helper, pos, pane, Direction.NORTH, Direction.EAST, Direction.UP, true, 0);
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void stackedPaneCapsRespectNeighborFootprintsAndBlockIdentity(GameTestHelper helper) {
        var pane = BlockRegistry.STARCONDENSE_GLASS_PANE.get();
        var pos = prepare(helper, pane);
        var level = helper.getLevel();
        var straight = pane.defaultBlockState().setValue(BlockStateProperties.NORTH, true)
                .setValue(BlockStateProperties.SOUTH, true);
        for (var direction : List.of(Direction.UP, Direction.DOWN)) {
            level.setBlock(pos.relative(direction), straight, Block.UPDATE_CLIENTS);
            var covered = ConnectedPaneCulling.coveringShape(level, pos, straight, direction);
            helper.assertTrue(ConnectedPaneCulling.covers(covered, 7.0 / 16, 7.0 / 16, 9.0 / 16, 9.0 / 16),
                    "Stacked panes must hide their shared center caps");
            helper.assertTrue(ConnectedPaneCulling.covers(covered, 7.0 / 16, 0, 9.0 / 16, 7.0 / 16),
                    "Stacked straight panes must hide shared arm caps");
            var corner = pane.defaultBlockState().setValue(BlockStateProperties.EAST, true)
                    .setValue(BlockStateProperties.SOUTH, true);
            level.setBlock(pos.relative(direction), corner, Block.UPDATE_CLIENTS);
            covered = ConnectedPaneCulling.coveringShape(level, pos, straight, direction);
            helper.assertTrue(!ConnectedPaneCulling.covers(covered, 7.0 / 16, 0, 9.0 / 16, 7.0 / 16),
                    "A missing arm in the neighboring pane must leave the exposed cap visible");
            helper.assertTrue(ConnectedPaneCulling.covers(covered, 7.0 / 16, 9.0 / 16, 9.0 / 16, 1),
                    "Matching arms must remain hidden even when other arms differ");
            level.setBlock(pos.relative(direction), BlockRegistry.SHIMMER_STARCONDENSE_GLASS_PANE.get()
                    .defaultBlockState().setValue(BlockStateProperties.NORTH, true), Block.UPDATE_CLIENTS);
            covered = ConnectedPaneCulling.coveringShape(level, pos, straight, direction);
            helper.assertTrue(!ConnectedPaneCulling.covers(covered, 7.0 / 16, 7.0 / 16, 9.0 / 16, 9.0 / 16),
                    "Different pane variants must keep their boundary caps");
        }
        helper.succeed();
    }

    private static BlockPos prepare(GameTestHelper helper, Block block) {
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        for (var nearby : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            helper.getLevel().setBlockAndUpdate(nearby, Blocks.AIR.defaultBlockState());
        }
        helper.getLevel().setBlockAndUpdate(pos, block.defaultBlockState());
        return pos;
    }

    private static void check(GameTestHelper helper, BlockPos pos, Block block, Direction face,
                              Direction right, Direction up, boolean pane, int expected) {
        int neighbors = ConnectedGlassTextureLayout.neighborhood(helper.getLevel(), pos, block.defaultBlockState());
        int actual = ConnectedGlassTextureLayout.tile(neighbors, face, right, up, pane);
        helper.assertTrue(actual == expected,
                "Connected glass must select tile " + expected + ", got " + actual
                        + "; face=" + face + "; right=" + right + "; up=" + up);
    }
}
