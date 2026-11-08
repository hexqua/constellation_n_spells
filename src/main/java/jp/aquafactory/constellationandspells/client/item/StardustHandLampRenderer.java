package jp.aquafactory.constellationandspells.client.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.aquafactory.constellationandspells.item.stardusthandlamp.StardustHandLampItem;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class StardustHandLampRenderer extends GeoItemRenderer<StardustHandLampItem> {
    public StardustHandLampRenderer() {
        super(new StardustHandLampModel());
    }

    @Override
    public RenderType getRenderType(StardustHandLampItem animatable, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        // 同一テクスチャ内の球殻と内部をまとめて半透明ソートし、中間アルファを保持する。
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public void renderCubesOfBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer,
                                  int packedLight, int packedOverlay, int colour) {
        super.renderCubesOfBone(poseStack, bone, buffer,
                "torch".equals(bone.getName()) ? LightTexture.FULL_BRIGHT : packedLight, packedOverlay, colour);
    }
}
