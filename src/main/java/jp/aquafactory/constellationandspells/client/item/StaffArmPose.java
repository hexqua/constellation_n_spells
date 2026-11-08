package jp.aquafactory.constellationandspells.client.item;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

public final class StaffArmPose {
    // Iron'sの通常の杖と同じ腕角度にし、描画モデルの位置調整とは独立させる.
    public static final EnumProxy<HumanoidModel.ArmPose> STAFF_ARM_POSE = new EnumProxy<>(
            HumanoidModel.ArmPose.class, false, (IArmPoseTransformer) (model, entity, arm) -> {
                var heldArm = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
                heldArm.xRot = Mth.lerp(0.85F, heldArm.xRot, -(float) Math.PI / 3.5F + model.head.xRot / 2F);
            });

    private StaffArmPose() {
    }
}
