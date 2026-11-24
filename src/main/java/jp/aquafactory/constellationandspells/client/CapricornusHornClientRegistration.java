package jp.aquafactory.constellationandspells.client;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.client.render.ExtrudedSpriteRenderer;
import jp.aquafactory.constellationandspells.registry.EntityRegistry;
import jp.aquafactory.constellationandspells.spell.capricornushorn.CapricornusHornShakePayload;
import jp.aquafactory.constellationandspells.spell.capricornushorn.client.CapricornusHornRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = ConstellationnSpells.MODID, value = Dist.CLIENT)
public final class CapricornusHornClientRegistration {
    private static final int SHAKE_TICKS = 12;
    @Nullable
    private static ClientLevel shakeLevel;
    private static long shakeStart;

    private CapricornusHornClientRegistration() {}

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> CapricornusHornShakePayload.setClientHandler(CapricornusHornClientRegistration::startShake));
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.CAPRICORNUS_HORN.get(), CapricornusHornRenderer::new);
    }

    @SubscribeEvent
    public static void reloadListeners(RegisterClientReloadListenersEvent event) {
        ResourceManagerReloadListener sprites = manager -> ExtrudedSpriteRenderer.clearCache();
        event.registerReloadListener(sprites);
    }

    private static void startShake() {
        shakeLevel = Minecraft.getInstance().level;
        if (shakeLevel != null) shakeStart = shakeLevel.getGameTime();
    }

    @SubscribeEvent
    public static void cameraShake(ViewportEvent.ComputeCameraAngles event) {
        if (shakeLevel == null) return;
        float age = (float) (shakeLevel.getGameTime() - shakeStart + event.getPartialTick());
        if (shakeLevel != Minecraft.getInstance().level || age >= SHAKE_TICKS) {
            shakeLevel = null;
            return;
        }

        float strength = 0.4f * (1 - Mth.clamp(age / SHAKE_TICKS, 0, 1));
        event.setYaw(event.getYaw() + Mth.sin(age * 2.1f) * strength);
        event.setPitch(event.getPitch() + Mth.cos(age * 1.7f) * strength);
        event.setRoll(event.getRoll() + Mth.sin(age * 2.4f) * strength);
    }

    @SubscribeEvent
    public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        shakeLevel = null;
    }
}
