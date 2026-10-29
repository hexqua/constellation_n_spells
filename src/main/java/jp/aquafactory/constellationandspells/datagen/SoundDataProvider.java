package jp.aquafactory.constellationandspells.datagen;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.SoundRegistry;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public final class SoundDataProvider extends SoundDefinitionsProvider {
    public SoundDataProvider(PackOutput output, ExistingFileHelper helper) { super(output, ConstellationnSpells.MODID, helper); }

    @Override
    public void registerSounds() {
        for (var sound : SoundRegistry.SOUNDS.getEntries()) {
            add(sound.getId(), definition().subtitle("subtitle." + ConstellationnSpells.MODID + ".sound." + sound.getId().getPath())
                    .with(sound(sound.getId())));
        }
    }
}
