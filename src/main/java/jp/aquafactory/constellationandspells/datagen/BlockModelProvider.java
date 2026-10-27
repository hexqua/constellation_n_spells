package jp.aquafactory.constellationandspells.datagen;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class BlockModelProvider extends BlockStateProvider {
    public BlockModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, ConstellationnSpells.MODID, existingFiles);
    }

    @Override
    protected void registerStatesAndModels() {
        for (var holder : BlockRegistry.BLOCKS.getEntries()) {
            var block = holder.get();
            simpleBlockWithItem(block, cubeAll(block));
        }
    }
}
