package jp.aquafactory.constellationandspells.item.armor;

import com.google.common.base.Suppliers;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Supplier;

public abstract class BaseGeoArmorItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<ItemAttributeModifiers> modifiers;

    protected BaseGeoArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties,
                              Supplier<ItemAttributeModifiers> additionalModifiers) {
        super(material, type, properties);
        // 登録中は素材・属性の Holder が未解決のため、標準属性との合成も初回参照まで遅延する。
        modifiers = Suppliers.memoize(() -> {
            var builder = ItemAttributeModifiers.builder();
            for (var entry : super.getDefaultAttributeModifiers().modifiers()) {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
            for (var entry : additionalModifiers.get().modifiers()) {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
            return builder.build();
        });
    }

    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
        return modifiers.get();
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 静止モデルにはアニメーションがないため、既定の idle 再生を要求しない。
    }
}
