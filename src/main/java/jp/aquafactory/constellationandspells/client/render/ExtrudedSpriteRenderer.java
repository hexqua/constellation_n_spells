package jp.aquafactory.constellationandspells.client.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ExtrudedSpriteRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final float HALF_THICKNESS = 1.0f / 32.0f;
    private static final Map<ResourceLocation, List<Quad>> MESHES = new HashMap<>();

    private ExtrudedSpriteRenderer() {}

    public static void render(PoseStack pose, MultiBufferSource buffers, int light, ResourceLocation texture) {
        var quads = MESHES.computeIfAbsent(texture, ExtrudedSpriteRenderer::load);
        if (quads.isEmpty()) return;

        var consumer = buffers.getBuffer(RenderType.entityCutout(texture));
        for (var quad : quads) {
            vertex(consumer, pose.last(), quad.first(), quad.normal(), light);
            vertex(consumer, pose.last(), quad.second(), quad.normal(), light);
            vertex(consumer, pose.last(), quad.third(), quad.normal(), light);
            vertex(consumer, pose.last(), quad.fourth(), quad.normal(), light);
        }
    }

    public static void clearCache() {
        MESHES.clear();
    }

    private static List<Quad> load(ResourceLocation texture) {
        try (var stream = Minecraft.getInstance().getResourceManager().open(texture);
             var image = NativeImage.read(stream)) {
            return bake(image);
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.error("Failed to load extruded sprite: {}", texture, e);
            return List.of();
        }
    }

    private static List<Quad> bake(NativeImage image) {
        var quads = new ArrayList<Quad>();
        float front = -HALF_THICKNESS;
        float back = HALF_THICKNESS;
        // 前後の面は透過テクスチャに任せ、厚みを作る側面だけを輪郭に沿って生成する。
        quads.add(new Quad(Direction.NORTH,
                new Vertex(-0.5f, -0.5f, front, 0, 1), new Vertex(-0.5f, 0.5f, front, 0, 0),
                new Vertex(0.5f, 0.5f, front, 1, 0), new Vertex(0.5f, -0.5f, front, 1, 1)));
        quads.add(new Quad(Direction.SOUTH,
                new Vertex(-0.5f, -0.5f, back, 0, 1), new Vertex(0.5f, -0.5f, back, 1, 1),
                new Vertex(0.5f, 0.5f, back, 1, 0), new Vertex(-0.5f, 0.5f, back, 0, 0)));

        int width = image.getWidth();
        int height = image.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isTransparent(image, x, y)) continue;
                float left = x / (float) width - 0.5f;
                float right = (x + 1) / (float) width - 0.5f;
                float top = 0.5f - y / (float) height;
                float bottom = 0.5f - (y + 1) / (float) height;
                // 側面は境界の内側の色を使い、隣の透明ピクセルを拾わないようにする。
                float u = (x + 0.5f) / width;
                float v = (y + 0.5f) / height;
                if (isTransparent(image, x - 1, y)) quads.add(side(Direction.WEST, left, bottom, left, top, u, v));
                if (isTransparent(image, x + 1, y)) quads.add(side(Direction.EAST, right, top, right, bottom, u, v));
                if (isTransparent(image, x, y - 1)) quads.add(side(Direction.UP, left, top, right, top, u, v));
                if (isTransparent(image, x, y + 1)) quads.add(side(Direction.DOWN, right, bottom, left, bottom, u, v));
            }
        }
        return quads;
    }

    private static boolean isTransparent(NativeImage image, int x, int y) {
        return x < 0 || x >= image.getWidth() || y < 0 || y >= image.getHeight()
                || (image.getPixelRGBA(x, y) >>> 24) == 0;
    }

    private static Quad side(Direction normal, float x0, float y0, float x1, float y1, float u, float v) {
        return new Quad(normal,
                new Vertex(x0, y0, -HALF_THICKNESS, u, v), new Vertex(x0, y0, HALF_THICKNESS, u, v),
                new Vertex(x1, y1, HALF_THICKNESS, u, v), new Vertex(x1, y1, -HALF_THICKNESS, u, v));
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, Vertex vertex, Direction normal, int light) {
        consumer.addVertex(pose.pose(), vertex.x(), vertex.y(), vertex.z())
                .setColor(255, 255, 255, 255)
                .setUv(vertex.u(), vertex.v())
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, normal.getStepX(), normal.getStepY(), normal.getStepZ());
    }

    private record Quad(Direction normal, Vertex first, Vertex second, Vertex third, Vertex fourth) {}

    private record Vertex(float x, float y, float z, float u, float v) {}
}
