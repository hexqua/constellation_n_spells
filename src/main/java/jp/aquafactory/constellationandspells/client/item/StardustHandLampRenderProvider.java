package jp.aquafactory.constellationandspells.client.item;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

public final class StardustHandLampRenderProvider {
    private StardustHandLampRenderProvider() {}

    public static GeoRenderProvider create() {
        return new GeoRenderProvider() {
            private StardustHandLampRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (renderer == null) {
                    renderer = new StardustHandLampRenderer();
                }
                return renderer;
            }
        };
    }
}
