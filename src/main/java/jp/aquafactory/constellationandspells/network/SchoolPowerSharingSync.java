package jp.aquafactory.constellationandspells.network;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.config.ServerConfig;
import jp.aquafactory.constellationandspells.magic.SchoolPowerSharing;
import jp.aquafactory.constellationandspells.magic.SchoolPowerSharingRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = ConstellationnSpells.MODID)
public final class SchoolPowerSharingSync {
    private SchoolPowerSharingSync() {
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener(SchoolPowerSharingSync::registerPayloads);
        eventBus.addListener(SchoolPowerSharingSync::onConfigLoading);
        eventBus.addListener(SchoolPowerSharingSync::onConfigReloading);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        // registrar の既定 MAIN handler により、client の状態差し替えは client thread で行う。
        event.registrar("1").playToClient(SyncSchoolPowerSharingPayload.TYPE,
                SyncSchoolPowerSharingPayload.STREAM_CODEC,
                (payload, context) -> SchoolPowerSharing.updateClient(payload.rules()));
    }

    private static void onConfigLoading(ModConfigEvent.Loading event) {
        refreshFromConfig(event);
    }

    private static void onConfigReloading(ModConfigEvent.Reloading event) {
        refreshFromConfig(event);
    }

    private static void refreshFromConfig(ModConfigEvent event) {
        var config = event.getConfig();
        if (config.getSpec() != ServerConfig.SPEC || config.getLoadedConfig() == null) {
            return;
        }
        // リモート接続中の client には currentServer がないため、標準同期の通知を再送しない。
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            var rules = ServerConfig.snapshot();
            // ファイル監視 thread からも通知されるため、解決・適用・送信を server thread に揃える。
            server.execute(() -> {
                if (ServerLifecycleHooks.getCurrentServer() == server && !server.isStopped()) {
                    publish(server, rules);
                }
            });
        }
    }

    private static void publish(MinecraftServer server, SchoolPowerSharingRules rules) {
        SchoolPowerSharing.updateServer(rules);
        var payload = new SyncSchoolPowerSharingPayload(rules);
        server.getPlayerList().getPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, payload));
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        // 設定読込時には registry が未完成でも、起動イベントの時点で対象を確定できる。
        SchoolPowerSharing.updateServer(ServerConfig.snapshot());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        SchoolPowerSharing.resetServer();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player,
                    new SyncSchoolPowerSharingPayload(SchoolPowerSharing.serverState().rules()));
        }
    }
}
