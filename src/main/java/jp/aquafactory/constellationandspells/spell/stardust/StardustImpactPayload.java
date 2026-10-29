package jp.aquafactory.constellationandspells.spell.stardust;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public record StardustImpactPayload(Vec3 position, Vec3 incoming) implements CustomPacketPayload {
    public static final Type<StardustImpactPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "stardust_impact"));
    public static final StreamCodec<FriendlyByteBuf, StardustImpactPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> { buffer.writeVec3(payload.position); buffer.writeVec3(payload.incoming); },
            buffer -> new StardustImpactPayload(buffer.readVec3(), buffer.readVec3()));
    private static Consumer<StardustImpactPayload> clientHandler = ignored -> {};

    public static void register(RegisterPayloadHandlersEvent event) {
        // client 実装は client setup から注入し、専用サーバーでは描画クラスを解決しない。
        event.registrar("1").playToClient(TYPE, STREAM_CODEC, (payload, context) -> clientHandler.accept(payload));
    }

    public static void setClientHandler(Consumer<StardustImpactPayload> handler) { clientHandler = handler; }

    @Override
    public @NotNull Type<StardustImpactPayload> type() { return TYPE; }
}
