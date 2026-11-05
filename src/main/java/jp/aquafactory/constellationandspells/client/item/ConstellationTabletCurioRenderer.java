package jp.aquafactory.constellationandspells.client.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public final class ConstellationTabletCurioRenderer implements ICurioRenderer {
    private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack, SlotContext slotContext, PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent, MultiBufferSource bufferSource, int light,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch) {
        if (!(renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel)) {
            return;
        }

        poseStack.pushPose();
        humanoidModel.body.translateAndRotate(poseStack);
        // Iron's本体のSpellBookCurioRendererに合わせ、胸装備による腰位置の補正も維持する.
        boolean hasChestEquipment = slotContext.entity() != null
                && !slotContext.entity().getItemBySlot(EquipmentSlot.CHEST).isEmpty();
        poseStack.translate((hasChestEquipment ? -5.5 : -4.5) * .0625f, 9 * .0625f, 0);

        // モデル形状上そのまま描画するとY軸が90度ずれるため、額縁用FIXEDを変えずに板面を右腰の外側へ向ける.
        // 腰位置で胴体のY軸を基準に回す.
        poseStack.mulPose(Axis.YP.rotationDegrees(-90));
        poseStack.mulPose(Axis.YP.rotation(Mth.PI));
        poseStack.mulPose(Axis.ZP.rotation(Mth.PI - 5 * Mth.DEG_TO_RAD));
        poseStack.scale(.625f, .625f, .625f);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, null, 0);
        poseStack.popPose();
    }
}
