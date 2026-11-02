package jp.aquafactory.constellationandspells.item.armor;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.client.armor.StarGatherersArmorRenderProvider;
import jp.aquafactory.constellationandspells.registry.ArmorMaterialRegistry;
import jp.aquafactory.constellationandspells.registry.AttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

import java.util.function.Consumer;

import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MAX_MANA;

public final class StarGatherersArmorItem extends BaseGeoArmorItem {
    public StarGatherersArmorItem(Type type, Properties properties) {
        super(ArmorMaterialRegistry.STAR_GATHERERS, type, properties.durability(durability(type)),
                () -> attributes(type));
    }

    private static int durability(Type type) {
        return switch (type) {
            case HELMET -> 165;
            case CHESTPLATE -> 240;
            case LEGGINGS -> 225;
            case BOOTS -> 195;
            case BODY -> throw new IllegalArgumentException("Star Gatherer's armor does not support body armor");
        };
    }

    private static ItemAttributeModifiers attributes(Type type) {
        var slot = EquipmentSlotGroup.bySlot(type.getSlot());
        var id = ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID,
                "star_gatherers." + type.getName());
        return ItemAttributeModifiers.builder()
                .add(MAX_MANA,
                        new AttributeModifier(id, 50, AttributeModifier.Operation.ADD_VALUE), slot)
                .add(AttributeRegistry.ZODIAC_SPELL_POWER,
                        new AttributeModifier(id, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slot)
                .build();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        // GeckoLib が client で描画 provider を要求した時点でのみ client クラスを生成する。
        consumer.accept(StarGatherersArmorRenderProvider.create());
    }
}
