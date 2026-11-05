package jp.aquafactory.constellationandspells.item.armor;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.client.armor.AstralObserversArmorRenderProvider;
import jp.aquafactory.constellationandspells.registry.ArmorMaterialRegistry;
import jp.aquafactory.constellationandspells.registry.AttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

import java.util.function.Consumer;

public final class AstralObserversArmorItem extends ImbuableGeoArmorItem {
    private static final ResourceLocation SET_ID =
            ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "astral_observers");

    public AstralObserversArmorItem(Type type, Properties properties) {
        super(ArmorMaterialRegistry.ASTRAL_OBSERVERS, type,
                properties.durability(SchoolArmorProperties.durability(type)),
                () -> SchoolArmorProperties.attributes(SET_ID, type, AttributeRegistry.ZODIAC_SPELL_POWER),
                Type.CHESTPLATE, 1);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        // 描画 provider が要求されるまで client 側クラスを生成しない.
        consumer.accept(AstralObserversArmorRenderProvider.create());
    }
}
