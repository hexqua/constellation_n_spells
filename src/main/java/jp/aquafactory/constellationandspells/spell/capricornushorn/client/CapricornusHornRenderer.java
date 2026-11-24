package jp.aquafactory.constellationandspells.spell.capricornushorn.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.client.render.ExtrudedSpriteRenderer;
import jp.aquafactory.constellationandspells.spell.capricornushorn.CapricornusHorn;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class CapricornusHornRenderer extends EntityRenderer<CapricornusHorn> {
    private static final int SEGMENTS = 48;
    private static final int FIRST_WAVE_TICKS = 20;
    private static final int FIRST_WAVE_CORE_TICKS = 10;
    private static final int AFTERWAVE_TICKS = 24;
    private static final float DEGREES_PER_SEED_UNIT = 360.0f / 0x10000;
    private static final RenderType WAVE = WaveRenderState.create();
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ConstellationnSpells.MODID, "textures/spell/capricornus_horn.png");

    public CapricornusHornRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull CapricornusHorn horn, float yaw, float partialTick, @NotNull PoseStack pose,
                       @NotNull MultiBufferSource buffers, int light) {
        float age = horn.age() + partialTick;
        if (horn.radius() <= 0 || age < 0 || age >= CapricornusHorn.LIFETIME) return;

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(CapricornusHornVisuals.rotation(age)));
        float scale = CapricornusHornVisuals.scale(age);
        pose.scale(scale, scale, scale);
        ExtrudedSpriteRenderer.render(pose, buffers, LightTexture.FULL_BRIGHT, TEXTURE);
        pose.popPose();

        var consumer = buffers.getBuffer(WAVE);
        float coreAlpha = 0.60f * (1 - Mth.clamp(age / FIRST_WAVE_CORE_TICKS, 0, 1));
        wave(pose, consumer, age, horn.radius(), FIRST_WAVE_TICKS, 0.38f, coreAlpha,
                CapricornusHornVisuals.SILVER);
        var id = horn.getUUID();
        afterwave(pose, consumer, age - 7, horn.radius() * 0.86f, 0.21f, id.getLeastSignificantBits());
        afterwave(pose, consumer, age - 16, horn.radius() * 0.68f, 0.12f, id.getMostSignificantBits() >>> 32);
    }

    private static void afterwave(PoseStack pose, VertexConsumer consumer, float age, float maxRadius,
                                  float opacity, long seed) {
        if (age < 0 || age >= AFTERWAVE_TICKS) return;

        // 召喚ごとに余波の向きを変えつつ、描画するフレームや観測者によるずれを防ぐ。
        float yaw = (seed & 0xFFFFL) * DEGREES_PER_SEED_UNIT;
        float pitch = ((seed >>> 16) & 0xFFFFL) * DEGREES_PER_SEED_UNIT;
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.mulPose(Axis.XP.rotationDegrees(pitch));
        wave(pose, consumer, age, maxRadius, AFTERWAVE_TICKS, opacity, 0, CapricornusHornVisuals.EARTH);
        pose.popPose();
    }

    static float firstWaveRadius(float age, float maxRadius) {
        return waveRadius(age / FIRST_WAVE_TICKS, maxRadius);
    }

    private static float waveRadius(float progress, float maxRadius) {
        float remaining = 1 - progress;
        return Math.max(0.2f, maxRadius * (1 - remaining * remaining));
    }

    private static void wave(PoseStack pose, VertexConsumer consumer, float age, float maxRadius, int duration,
                             float opacity, float coreAlpha, int crestColor) {
        if (age < 0 || age >= duration) return;

        float progress = age / duration;
        float radius = waveRadius(progress, maxRadius);
        float alpha = opacity * (1 - progress);
        ring(pose.last().pose(), consumer, radius, alpha, coreAlpha, crestColor);

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(38));
        pose.mulPose(Axis.XP.rotationDegrees(60));
        ring(pose.last().pose(), consumer, radius, alpha * 0.55f, coreAlpha * 0.55f, crestColor);
        pose.mulPose(Axis.ZP.rotationDegrees(90));
        ring(pose.last().pose(), consumer, radius, alpha * 0.55f, coreAlpha * 0.55f, crestColor);
        pose.popPose();
    }

    private static void ring(Matrix4f matrix, VertexConsumer consumer, float radius, float alpha, float coreAlpha,
                             int crestColor) {
        float inner = radius * 0.88f;
        float shoulder = radius * 0.965f;
        float crest = radius * 0.985f;
        // 外側に白銀の前縁を寄せ、土色の尾を同じ面の連続した帯として描く。
        int earth = CapricornusHornVisuals.EARTH;
        int shoulderColor = ARGB32.color(Math.round(alpha * 255), earth);
        int brightCrest = ARGB32.color(Math.round((alpha * 0.7f + coreAlpha) * 255), crestColor);
        for (int i = 0; i < SEGMENTS; i++) {
            float from = i * (Mth.TWO_PI / SEGMENTS);
            float to = (i + 1) * (Mth.TWO_PI / SEGMENTS);
            float ax = Mth.cos(from);
            float az = Mth.sin(from);
            float bx = Mth.cos(to);
            float bz = Mth.sin(to);
            strip(matrix, consumer, ax, az, bx, bz, inner, shoulder, earth, shoulderColor);
            strip(matrix, consumer, ax, az, bx, bz, shoulder, crest, shoulderColor, brightCrest);
            strip(matrix, consumer, ax, az, bx, bz, crest, radius, brightCrest, crestColor);
        }
    }

    private static void strip(Matrix4f matrix, VertexConsumer consumer, float ax, float az, float bx, float bz,
                              float inner, float outer, int innerColor, int outerColor) {
        point(matrix, consumer, ax, az, inner, innerColor);
        point(matrix, consumer, bx, bz, inner, innerColor);
        point(matrix, consumer, bx, bz, outer, outerColor);
        point(matrix, consumer, ax, az, outer, outerColor);
    }

    private static void point(Matrix4f matrix, VertexConsumer consumer, float x, float z, float radius, int color) {
        consumer.addVertex(matrix, x * radius, 0, z * radius).setColor(color);
    }

    @Override
    public boolean shouldRender(@NotNull CapricornusHorn horn, @NotNull Frustum frustum, double x, double y, double z) {
        return horn.shouldRender(x, y, z) && frustum.isVisible(horn.getBoundingBox().inflate(horn.radius()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull CapricornusHorn horn) {
        return TEXTURE;
    }

    private static final class WaveRenderState extends RenderStateShard {
        private WaveRenderState() {
            super("capricornus_horn_wave", () -> {}, () -> {});
        }

        private static RenderType create() {
            return RenderType.create("capricornus_horn_wave", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS,
                    8192, false, true, RenderType.CompositeState.builder()
                            .setShaderState(POSITION_COLOR_SHADER)
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setWriteMaskState(COLOR_WRITE)
                            .setCullState(NO_CULL)
                            .createCompositeState(false));
        }
    }
}
