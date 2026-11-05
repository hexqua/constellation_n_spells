package jp.aquafactory.constellationandspells.item;

import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import jp.aquafactory.constellationandspells.client.item.ConstellationTabletRenderProvider;
import jp.aquafactory.constellationandspells.registry.AttributeRegistry;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Rarity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MAX_MANA;

public final class ConstellationTabletItem extends SpellBook implements GeoItem {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation LECTERN = RawAnimation.begin().thenLoop("lectern");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ConstellationTabletItem(Properties properties) {
        super(12, properties.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant());
        withSpellbookAttributes(
                new AttributeContainer(MAX_MANA,
                        200, AttributeModifier.Operation.ADD_VALUE),
                new AttributeContainer(AttributeRegistry.ZODIAC_SPELL_POWER,
                        0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean isPerspectiveAware() {
        // 書見台とGUIの同時描画でも、片方のアニメーション状態が他方へ漏れないようにする.
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state ->
                state.setAndContinue(state.getData(DataTickets.ITEM_RENDER_PERSPECTIVE) == ItemDisplayContext.HEAD
                        ? LECTERN : IDLE)));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        // 描画クラスはclientがproviderを要求するまで生成しない。
        consumer.accept(ConstellationTabletRenderProvider.create());
    }
}
