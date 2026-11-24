package jp.aquafactory.constellationandspells.datagen;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.SoundRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public final class SoundDataProvider extends SoundDefinitionsProvider {
    public SoundDataProvider(PackOutput output, ExistingFileHelper helper) { super(output, ConstellationnSpells.MODID, helper); }

    @Override
    public void registerSounds() {
        for (var sound : SoundRegistry.SOUNDS.getEntries()) {
            if (sound == SoundRegistry.ARMOR_EQUIP_CLOTH) {
                // 音源だけを革装備と共有し、字幕は布防具用の独立したイベントに持たせる。
                var cloth = definition().subtitle("subtitle." + ConstellationnSpells.MODID + ".sound.armor_equip_cloth");
                for (int variant = 1; variant <= 6; variant++) {
                    cloth.with(sound(ResourceLocation.withDefaultNamespace("item/armor/equip_leather" + variant)));
                }
                add(sound.getId(), cloth);
                continue;
            }
            var soundDefinition = definition().with(sound(sound.getId()));
            // 角笛と衝撃波は同時に鳴るので、発動を知らせる字幕は角笛にまとめる。
            if (sound != SoundRegistry.CAPRICORNUS_SHOCK) {
                soundDefinition.subtitle("subtitle." + ConstellationnSpells.MODID + ".sound." + sound.getId().getPath());
            }
            add(sound.getId(), soundDefinition);
        }
    }
}
