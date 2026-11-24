package jp.aquafactory.constellationandspells.spell.capricornushorn;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.jetbrains.annotations.NotNull;

public enum CapricornusHornShakePayload implements CustomPacketPayload {
    INSTANCE;

    private static final Type<CapricornusHornShakePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "capricornus_horn_shake"));
    private static final StreamCodec<FriendlyByteBuf, CapricornusHornShakePayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    private static Runnable clientHandler = () -> {};

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE, STREAM_CODEC, (payload, context) -> clientHandler.run());
    }

    public static void setClientHandler(Runnable handler) {
        clientHandler = handler;
    }

    @Override
    public @NotNull Type<CapricornusHornShakePayload> type() {
        return TYPE;
    }
}
