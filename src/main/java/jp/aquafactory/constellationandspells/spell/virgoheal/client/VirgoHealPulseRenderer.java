package jp.aquafactory.constellationandspells.spell.virgoheal.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.particle.AdditiveGlowParticleOptions;
import jp.aquafactory.constellationandspells.registry.ParticleRegistry;
import jp.aquafactory.constellationandspells.spell.virgoheal.VirgoHealPulsePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayDeque;
import java.util.Deque;

@EventBusSubscriber(modid = ConstellationnSpells.MODID, value = Dist.CLIENT)
public final class VirgoHealPulseRenderer {
    private static final int LIFETIME_TICKS = 14;
    private static final int SEGMENTS = 64;
    private static final int MAX_PULSES = 128;
    private static final int RED = 162;
    private static final int GREEN = 201;
    private static final int BLUE = 135;
    private static final RenderType WAVE = WaveRenderState.create();
    private static final Deque<Pulse> PULSES = new ArrayDeque<>();
    private static @Nullable ClientLevel pulseLevel;

    private VirgoHealPulseRenderer() {}

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> VirgoHealPulsePayload.setClientHandler(VirgoHealPulseRenderer::enqueue));
    }

    private static void enqueue(VirgoHealPulsePayload payload) {
        var level = Minecraft.getInstance().level;
        discardExpired(level);
        if (level == null) {
            return;
        }
        if (PULSES.size() >= MAX_PULSES) {
            PULSES.removeFirst();
        }
        PULSES.addLast(new Pulse(payload.center(), payload.radius(), level.getGameTime()));
        var center = payload.center();
        var flash = particle(ParticleRegistry.ADDITIVE_GLOWING_SPARK.get(), 0.16f, 2);
        level.addParticle(flash, center.x, center.y, center.z, 0, 0.015, 0);

        var recovery = particle(ParticleRegistry.ADDITIVE_SPARK.get(), 0.09f, 2);
        for (var id : payload.healedEntityIds()) {
            var entity = level.getEntity(id);
            if (entity != null) {
                spawnRecoverySparks(level, entity.getBoundingBox(), recovery);
            }
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        discardExpired(level);
        if (level == null || minecraft.isPaused()) {
            return;
        }
        long gameTime = level.getGameTime();
        for (var pulse : PULSES) {
            long age = gameTime - pulse.startTick();
            // 拡大中の縁から数回に分けてこぼし、横からでも波の広がりを見せる。
            if (age == 2 || age == 4 || age == 6) {
                spawnRimSparks(level, pulse, age);
            }
        }
    }

    private static void spawnRimSparks(ClientLevel level, Pulse pulse, float age) {
        float rim = radiusAt(pulse, remainingAt(age));
        float angle = level.random.nextFloat() * Mth.TWO_PI;
        var center = pulse.center();
        var spark = particle(ParticleRegistry.ADDITIVE_SPARK.get(), 0.07f, 0);
        for (int i = 0; i < 2; i++) {
            float x = Mth.cos(angle);
            float z = Mth.sin(angle);
            level.addParticle(spark, center.x + x * rim, center.y, center.z + z * rim,
                    x * 0.04, -0.065, z * 0.04);
            angle += Mth.PI;
        }
    }

    private static void spawnRecoverySparks(ClientLevel level, AABB box, AdditiveGlowParticleOptions spark) {
        var center = box.getCenter();
        float angle = level.random.nextFloat() * Mth.TWO_PI;
        for (int i = 0; i < 6; i++) {
            float x = Mth.cos(angle);
            float z = Mth.sin(angle);
            double y = box.minY + box.getYsize() * (0.20 + level.random.nextDouble() * 0.35);
            level.addParticle(spark, center.x + x * box.getXsize() * 0.55, y,
                    center.z + z * box.getZsize() * 0.55, x * 0.025,
                    0.045 + level.random.nextDouble() * 0.045, z * 0.025);
            angle += Mth.TWO_PI / 6;
        }
    }

    private static AdditiveGlowParticleOptions particle(ParticleType<AdditiveGlowParticleOptions> type,
                                                       float size, int whitenTicks) {
        return new AdditiveGlowParticleOptions(type, size, RED / 255f, GREEN / 255f, BLUE / 255f,
                whitenTicks, 6, 3, 0.85f, 1.15f, 0.65f, 0.90f, 0.08f, 0.45f, 0.5f, true);
    }

    private static void discardExpired(@Nullable ClientLevel level) {
        if (pulseLevel != level) {
            PULSES.clear();
            pulseLevel = level;
        }
        if (level != null) {
            long gameTime = level.getGameTime();
            PULSES.removeIf(pulse -> gameTime - pulse.startTick() >= LIFETIME_TICKS);
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        discardExpired(level);
        if (level == null || PULSES.isEmpty()) {
            return;
        }

        var buffers = minecraft.renderBuffers().bufferSource();
        var buffer = buffers.getBuffer(WAVE);
        var pose = event.getPoseStack();
        var camera = event.getCamera().getPosition();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        for (var pulse : PULSES) {
            float age = level.getGameTime() - pulse.startTick() + partialTick;
            float remaining = remainingAt(age);
            float radius = radiusAt(pulse, remaining);
            var offset = pulse.center().subtract(camera);
            pose.pushPose();
            pose.translate(offset.x, offset.y, offset.z);
            drawRing(buffer, pose.last().pose(), radius, Math.round(185 * remaining));
            pose.popPose();
        }
        buffers.endBatch(WAVE);
    }

    private static float remainingAt(float age) {
        return 1.0f - Mth.clamp(age / LIFETIME_TICKS, 0.0f, 1.0f);
    }

    private static float radiusAt(Pulse pulse, float remaining) {
        return Mth.lerp(1.0f - remaining * remaining, 0.35f, pulse.radius());
    }

    private static void drawRing(VertexConsumer buffer, Matrix4f matrix, float radius, int opacity) {
        float inner = Math.max(0.0f, radius - 0.32f);
        float crest = radius - 0.10f;
        for (int segment = 0; segment < SEGMENTS; segment++) {
            float start = Mth.TWO_PI * segment / SEGMENTS;
            float end = Mth.TWO_PI * (segment + 1) / SEGMENTS;
            float x1 = Mth.cos(start);
            float z1 = Mth.sin(start);
            float x2 = Mth.cos(end);
            float z2 = Mth.sin(end);
            vertex(buffer, matrix, inner * x1, inner * z1, 0);
            vertex(buffer, matrix, inner * x2, inner * z2, 0);
            vertex(buffer, matrix, crest * x2, crest * z2, opacity);
            vertex(buffer, matrix, crest * x1, crest * z1, opacity);
            vertex(buffer, matrix, crest * x1, crest * z1, opacity);
            vertex(buffer, matrix, crest * x2, crest * z2, opacity);
            vertex(buffer, matrix, radius * x2, radius * z2, 0);
            vertex(buffer, matrix, radius * x1, radius * z1, 0);
        }
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix, float x, float z, int opacity) {
        buffer.addVertex(matrix, x, 0.0f, z).setColor(RED, GREEN, BLUE, opacity);
    }

    private static final class WaveRenderState extends RenderStateShard {
        // Fabulous の AFTER_PARTICLES でも、波の色をシーンの背景色へ直接加算する。
        private static final OutputStateShard WAVE_TARGET = new OutputStateShard("virgo_heal_wave_target", () -> {
            if (Minecraft.useShaderTransparency()) {
                Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
            }
        }, PARTICLES_TARGET::setupRenderState);

        private WaveRenderState() {
            super("virgo_heal_wave", () -> {}, () -> {});
        }

        private static RenderType create() {
            return RenderType.create("virgo_heal_wave", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS,
                    1536, false, true, RenderType.CompositeState.builder()
                            .setShaderState(POSITION_COLOR_SHADER)
                            .setTransparencyState(LIGHTNING_TRANSPARENCY)
                            .setCullState(NO_CULL)
                            .setDepthTestState(LEQUAL_DEPTH_TEST)
                            .setWriteMaskState(COLOR_WRITE)
                            .setOutputState(WAVE_TARGET)
                            .createCompositeState(false));
        }
    }

    private record Pulse(Vec3 center, float radius, long startTick) {}
}
