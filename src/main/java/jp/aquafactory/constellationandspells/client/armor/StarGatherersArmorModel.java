package jp.aquafactory.constellationandspells.client.armor;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.item.armor.StarGatherersArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class StarGatherersArmorModel extends GeoModel<StarGatherersArmorItem> {
    private static final ResourceLocation MODEL = resource("geo/star_gatherers_armor.geo.json");
    private static final ResourceLocation TEXTURE = resource("textures/geo/star_gatherers_armor.png");
    private static final ResourceLocation ANIMATION = resource("animations/star_gatherers_armor.animation.json");

    @Override
    public ResourceLocation getModelResource(StarGatherersArmorItem armor) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(StarGatherersArmorItem armor) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(StarGatherersArmorItem armor) {
        return ANIMATION;
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, path);
    }
}
