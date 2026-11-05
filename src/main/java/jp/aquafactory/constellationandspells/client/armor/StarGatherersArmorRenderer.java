package jp.aquafactory.constellationandspells.client.armor;

import jp.aquafactory.constellationandspells.item.armor.StarGatherersArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public final class StarGatherersArmorRenderer extends GeoArmorRenderer<StarGatherersArmorItem> {
    public StarGatherersArmorRenderer() {
        super(new StarGatherersArmorModel());
    }
}
