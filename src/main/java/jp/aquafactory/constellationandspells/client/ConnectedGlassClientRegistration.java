package jp.aquafactory.constellationandspells.client;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.client.model.ConnectedGlassModel;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.IdentityHashMap;

@EventBusSubscriber(modid = ConstellationnSpells.MODID, value = Dist.CLIENT)
public final class ConnectedGlassClientRegistration {
    private ConnectedGlassClientRegistration() {}

    @SubscribeEvent
    public static void models(ModelEvent.ModifyBakingResult event) {
        wrap(event, BlockRegistry.STARCONDENSE_GLASS.get(), "starcondense_glass", false);
        wrap(event, BlockRegistry.SHIMMER_STARCONDENSE_GLASS.get(), "shimmer_starcondense_glass", false);
        wrap(event, BlockRegistry.STARCONDENSE_GLASS_PANE.get(), "starcondense_glass", true);
        wrap(event, BlockRegistry.SHIMMER_STARCONDENSE_GLASS_PANE.get(), "shimmer_starcondense_glass", true);
        wrap(event, BlockRegistry.MIDNIGHT_STARCONDENSE_GLASS.get(), "midnight_starcondense_glass", false);
    }

    private static void wrap(ModelEvent.ModifyBakingResult event, Block block, String texture, boolean pane) {
        var original = event.getTextureGetter().apply(material(texture));
        var connected = event.getTextureGetter().apply(material(texture + "_connected"));
        // 同じ multipart モデルを共有する各 state に対し、変換済み quad のキャッシュも共有する。
        var wrappers = new IdentityHashMap<BakedModel, ConnectedGlassModel>();
        for (var state : block.getStateDefinition().getPossibleStates()) {
            event.getModels().computeIfPresent(BlockModelShaper.stateToModelLocation(state),
                    (location, model) -> wrappers.computeIfAbsent(model,
                            key -> new ConnectedGlassModel(key, original, connected, pane)));
        }
    }

    private static Material material(String texture) {
        return new Material(InventoryMenu.BLOCK_ATLAS,
                ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "block/" + texture));
    }
}
