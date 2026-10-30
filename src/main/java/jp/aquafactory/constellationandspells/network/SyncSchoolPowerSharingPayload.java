package jp.aquafactory.constellationandspells.network;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.magic.SchoolPowerSharingRules;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public record SyncSchoolPowerSharingPayload(SchoolPowerSharingRules rules) implements CustomPacketPayload {
    public static final Type<SyncSchoolPowerSharingPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "school_power_sharing"));
    public static final StreamCodec<FriendlyByteBuf, SyncSchoolPowerSharingPayload> STREAM_CODEC =
            StreamCodec.of(SyncSchoolPowerSharingPayload::write, SyncSchoolPowerSharingPayload::read);

    private static void write(FriendlyByteBuf buffer, SyncSchoolPowerSharingPayload payload) {
        buffer.writeBoolean(payload.rules.enabled());
        buffer.writeBoolean(payload.rules.autoDetectAstral());
        buffer.writeCollection(payload.rules.additionalSchools(), FriendlyByteBuf::writeResourceLocation);
        buffer.writeCollection(payload.rules.excludedSchools(), FriendlyByteBuf::writeResourceLocation);
    }

    private static SyncSchoolPowerSharingPayload read(FriendlyByteBuf buffer) {
        return new SyncSchoolPowerSharingPayload(new SchoolPowerSharingRules(
                buffer.readBoolean(), buffer.readBoolean(),
                buffer.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation),
                buffer.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation)));
    }

    @Override
    public @NotNull Type<SyncSchoolPowerSharingPayload> type() {
        return TYPE;
    }
}
