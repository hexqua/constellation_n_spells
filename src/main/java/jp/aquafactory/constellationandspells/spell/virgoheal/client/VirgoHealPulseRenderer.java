package jp.aquafactory.constellationandspells.spell.virgoheal.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.spell.virgoheal.VirgoHealPulsePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
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
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        discardExpired(Minecraft.getInstance().level);
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
        // 色だけを書き込み、壁や床の深度で波を隠す。
        var renderType = RenderType.debugStructureQuads();
        var buffer = buffers.getBuffer(renderType);
        var pose = event.getPoseStack();
        var camera = event.getCamera().getPosition();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        for (var pulse : PULSES) {
            float age = level.getGameTime() - pulse.startTick() + partialTick;
            float remaining = 1.0f - Mth.clamp(age / LIFETIME_TICKS, 0.0f, 1.0f);
            float radius = Mth.lerp(1.0f - remaining * remaining, 0.35f, pulse.radius());
            var offset = pulse.center().subtract(camera);
            pose.pushPose();
            pose.translate(offset.x, offset.y + 0.05, offset.z);
            drawRing(buffer, pose.last().pose(), radius, Math.round(185 * remaining));
            pose.popPose();
        }
        buffers.endBatch(renderType);
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
        buffer.addVertex(matrix, x, 0.0f, z).setColor(92, 250, 117, opacity);
    }

    private record Pulse(Vec3 center, float radius, long startTick) {}
}
