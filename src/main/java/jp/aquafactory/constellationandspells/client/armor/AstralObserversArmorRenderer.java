package jp.aquafactory.constellationandspells.client.armor;

import jp.aquafactory.constellationandspells.item.armor.AstralObserversArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public final class AstralObserversArmorRenderer extends GeoArmorRenderer<AstralObserversArmorItem> {
    public AstralObserversArmorRenderer() {
        super(new AstralObserversArmorModel());
    }
}
