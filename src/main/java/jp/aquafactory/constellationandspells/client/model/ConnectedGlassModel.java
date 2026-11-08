package jp.aquafactory.constellationandspells.client.model;

import jp.aquafactory.constellationandspells.block.ConnectedGlassTextureLayout;
import jp.aquafactory.constellationandspells.block.ConnectedPaneCulling;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConnectedGlassModel extends BakedModelWrapper<BakedModel> {
    private static final ModelProperty<Integer> NEIGHBORS = new ModelProperty<>();
    private static final ModelProperty<PaneCaps> PANE_CAPS = new ModelProperty<>();
    private final TextureAtlasSprite originalSprite;
    private final TextureAtlasSprite connectedSprite;
    private final boolean pane;
    private final Map<QuadTile, BakedQuad> quads = new ConcurrentHashMap<>();
    private final Map<CapShape, Boolean> coveredCaps = new ConcurrentHashMap<>();

    public ConnectedGlassModel(BakedModel original, TextureAtlasSprite originalSprite,
                               TextureAtlasSprite connectedSprite, boolean pane) {
        super(original);
        this.originalSprite = originalSprite;
        this.connectedSprite = connectedSprite;
        this.pane = pane;
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos,
                                           @NotNull BlockState state, @NotNull ModelData data) {
        var builder = super.getModelData(level, pos, state, data).derive()
                .with(NEIGHBORS, ConnectedGlassTextureLayout.neighborhood(level, pos, state));
        if (pane) {
            builder.with(PANE_CAPS, new PaneCaps(
                    ConnectedPaneCulling.coveringShape(level, pos, state, Direction.UP),
                    ConnectedPaneCulling.coveringShape(level, pos, state, Direction.DOWN)));
        }
        return builder.build();
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                            @NotNull RandomSource random, @NotNull ModelData data,
                                            @Nullable RenderType renderType) {
        var originals = super.getQuads(state, side, random, data, renderType);
        var neighbors = data.get(NEIGHBORS);
        if (state == null || neighbors == null) {
            return originals;
        }
        var result = new ArrayList<BakedQuad>(originals.size());
        var caps = data.get(PANE_CAPS);
        for (var quad : originals) {
            // バニラの板モデルの上下端面には cullface がなく、skipRendering だけでは消えない。
            // UV の変換対象外である厚み面も含め、実際に覆われた部分だけを除去する。
            if (caps != null && quad.getDirection().getAxis() == Direction.Axis.Y) {
                var coveringShape = quad.getDirection() == Direction.UP ? caps.above() : caps.below();
                if (coveredCaps.computeIfAbsent(new CapShape(quad, coveringShape),
                        key -> isCapCovered(key.quad(), key.shape()))) {
                    continue;
                }
            }
            // 板の厚みのテクスチャやリソースパックで差し替えた別素材の面は維持する。
            if (quad.getSprite() != originalSprite) {
                result.add(quad);
                continue;
            }
            var right = textureDirection(quad, true);
            var up = textureDirection(quad, false);
            if (right == null || up == null || right.getAxis() == up.getAxis()) {
                result.add(quad);
                continue;
            }
            int tile = ConnectedGlassTextureLayout.tile(neighbors, quad.getDirection(), right, up, pane);
            result.add(quads.computeIfAbsent(new QuadTile(quad, tile), key -> remap(key.quad(), key.tile())));
        }
        return result;
    }

    private static boolean isCapCovered(BakedQuad quad, VoxelShape coveringShape) {
        int[] vertices = quad.getVertices();
        double minX = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        double boundary = quad.getDirection() == Direction.UP ? 1 : 0;
        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * IQuadTransformer.STRIDE + IQuadTransformer.POSITION;
            double x = Float.intBitsToFloat(vertices[offset]);
            double y = Float.intBitsToFloat(vertices[offset + 1]);
            double z = Float.intBitsToFloat(vertices[offset + 2]);
            if (Math.abs(y - boundary) > 0.00001) {
                return false;
            }
            minX = Math.min(minX, x);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxZ = Math.max(maxZ, z);
        }
        return maxX - minX > 0.00002 && maxZ - minZ > 0.00002
                && ConnectedPaneCulling.covers(coveringShape, minX, minZ, maxX, maxZ);
    }

    private BakedQuad remap(BakedQuad quad, int tile) {
        int[] vertices = quad.getVertices().clone();
        int tileX = tile % ConnectedGlassTextureLayout.SHEET_SIZE;
        int tileY = tile / ConnectedGlassTextureLayout.SHEET_SIZE;
        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * IQuadTransformer.STRIDE + IQuadTransformer.UV0;
            float u = originalSprite.getUOffset(Float.intBitsToFloat(vertices[offset]));
            float v = originalSprite.getVOffset(Float.intBitsToFloat(vertices[offset + 1]));
            vertices[offset] = Float.floatToRawIntBits(connectedSprite.getU(
                    (u + tileX) / ConnectedGlassTextureLayout.SHEET_SIZE));
            vertices[offset + 1] = Float.floatToRawIntBits(connectedSprite.getV(
                    (v + tileY) / ConnectedGlassTextureLayout.SHEET_SIZE));
        }
        // 接続シート自体を sprite として渡し、通常・Sodium のアニメーション追跡対象にする。
        return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), connectedSprite,
                quad.isShade(), quad.hasAmbientOcclusion());
    }

    private static @Nullable Direction textureDirection(BakedQuad quad, boolean horizontal) {
        int[] vertices = quad.getVertices();
        int uv = IQuadTransformer.UV0 + (horizontal ? 0 : 1);
        int otherUv = IQuadTransformer.UV0 + (horizontal ? 1 : 0);
        for (int vertex = 1; vertex < 4; vertex++) {
            int offset = vertex * IQuadTransformer.STRIDE;
            float delta = Float.intBitsToFloat(vertices[offset + uv]) - Float.intBitsToFloat(vertices[uv]);
            float otherDelta = Float.intBitsToFloat(vertices[offset + otherUv]) - Float.intBitsToFloat(vertices[otherUv]);
            if (Math.abs(delta) < 0.0000001F || Math.abs(otherDelta) > 0.0000001F) {
                continue;
            }
            // 実際の UV と頂点位置から向きを求め、板の裏面や回転モデルの反転にも追従する。
            float sign = Math.signum(delta) * (horizontal ? 1 : -1);
            int position = IQuadTransformer.POSITION;
            return Direction.getNearest(
                    (Float.intBitsToFloat(vertices[offset + position]) - Float.intBitsToFloat(vertices[position])) * sign,
                    (Float.intBitsToFloat(vertices[offset + position + 1]) - Float.intBitsToFloat(vertices[position + 1])) * sign,
                    (Float.intBitsToFloat(vertices[offset + position + 2]) - Float.intBitsToFloat(vertices[position + 2])) * sign);
        }
        return null;
    }

    private record QuadTile(BakedQuad quad, int tile) {}
    private record PaneCaps(VoxelShape above, VoxelShape below) {}
    private record CapShape(BakedQuad quad, VoxelShape shape) {}
}
