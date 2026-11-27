package jp.aquafactory.constellationandspells.registry;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.block.starlightgathering.StarlightGatheringPlateBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ConstellationnSpells.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StarlightGatheringPlateBlockEntity>> STARLIGHT_GATHERING_PLATE =
            BLOCK_ENTITIES.register("starlight_gathering_plate", () -> BlockEntityType.Builder.of(
                    StarlightGatheringPlateBlockEntity::new, BlockRegistry.STARLIGHT_GATHERING_PLATE.get()).build(null));

    private BlockEntityRegistry() {}

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, STARLIGHT_GATHERING_PLATE.get(),
                (plate, side) -> plate.itemHandler());
    }
}
