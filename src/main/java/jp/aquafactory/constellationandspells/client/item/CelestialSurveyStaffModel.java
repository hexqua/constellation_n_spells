package jp.aquafactory.constellationandspells.client.item;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.item.CelestialSurveyStaffItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class CelestialSurveyStaffModel extends GeoModel<CelestialSurveyStaffItem> {
    private static final ResourceLocation MODEL = resource("geo/celestial_survey_staff.geo.json");
    private static final ResourceLocation TEXTURE = resource("textures/geo/celestial_survey_staff.png");
    private static final ResourceLocation ANIMATION = resource("animations/celestial_survey_staff.animation.json");

    @Override
    public ResourceLocation getModelResource(CelestialSurveyStaffItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(CelestialSurveyStaffItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(CelestialSurveyStaffItem animatable) {
        return ANIMATION;
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, path);
    }
}
