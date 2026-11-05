package jp.aquafactory.constellationandspells.client.item;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.item.ConstellationTabletItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class ConstellationTabletModel extends GeoModel<ConstellationTabletItem> {
    private static final ResourceLocation MODEL = resource("geo/constellation_tablet.geo.json");
    private static final ResourceLocation TEXTURE = resource("textures/geo/constellation_tablet.png");
    private static final ResourceLocation ANIMATION = resource("animations/constellation_tablet.animation.json");

    @Override
    public ResourceLocation getModelResource(ConstellationTabletItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ConstellationTabletItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ConstellationTabletItem animatable) {
        return ANIMATION;
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, path);
    }
}
