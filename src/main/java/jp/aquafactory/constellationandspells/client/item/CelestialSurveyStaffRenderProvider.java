package jp.aquafactory.constellationandspells.client.item;

import jp.aquafactory.constellationandspells.item.CelestialSurveyStaffItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class CelestialSurveyStaffRenderProvider {
    private CelestialSurveyStaffRenderProvider() {
    }

    public static GeoRenderProvider create() {
        return new GeoRenderProvider() {
            private GeoItemRenderer<CelestialSurveyStaffItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (renderer == null) {
                    renderer = new GeoItemRenderer<>(new CelestialSurveyStaffModel());
                }
                return renderer;
            }
        };
    }
}
