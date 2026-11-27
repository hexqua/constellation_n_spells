package jp.aquafactory.constellationandspells.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.HashCommon;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.block.starlightgathering.StarlightGatheringPlateBlockEntity;
import jp.aquafactory.constellationandspells.registry.BlockEntityRegistry;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = ConstellationnSpells.MODID, value = Dist.CLIENT)
public final class StarlightGatheringPlateRenderer implements BlockEntityRenderer<StarlightGatheringPlateBlockEntity> {
    private static final float ITEM_SCALE = 0.245F;
    private static final ItemPlacement[][] ITEM_LAYOUTS = {
        {
            new ItemPlacement(-3.00, 2.55, 0F, 0),
            new ItemPlacement(-3.00, 1.15, 90F, 1),
            new ItemPlacement(3.00, -0.10, 180F, 0),
            new ItemPlacement(0.45, -3.00, 270F, 1),
            new ItemPlacement(3.00, -2.80, 90F, 2),
            new ItemPlacement(-3.00, -0.85, 0F, 2),
            new ItemPlacement(2.75, 3.00, 270F, 1),
            new ItemPlacement(-2.85, -3.00, 180F, 0),
        },
        {
            new ItemPlacement(3.00, 2.22, 90F, 0),
            new ItemPlacement(0.18, 3.00, 180F, 1),
            new ItemPlacement(-3.00, -2.90, 90F, 0),
            new ItemPlacement(-3.00, -1.60, 270F, 1),
            new ItemPlacement(-3.00, 2.28, 180F, 0),
            new ItemPlacement(3.00, -0.75, 0F, 2),
            new ItemPlacement(3.00, -2.65, 180F, 0),
            new ItemPlacement(-1.35, 3.00, 90F, 2),
        },
        {
            new ItemPlacement(-3.00, -0.35, 270F, 0),
            new ItemPlacement(-2.90, -3.00, 90F, 1),
            new ItemPlacement(3.00, 2.93, 180F, 0),
            new ItemPlacement(1.65, 3.00, 0F, 2),
            new ItemPlacement(3.00, -0.48, 270F, 1),
            new ItemPlacement(-3.00, 2.30, 90F, 1),
            new ItemPlacement(2.90, -3.00, 180F, 0),
            new ItemPlacement(-0.45, -3.00, 0F, 2),
        },
    };

    private final ItemRenderer items;

    public StarlightGatheringPlateRenderer(BlockEntityRendererProvider.Context context) {
        items = context.getItemRenderer();
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityRegistry.STARLIGHT_GATHERING_PLATE.get(), StarlightGatheringPlateRenderer::new);
    }

    @Override
    public void render(@NotNull StarlightGatheringPlateBlockEntity plate, float partialTick, @NotNull PoseStack poses,
                       @NotNull MultiBufferSource buffers, int packedLight, int overlay) {
        var level = plate.getLevel();
        if (level == null) {
            return;
        }
        var pos = plate.getBlockPos();
        int light = LevelRenderer.getLightColor(level, pos.above());
        int seed = pos.hashCode();
        long layoutSeed = HashCommon.mix(pos.asLong());
        var layout = ITEM_LAYOUTS[Math.floorMod(layoutSeed, ITEM_LAYOUTS.length)];
        for (int slot = 0; slot < StarlightGatheringPlateBlockEntity.PLATE_CAPACITY; slot++) {
            var stack = plate.displayedItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            poses.pushPose();
            layout[slot].apply(poses);
            items.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, poses, buffers, level, seed + slot);
            poses.popPose();
        }

        var fuel = plate.displayedFuel();
        if (!fuel.isEmpty()) {
            poses.pushPose();
            poses.translate(0.5, 0.91, 0.5);
            poses.mulPose(Axis.YP.rotationDegrees((level.getGameTime() % 120L + partialTick) * 3F));
            poses.scale(0.30F, 0.30F, 0.30F);
            items.renderStatic(fuel, ItemDisplayContext.FIXED, light, overlay, poses, buffers, level, seed);
            poses.popPose();
        }
    }

    private record ItemPlacement(double xFromCenter, double zFromCenter, float yaw, int layer) {
        void apply(PoseStack poses) {
            // 重なる品はモデルの厚みより広い間隔で、高さだけずらす。
            double height = 10.19 + 0.30 * layer;
            poses.translate(0.5 + xFromCenter / 16.0, height / 16.0, 0.5 + zFromCenter / 16.0);
            poses.mulPose(Axis.YP.rotationDegrees(yaw));
            poses.mulPose(Axis.XP.rotationDegrees(90F));
            poses.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        }
    }
}
