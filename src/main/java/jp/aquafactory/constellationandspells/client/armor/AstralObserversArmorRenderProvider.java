package jp.aquafactory.constellationandspells.client.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

public final class AstralObserversArmorRenderProvider {
    private AstralObserversArmorRenderProvider() {
    }

    public static GeoRenderProvider create() {
        return new GeoRenderProvider() {
            private AstralObserversArmorRenderer renderer;

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(
                    @Nullable T livingEntity, ItemStack stack, @Nullable EquipmentSlot slot,
                    @Nullable HumanoidModel<T> original) {
                if (renderer == null) {
                    renderer = new AstralObserversArmorRenderer();
                }
                return renderer;
            }
        };
    }
}
