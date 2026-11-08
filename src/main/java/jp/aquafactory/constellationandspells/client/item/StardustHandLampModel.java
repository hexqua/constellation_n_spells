package jp.aquafactory.constellationandspells.client.item;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.item.stardusthandlamp.StardustHandLampItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class StardustHandLampModel extends GeoModel<StardustHandLampItem> {
    private static final ResourceLocation MODEL = resource("geo/stardust_hand_lamp.geo.json");
    private static final ResourceLocation TEXTURE = resource("textures/geo/stardust_hand_lamp.png");
    private static final ResourceLocation ANIMATION = resource("animations/stardust_hand_lamp.animation.json");

    @Override
    public ResourceLocation getModelResource(StardustHandLampItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(StardustHandLampItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(StardustHandLampItem animatable) {
        return ANIMATION;
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, path);
    }
}
