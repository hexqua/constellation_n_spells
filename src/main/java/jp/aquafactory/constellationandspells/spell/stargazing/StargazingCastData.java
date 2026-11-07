package jp.aquafactory.constellationandspells.spell.stargazing;

import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class StargazingCastData implements ICastDataSerializable {
    private StargazingArea area;
    private UUID preview;

    public StargazingCastData() {}
    public StargazingCastData(StargazingArea area) { this.area = area; }
    public StargazingArea area() { return area; }
    public UUID preview() { return preview; }
    public void setPreview(UUID preview) { this.preview = preview; }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeBoolean(area != null);
        if (area != null) {
            buffer.writeVec3(area.base());
            buffer.writeFloat(area.halfExtent());
            buffer.writeFloat(area.yaw());
        }
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        reset();
        if (buffer.readBoolean()) area = new StargazingArea(buffer.readVec3(), buffer.readFloat(), buffer.readFloat());
    }

    @Override
    public void reset() { area = null; preview = null; }

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        var tag = new CompoundTag();
        if (area != null) {
            tag.putDouble("X", area.base().x);
            tag.putDouble("Y", area.base().y);
            tag.putDouble("Z", area.base().z);
            tag.putFloat("HalfExtent", area.halfExtent());
            tag.putFloat("Yaw", area.yaw());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag tag) {
        reset();
        if (tag.contains("HalfExtent")) area = new StargazingArea(new Vec3(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z")),
                tag.getFloat("HalfExtent"), tag.getFloat("Yaw"));
    }
}
