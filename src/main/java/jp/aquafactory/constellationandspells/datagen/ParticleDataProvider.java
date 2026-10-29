package jp.aquafactory.constellationandspells.datagen;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.ParticleRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.ParticleDescriptionProvider;

public final class ParticleDataProvider extends ParticleDescriptionProvider {
    public ParticleDataProvider(PackOutput output, ExistingFileHelper helper) { super(output, helper); }

    @Override
    protected void addDescriptions() {
        sprite(ParticleRegistry.ADDITIVE_SPARK.get(), texture("glow_square"));
        sprite(ParticleRegistry.ADDITIVE_RHOMBUS.get(), texture("glow_rhombus"));
        sprite(ParticleRegistry.FALLING_SPARK.get(), texture("glow_square"));
    }

    private static ResourceLocation texture(String name) { return ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, name); }
}
