package jp.aquafactory.constellationandspells.client.armor;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.item.armor.AstralObserversArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class AstralObserversArmorModel extends GeoModel<AstralObserversArmorItem> {
    private static final ResourceLocation MODEL = resource("geo/astral_observers_armor.geo.json");
    private static final ResourceLocation TEXTURE = resource("textures/geo/astral_observers_armor.png");
    private static final ResourceLocation ANIMATION = resource("animations/astral_observers_armor.animation.json");

    @Override
    public ResourceLocation getModelResource(AstralObserversArmorItem armor) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(AstralObserversArmorItem armor) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(AstralObserversArmorItem armor) {
        return ANIMATION;
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, path);
    }
}
