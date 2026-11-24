package jp.aquafactory.constellationandspells.spell.capricornushorn.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import jp.aquafactory.constellationandspells.spell.capricornushorn.CapricornusHorn;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class CapricornusHornRenderer extends EntityRenderer<CapricornusHorn> {
    private static final int SEGMENTS = 48;
    private static final int WAVE_COLOR = 0xFFD393;
    private static final int CORE_COLOR = 0xFFF0C2;
    private static final int FIRST_WAVE_CORE_TICKS = 10;
    private static final int AFTERWAVE_TICKS = 24;
    private static final float DEGREES_PER_SEED_UNIT = 360.0f / 0x10000;
    private static final RenderType WAVE = WaveRenderState.create();
    private final ItemStack model = new ItemStack(Items.GOAT_HORN);
    private final ItemRenderer items;

    public CapricornusHornRenderer(EntityRendererProvider.Context context) {
        super(context);
        items = context.getItemRenderer();
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
        // todo:現在バニラの角笛をアイテム描画しているがExtrudedSpriteを移植した上でオリジナル角笛にする.
        items.renderStatic(model, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers,
                horn.level(), horn.getId());
        pose.popPose();

        var consumer = buffers.getBuffer(WAVE);
        float coreAlpha = 0.85f * (1 - Mth.clamp(age / FIRST_WAVE_CORE_TICKS, 0, 1));
        wave(pose, consumer, age, horn.radius(), 20, 0.34f, coreAlpha);
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
        wave(pose, consumer, age, maxRadius, AFTERWAVE_TICKS, opacity, 0);
        pose.popPose();
    }

    private static void wave(PoseStack pose, VertexConsumer consumer, float age, float maxRadius, int duration,
                             float opacity, float coreAlpha) {
        if (age < 0 || age >= duration) return;

        float progress = age / duration;
        float radius = Math.max(0.2f, maxRadius * (1 - (1 - progress) * (1 - progress)));
        float alpha = opacity * (1 - progress);
        ring(pose.last().pose(), consumer, radius, alpha, coreAlpha);

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(38));
        pose.mulPose(Axis.XP.rotationDegrees(60));
        ring(pose.last().pose(), consumer, radius, alpha * 0.65f, coreAlpha * 0.75f);
        pose.mulPose(Axis.ZP.rotationDegrees(90));
        ring(pose.last().pose(), consumer, radius, alpha * 0.65f, coreAlpha * 0.75f);
        pose.popPose();
    }

    private static void ring(Matrix4f matrix, VertexConsumer consumer, float radius, float alpha, float coreAlpha) {
        float inner = radius * 0.92f;
        float middle = (inner + radius) * 0.5f;
        float shoulderWidth = radius * 0.018f * coreAlpha;
        float coreHalfWidth = shoulderWidth * 0.5f;
        // 芯を帯の一部として描くことで、同一平面で半透明の面が重ならないようにする。
        int bandCenter = ARGB32.color((int) (alpha * 255), WAVE_COLOR);
        int shoulders = ARGB32.color((int) (alpha * (1 - shoulderWidth / (middle - inner)) * 255), WAVE_COLOR);
        int core = ARGB32.lerp(coreAlpha, bandCenter, ARGB32.color(255, CORE_COLOR));
        for (int i = 0; i < SEGMENTS; i++) {
            float from = i * (Mth.TWO_PI / SEGMENTS);
            float to = (i + 1) * (Mth.TWO_PI / SEGMENTS);
            float ax = Mth.cos(from);
            float az = Mth.sin(from);
            float bx = Mth.cos(to);
            float bz = Mth.sin(to);
            strip(matrix, consumer, ax, az, bx, bz, inner, middle - shoulderWidth, WAVE_COLOR, shoulders);
            if (coreAlpha > 0) {
                strip(matrix, consumer, ax, az, bx, bz, middle - shoulderWidth, middle - coreHalfWidth, shoulders, core);
                strip(matrix, consumer, ax, az, bx, bz, middle - coreHalfWidth, middle + coreHalfWidth, core, core);
                strip(matrix, consumer, ax, az, bx, bz, middle + coreHalfWidth, middle + shoulderWidth, core, shoulders);
            }
            strip(matrix, consumer, ax, az, bx, bz, middle + shoulderWidth, radius, shoulders, WAVE_COLOR);
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
    protected int getBlockLightLevel(@NotNull CapricornusHorn horn, @NotNull BlockPos pos) {
        return Math.max(7, super.getBlockLightLevel(horn, pos));
    }

    @Override
    public boolean shouldRender(@NotNull CapricornusHorn horn, @NotNull Frustum frustum, double x, double y, double z) {
        return horn.shouldRender(x, y, z) && frustum.isVisible(horn.getBoundingBox().inflate(horn.radius()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull CapricornusHorn horn) {
        return InventoryMenu.BLOCK_ATLAS;
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
