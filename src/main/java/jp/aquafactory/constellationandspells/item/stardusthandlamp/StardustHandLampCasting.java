package jp.aquafactory.constellationandspells.item.stardusthandlamp;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class StardustHandLampCasting {
    public static final String SELECTION_SLOT = ConstellationnSpells.MODID + ":stardust_hand_lamp";

    private StardustHandLampCasting() {}

    public static ItemStack physicalOffhand(Player player) {
        // BetterCombatの両手武器による論理オフハンドの隠蔽を受けず、実際の所持品を参照する。
        var inventory = player.getInventory().offhand;
        return inventory.isEmpty() ? ItemStack.EMPTY : inventory.getFirst();
    }

    public static @Nullable HeldLamp find(Player player) {
        var offhand = physicalOffhand(player);
        if (offhand.is(ItemRegistry.STARDUST_HAND_LAMP.get())) {
            return new HeldLamp(offhand, EquipmentSlot.OFFHAND);
        }
        var mainhand = player.getMainHandItem();
        return mainhand.is(ItemRegistry.STARDUST_HAND_LAMP.get())
                ? new HeldLamp(mainhand, EquipmentSlot.MAINHAND) : null;
    }

    public static boolean suppressDirectUse(Player player, InteractionHand hand) {
        // Iron'sの杖はclientでFAILを返してオフハンドへ進むため、両側でランプの二重詠唱を抑止する。
        return hand == InteractionHand.OFF_HAND
                && player.getMainHandItem().has(ComponentRegistry.CASTING_IMPLEMENT);
    }

    public static boolean isLampSelection(ItemStack castingItem, Player player, CastSource source, String slot) {
        if (SELECTION_SLOT.equals(slot)) {
            return true;
        }
        if (source != CastSource.SWORD || !castingItem.has(ComponentRegistry.CASTING_IMPLEMENT)) {
            return false;
        }
        // 杖の右クリックではslotが杖の手へ置換されるので、開始時の選択元を取り直す。
        var selection = new SpellSelectionManager(player).getSelection();
        return selection != null && SELECTION_SLOT.equals(selection.slot);
    }

    public record HeldLamp(ItemStack stack, EquipmentSlot slot) {
        public boolean isStillHeld(Player player) {
            var held = slot == EquipmentSlot.OFFHAND ? physicalOffhand(player) : player.getMainHandItem();
            return held == stack && stack.is(ItemRegistry.STARDUST_HAND_LAMP.get());
        }
    }

    // 詠唱中だけ個体を追跡する。アイテムへの魔法保存やclientへのシリアライズは不要。
    public record CastData(HeldLamp lamp) implements ICastData {
        @Override
        public void reset() {}
    }
}
