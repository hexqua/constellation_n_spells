package jp.aquafactory.constellationandspells.mixin;

import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// school 名から属性 ID を推測せず、独自の威力計算を介さない生の属性を参照する。
@Mixin(value = SchoolType.class, remap = false)
public interface SchoolTypeAccessor {
    @Accessor("powerAttribute")
    Holder<Attribute> constellationandspells$getPowerAttribute();
}
