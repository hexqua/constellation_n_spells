package jp.aquafactory.constellationandspells.item.stardusthandlamp;

import io.redspace.ironsspellbooks.api.spells.CastSource;
import jp.aquafactory.constellationandspells.client.item.StardustHandLampRenderProvider;
import jp.aquafactory.constellationandspells.registry.SpellRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class StardustHandLampItem extends Item implements GeoItem {
    private static final RawAnimation IDLE_ORB = RawAnimation.begin().thenLoop("idle_orb");
    private static final RawAnimation IDLE_TORCH = RawAnimation.begin().thenLoop("idle_torch");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public StardustHandLampItem(Properties properties) {
        super(properties.durability(59));
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 15;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return enchantment.is(Enchantments.UNBREAKING) || enchantment.is(Enchantments.MENDING)
                || enchantment.is(Enchantments.VANISHING_CURSE);
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return enchantment.is(Enchantments.UNBREAKING);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (StardustHandLampCasting.suppressDirectUse(player, hand)) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.consume(stack);
        }
        var spell = SpellRegistry.STARDUST.get();
        boolean started = spell.attemptInitiateCast(stack, spell.getLevelFor(1, player), level, player,
                CastSource.SWORD, true, StardustHandLampCasting.SELECTION_SLOT);
        return started ? InteractionResultHolder.consume(stack) : InteractionResultHolder.fail(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        var spell = SpellRegistry.STARDUST.get();
        var spellName = Component.translatable(spell.getComponentId()).append(" 1")
                .withStyle(spell.getSchoolType().getDisplayName().getStyle());
        tooltip.add(Component.translatable(getDescriptionId() + ".tooltip.desc_1", spellName).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(getDescriptionId() + ".tooltip.desc_2").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "orb", 0, state -> state.setAndContinue(IDLE_ORB)),
                new AnimationController<>(this, "torch", 0, state -> state.setAndContinue(IDLE_TORCH)));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        // 専用サーバーでは描画クラスを生成しない。
        consumer.accept(StardustHandLampRenderProvider.create());
    }
}
