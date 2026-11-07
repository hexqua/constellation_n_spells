package jp.aquafactory.constellationandspells.spell.stargazing.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.spell.stargazing.StargazingChart;
import jp.aquafactory.constellationandspells.spell.stargazing.StargazingField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class StargazingRenderer extends EntityRenderer<StargazingField> {
    private static final ResourceLocation LINE_TEXTURE = texture("spell/glow_line_blur");
    private static final ResourceLocation STAR_TEXTURE = texture("particle/glow_circle");
    private static final RenderType PREVIEW = Types.preview();
    private static final RenderType CORE = Types.core();
    private static final RenderType LINE_GLOW = Types.glow("stargazing_line_glow", LINE_TEXTURE);
    private static final RenderType STAR_GLOW = Types.glow("stargazing_star_glow", STAR_TEXTURE);
    private static final float[][] CORNERS = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
    private static final float[] WIDTH_STEPS = {-1, -0.7f, 0.7f, 1};
    private static final float[] EDGE_FADE = {0, 1, 1, 0};
    private static final float CULLING_MARGIN = 0.5f;
    private static final Tint BLUE = new Tint(0.27f, 0.42f, 0.82f);
    private static final Tint GOLD = new Tint(1, 0xDB / 255f, 0x57 / 255f);
    private static final Tint PARCHMENT = new Tint(0xE6 / 255f, 0xA1 / 255f, 0x5A / 255f);

    public StargazingRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0;
    }

    @Override
    public void render(@NotNull StargazingField field, float entityYaw, float partialTick, @NotNull PoseStack pose,
                       @NotNull MultiBufferSource buffers, int light) {
        var viewer = Minecraft.getInstance().player;
        if (!field.active() && (viewer == null || viewer.getId() != field.ownerId())) return;
        float age = field.age() + partialTick;
        if (field.active() && age >= field.duration()) return;
        pose.pushPose();
        // 当たり判定のローカル X 軸 (cos(yaw), sin(yaw)) に描画を合わせる。
        pose.mulPose(Axis.YP.rotationDegrees(-field.area().yaw()));
        float half = field.area().halfExtent();
        if (field.active()) renderChart(field, age, half, pose.last().pose(), buffers);
        else renderPreview(half, pose.last().pose(), buffers.getBuffer(PREVIEW));
        pose.popPose();
    }

    private static void renderPreview(float half, Matrix4f matrix, VertexConsumer consumer) {
        for (int i = 0; i < CORNERS.length; i++) {
            var a = CORNERS[i];
            var b = CORNERS[(i + 1) % CORNERS.length];
            previewVertex(consumer, matrix, a[0] * half, 0, a[1] * half, 0.42f);
            previewVertex(consumer, matrix, b[0] * half, 0, b[1] * half, 0.42f);
            previewVertex(consumer, matrix, b[0] * half, 1, b[1] * half, 0);
            previewVertex(consumer, matrix, a[0] * half, 1, a[1] * half, 0);
        }
    }

    private static void renderChart(StargazingField field, float age, float half, Matrix4f matrix, MultiBufferSource buffers) {
        float reveal = Mth.clamp(age / StargazingField.FIRST_PULSE, 0, 1);
        float sincePulse = age < StargazingField.FIRST_PULSE ? StargazingField.PULSE_INTERVAL
                : (age - StargazingField.FIRST_PULSE) % StargazingField.PULSE_INTERVAL;
        float pulse = Mth.clamp(1 - sincePulse / 6, 0, 1);
        pulse *= pulse;
        float fade = Mth.clamp((field.duration() - age) / 5, 0, 1);
        float brightness = (0.42f + 0.5f * pulse) * fade;
        float y = 0.5f + 0.04f * Mth.sin(age * 0.07f);
        var chart = StargazingCharts.INSTANCE.forSeed(field.seed());
        if (chart != null) {
            var glow = buffers.getBuffer(LINE_GLOW);
            drawConnections(glow, matrix, chart, age, half, y, brightness, pulse, true);
            var halo = buffers.getBuffer(STAR_GLOW);
            for (int i = 0; i < chart.stars().size(); i++) {
                var star = chart.stars().get(i);
                float appear = starReveal(age, i, chart.stars().size());
                float strength = brightness * star.brightness() * appear;
                float radius = (0.12f + pulse * 0.05f) * star.size();
                starHalo(halo, matrix, star.x() * half, star.z() * half, y, radius * 1.8f, strength * 0.12f, BLUE);
                starHalo(halo, matrix, star.x() * half, star.z() * half, y, radius, strength * 0.45f, GOLD);
            }
            var core = buffers.getBuffer(CORE);
            drawConnections(core, matrix, chart, age, half, y, brightness, pulse, false);
            for (int i = 0; i < chart.stars().size(); i++) {
                var star = chart.stars().get(i);
                float appear = starReveal(age, i, chart.stars().size());
                float size = (0.024f + pulse * 0.009f) * star.size() * appear;
                float strength = brightness * star.brightness() * appear;
                float x = star.x() * half, z = star.z() * half;
                coreLine(core, matrix, x - size, z, x + size, z, y, size * 0.23f, GOLD, strength);
                coreLine(core, matrix, x, z - size, x, z + size, y, size * 0.23f, GOLD, strength);
            }
        }
        var borderGlow = buffers.getBuffer(LINE_GLOW);
        for (int i = 0; i < CORNERS.length; i++) {
            var a = CORNERS[i];
            var b = CORNERS[(i + 1) % CORNERS.length];
            connectionGlow(borderGlow, matrix, a[0] * half, a[1] * half, b[0] * half, b[1] * half,
                    y, brightness * reveal, pulse, PARCHMENT);
        }
        var core = buffers.getBuffer(CORE);
        for (int i = 0; i < CORNERS.length; i++) {
            var a = CORNERS[i];
            var b = CORNERS[(i + 1) % CORNERS.length];
            coreLine(core, matrix, a[0] * half, a[1] * half, b[0] * half, b[1] * half,
                    y, 0.0045f + pulse * 0.0015f, PARCHMENT, brightness * reveal * 0.65f);
        }
        for (int i = 0; i < 64; i++) {
            double a = i * Math.PI * 2 / 64, b = (i + 1) * Math.PI * 2 / 64;
            float radius = half * 0.87f;
            coreLine(core, matrix, (float) Math.cos(a) * radius, (float) Math.sin(a) * radius,
                    (float) Math.cos(b) * radius, (float) Math.sin(b) * radius, y, 0.003f, BLUE, brightness * reveal * 0.18f);
            if (i % 4 == 0) {
                float outer = radius + (i % 16 == 0 ? 0.07f : 0.035f);
                coreLine(core, matrix, (float) Math.cos(a) * radius, (float) Math.sin(a) * radius,
                        (float) Math.cos(a) * outer, (float) Math.sin(a) * outer, y, 0.004f, GOLD, brightness * reveal * 0.3f);
            }
        }
    }

    private static void drawConnections(VertexConsumer consumer, Matrix4f matrix, StargazingChart chart, float age,
                                        float half, float y, float brightness, float pulse, boolean glow) {
        for (int i = 0; i < chart.edges().size(); i++) {
            var edge = chart.edges().get(i);
            var a = chart.stars().get(edge.a());
            var b = chart.stars().get(edge.b());
            float appear = Math.min(starReveal(age, edge.a(), chart.stars().size()), starReveal(age, edge.b(), chart.stars().size()));
            float progress = Mth.clamp((age - 5 - i * 12f / chart.edges().size()) / 3, 0, 1);
            float ax = a.x() * half, az = a.z() * half;
            float bx = Mth.lerp(progress, ax, b.x() * half), bz = Mth.lerp(progress, az, b.z() * half);
            float strength = brightness * appear;
            if (glow) {
                connectionGlow(consumer, matrix, ax, az, bx, bz, y, strength, pulse, BLUE);
            } else {
                coreLine(consumer, matrix, ax, az, bx, bz, y, 0.0045f + pulse * 0.0015f, BLUE, strength * 0.65f);
            }
        }
    }

    private static float starReveal(float age, int index, int count) {
        return Mth.clamp((age - 2 - index * 10f / count) / 4, 0, 1);
    }

    private static void connectionGlow(VertexConsumer consumer, Matrix4f matrix, float ax, float az, float bx, float bz,
                                       float y, float strength, float pulse, Tint tint) {
        // 境界も星座の線と同じ光量・幅で描き、攻撃範囲だけが背景装飾へ埋もれないようにする。
        glowLine(consumer, matrix, ax, az, bx, bz, y, 0.075f + pulse * 0.025f, strength * 0.55f, tint);
        glowLine(consumer, matrix, ax, az, bx, bz, y, 0.18f + pulse * 0.04f, strength * 0.10f, tint);
    }

    private static void glowLine(VertexConsumer consumer, Matrix4f matrix, float ax, float az, float bx, float bz,
                                 float y, float width, float strength, Tint tint) {
        float length = Mth.sqrt((bx - ax) * (bx - ax) + (bz - az) * (bz - az));
        if (length < 0.0001f || strength <= 0) return;
        float dx = (bx - ax) / length, dz = (bz - az) / length;
        // 元画像の端に残る明るさと長手方向の切断面は、画像を加工せず頂点 RGB で消す。
        for (int along = 0; along < 3; along++) {
            float start = along == 0 ? -width : along == 1 ? 0 : length;
            float end = along == 0 ? 0 : along == 1 ? length : length + width;
            float startFade = along == 0 ? 0 : 1, endFade = along == 2 ? 0 : 1;
            for (int across = 0; across < WIDTH_STEPS.length - 1; across++) {
                float left = WIDTH_STEPS[across], right = WIDTH_STEPS[across + 1];
                glowVertex(consumer, matrix, ax + dx * start - dz * left * width, y, az + dz * start + dx * left * width,
                        (left + 1) * 0.5f, 0, tint, strength * startFade * EDGE_FADE[across]);
                glowVertex(consumer, matrix, ax + dx * start - dz * right * width, y, az + dz * start + dx * right * width,
                        (right + 1) * 0.5f, 0, tint, strength * startFade * EDGE_FADE[across + 1]);
                glowVertex(consumer, matrix, ax + dx * end - dz * right * width, y, az + dz * end + dx * right * width,
                        (right + 1) * 0.5f, 1, tint, strength * endFade * EDGE_FADE[across + 1]);
                glowVertex(consumer, matrix, ax + dx * end - dz * left * width, y, az + dz * end + dx * left * width,
                        (left + 1) * 0.5f, 1, tint, strength * endFade * EDGE_FADE[across]);
            }
        }
    }

    private static void starHalo(VertexConsumer consumer, Matrix4f matrix, float x, float z, float y,
                                 float radius, float strength, Tint tint) {
        if (strength <= 0) return;
        glowVertex(consumer, matrix, x - radius, y, z - radius, 0, 0, tint, strength);
        glowVertex(consumer, matrix, x + radius, y, z - radius, 1, 0, tint, strength);
        glowVertex(consumer, matrix, x + radius, y, z + radius, 1, 1, tint, strength);
        glowVertex(consumer, matrix, x - radius, y, z + radius, 0, 1, tint, strength);
    }

    private static void coreLine(VertexConsumer consumer, Matrix4f matrix, float ax, float az, float bx, float bz,
                                 float y, float width, Tint tint, float strength) {
        float length = Mth.sqrt((bx - ax) * (bx - ax) + (bz - az) * (bz - az));
        if (length < 0.00001f || strength <= 0) return;
        float dx = -(bz - az) / length * width, dz = (bx - ax) / length * width;
        coreVertex(consumer, matrix, ax + dx, y, az + dz, tint, strength);
        coreVertex(consumer, matrix, ax - dx, y, az - dz, tint, strength);
        coreVertex(consumer, matrix, bx - dx, y, bz - dz, tint, strength);
        coreVertex(consumer, matrix, bx + dx, y, bz + dz, tint, strength);
    }

    private static void glowVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
                                   float u, float v, Tint tint, float strength) {
        tint(consumer.addVertex(matrix, x, y, z).setUv(u, v), tint, strength);
    }

    private static void coreVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, Tint tint, float strength) {
        tint(consumer.addVertex(matrix, x, y, z), tint, strength);
    }

    private static void tint(VertexConsumer consumer, Tint tint, float strength) {
        // 弱い光輪の alpha を下げると標準 shader の discard に裾が切られるため、光量は RGB に乗せる。
        consumer.setColor(Mth.clamp(tint.red * strength, 0, 1),
                Mth.clamp(tint.green * strength, 0, 1), Mth.clamp(tint.blue * strength, 0, 1), 1);
    }

    private record Tint(float red, float green, float blue) {}

    private static void previewVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float alpha) {
        consumer.addVertex(matrix, x, y, z).setColor(0.27f, 0.42f, 0.82f, alpha);
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "textures/" + path + ".png");
    }

    @Override
    public boolean shouldRender(@NotNull StargazingField field, @NotNull Frustum frustum, double x, double y, double z) {
        return field.shouldRender(x, y, z) && frustum.isVisible(field.area().bounds().inflate(CULLING_MARGIN));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull StargazingField field) { return LINE_TEXTURE; }

    private static final class Types extends RenderStateShard {
        private static final ShaderStateShard TEXTURED_GLOW_SHADER = new ShaderStateShard(GameRenderer::getPositionTexColorShader);
        private static final TransparencyStateShard GLOW_BLEND = new TransparencyStateShard("stargazing_glow", () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        }, () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        });

        private Types() { super("stargazing", () -> {}, () -> {}); }

        private static RenderType preview() {
            return RenderType.create("stargazing_preview", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
                    RenderType.CompositeState.builder().setShaderState(POSITION_COLOR_SHADER).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).createCompositeState(false));
        }

        private static RenderType core() {
            return RenderType.create("stargazing_core", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 4096, false, false,
                    RenderType.CompositeState.builder().setShaderState(POSITION_COLOR_SHADER).setTransparencyState(GLOW_BLEND)
                            .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).createCompositeState(false));
        }

        private static RenderType glow(String name, ResourceLocation texture) {
            return RenderType.create(name, DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 16384, false, false,
                    RenderType.CompositeState.builder().setShaderState(TEXTURED_GLOW_SHADER)
                            .setTextureState(new TextureStateShard(texture, true, false)).setTransparencyState(GLOW_BLEND)
                            .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).createCompositeState(false));
        }
    }
}
