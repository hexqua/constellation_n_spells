package jp.aquafactory.constellationandspells.spell.virgoheal;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public record VirgoHealPulsePayload(Vec3 center, float radius, List<Integer> healedEntityIds) implements CustomPacketPayload {
    private static final Type<VirgoHealPulsePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "virgo_heal_pulse"));
    private static final StreamCodec<FriendlyByteBuf, VirgoHealPulsePayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVec3(payload.center());
                buffer.writeFloat(payload.radius());
                buffer.writeCollection(payload.healedEntityIds(), FriendlyByteBuf::writeVarInt);
            },
            buffer -> new VirgoHealPulsePayload(buffer.readVec3(), buffer.readFloat(),
                    buffer.readList(FriendlyByteBuf::readVarInt)));
    private static Consumer<VirgoHealPulsePayload> clientHandler = ignored -> {};

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("2").playToClient(TYPE, STREAM_CODEC, (payload, context) -> clientHandler.accept(payload));
    }

    public static void setClientHandler(Consumer<VirgoHealPulsePayload> handler) {
        clientHandler = handler;
    }

    @Override
    public @NotNull Type<VirgoHealPulsePayload> type() {
        return TYPE;
    }
}
