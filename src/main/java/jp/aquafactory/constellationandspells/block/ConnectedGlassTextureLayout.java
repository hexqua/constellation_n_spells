package jp.aquafactory.constellationandspells.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Create の OMNIDIRECTIONAL 配置に対応する接続タイル選択。
 * Adapted from Create's AllCTTypes.OMNIDIRECTIONAL (MIT).
 * Copyright (c) The Create Team / The Creators of Create
 * See META-INF/licenses/create-connected-textures.txt for the license and source attribution.
 */
public final class ConnectedGlassTextureLayout {
    public static final int SHEET_SIZE = 8;

    private ConnectedGlassTextureLayout() {}

    public static int neighborhood(BlockGetter level, BlockPos pos, BlockState state) {
        int neighbors = 0;
        var cursor = new BlockPos.MutableBlockPos();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    cursor.setWithOffset(pos, x, y, z);
                    // 水没や板の接続形状が異なっても、同一の登録ブロックだけを接続する。
                    if (level.getBlockState(cursor).is(state.getBlock())) {
                        neighbors |= bit(x, y, z);
                    }
                }
            }
        }
        return neighbors;
    }

    public static int tile(int neighbors, Direction face, Direction right, Direction up, boolean pane) {
        boolean top = connects(neighbors, face, right, up, 0, 1, pane);
        boolean bottom = connects(neighbors, face, right, up, 0, -1, pane);
        boolean left = connects(neighbors, face, right, up, -1, 0, pane);
        boolean rightSide = connects(neighbors, face, right, up, 1, 0, pane);
        boolean topLeft = top && left && connects(neighbors, face, right, up, -1, 1, pane);
        boolean topRight = top && rightSide && connects(neighbors, face, right, up, 1, 1, pane);
        boolean bottomLeft = bottom && left && connects(neighbors, face, right, up, -1, -1, pane);
        boolean bottomRight = bottom && rightSide && connects(neighbors, face, right, up, 1, -1, pane);
        return index(top, bottom, left, rightSide, topLeft, topRight, bottomLeft, bottomRight);
    }

    private static boolean connects(int neighbors, Direction face, Direction right, Direction up,
                                    int horizontal, int vertical, boolean pane) {
        int x = right.getStepX() * horizontal + up.getStepX() * vertical;
        int y = right.getStepY() * horizontal + up.getStepY() * vertical;
        int z = right.getStepZ() * horizontal + up.getStepZ() * vertical;
        // 隣の立方体の手前に同じガラスがある場合は段差となるため、平らな面としてつながない。
        return (neighbors & bit(x, y, z)) != 0 && (pane
                || (neighbors & bit(x + face.getStepX(), y + face.getStepY(), z + face.getStepZ())) == 0);
    }

    private static int bit(int x, int y, int z) {
        return 1 << ((x + 1) * 9 + (y + 1) * 3 + z + 1);
    }

    private static int index(boolean up, boolean down, boolean left, boolean right,
                             boolean topLeft, boolean topRight, boolean bottomLeft, boolean bottomRight) {
        int tileX = (up ? 1 : 0) + (down ? 2 : 0);
        int tileY = (left ? 1 : 0) + (right ? 2 : 0);
        int borders = (up ? 0 : 1) + (down ? 0 : 1) + (left ? 0 : 1) + (right ? 0 : 1);
        if (borders == 0) {
            tileX += (topRight ? 1 : 0) + (topLeft ? 2 : 0);
            tileY += (bottomRight ? 2 : 0) + (bottomLeft ? 1 : 0);
        } else if (borders == 1) {
            if (!right && (topLeft || bottomLeft)) {
                tileY = 4;
                tileX = -1 + (bottomLeft ? 1 : 0) + (topLeft ? 2 : 0);
            }
            if (!left && (topRight || bottomRight)) {
                tileY = 5;
                tileX = -1 + (bottomRight ? 1 : 0) + (topRight ? 2 : 0);
            }
            if (!down && (topLeft || topRight)) {
                tileY = 6;
                tileX = -1 + (topLeft ? 1 : 0) + (topRight ? 2 : 0);
            }
            if (!up && (bottomLeft || bottomRight)) {
                tileY = 7;
                tileX = -1 + (bottomLeft ? 1 : 0) + (bottomRight ? 2 : 0);
            }
        } else if (borders == 2 && (up && left && topLeft || down && left && bottomLeft
                || up && right && topRight || down && right && bottomRight)) {
            tileX += 3;
        }
        return tileX + SHEET_SIZE * tileY;
    }
}
