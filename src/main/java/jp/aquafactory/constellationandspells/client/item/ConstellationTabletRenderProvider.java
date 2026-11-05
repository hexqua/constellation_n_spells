package jp.aquafactory.constellationandspells.client.item;

import jp.aquafactory.constellationandspells.item.ConstellationTabletItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class ConstellationTabletRenderProvider {
    private ConstellationTabletRenderProvider() {
    }

    public static GeoRenderProvider create() {
        return new GeoRenderProvider() {
            private GeoItemRenderer<ConstellationTabletItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (renderer == null) {
                    renderer = new GeoItemRenderer<>(new ConstellationTabletModel());
                }
                return renderer;
            }
        };
    }
}
