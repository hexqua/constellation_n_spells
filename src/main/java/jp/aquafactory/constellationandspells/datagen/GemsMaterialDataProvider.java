package jp.aquafactory.constellationandspells.datagen;

import com.mojang.serialization.JsonOps;
import io.redspace.ironsjewelry.core.actions.ApplyEffectAction;
import io.redspace.ironsjewelry.core.data.AttributeInstance;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.QualityScalar;
import io.redspace.ironsjewelry.core.parameters.ActionParameter;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.AttributeRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.COOLDOWN_REDUCTION;

public final class GemsMaterialDataProvider implements DataProvider {
    private final PackOutput.PathProvider materials;
    private final PackOutput.PathProvider tags;
    private final CompletableFuture<HolderLookup.Provider> lookup;

    public GemsMaterialDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        materials = output.createPathProvider(PackOutput.Target.DATA_PACK, "irons_jewelry/material");
        tags = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/irons_jewelry/material");
        this.lookup = lookup;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        return lookup.thenCompose(registries -> {
            var id = ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "zodiac_rune");
            // 既存 school ルーンと同じ基礎量にし、指輪の品質による倍率は Jewelry 側へ任せる。
            var material = new MaterialDefinition(
                    "material.constellationandspells.zodiac_rune",
                    Ingredient.of(ItemRegistry.ZODIAC_RUNE.get()),
                    ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "palettes/zodiac_rune"),
                    Map.of(ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(
                            AttributeRegistry.ZODIAC_SPELL_POWER, 0.025, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)),
                    1.5);
            var ops = RegistryOps.create(JsonOps.INSTANCE, registries);
            var materialJson = MaterialDefinition.CODEC.encodeStart(ops, material).getOrThrow();
            var runeTag = new TagFile(List.of(TagEntry.element(id)), false, List.of());
            var tagJson = TagFile.CODEC.encodeStart(JsonOps.INSTANCE, runeTag).getOrThrow();
            var stellarDropId = ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "stellar_drop");
            // 短い再付与で移動速度由来のFOVが頻繁に変わらないよう、最低2秒を保証する。
            var hastenedDuration = new QualityScalar(40, 40, 40, Optional.of(120d));
            var stellarDrop = new MaterialDefinition(
                    "material.constellationandspells.stellar_drop",
                    Ingredient.of(ItemRegistry.STARDUST_SHARD.get()),
                    ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "palettes/stellar_drop"),
                    Map.of(
                            ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(
                                    COOLDOWN_REDUCTION, 0.025, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                            ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                    new ApplyEffectAction(hastenedDuration, new QualityScalar(1), MobEffectRegistry.HASTENED), true),
                            ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffectRegistry.SLOWED,
                            ParameterTypeRegistry.POSITIVE_EFFECT_PARAMETER.get(), MobEffectRegistry.HASTENED),
                    0.5);
            var stellarDropJson = MaterialDefinition.CODEC.encodeStart(ops, stellarDrop).getOrThrow();
            var gemTag = new TagFile(List.of(TagEntry.element(stellarDropId)), false, List.of());
            var gemTagJson = TagFile.CODEC.encodeStart(JsonOps.INSTANCE, gemTag).getOrThrow();
            return CompletableFuture.allOf(
                    DataProvider.saveStable(output, materialJson, materials.json(id)),
                    DataProvider.saveStable(output, tagJson, tags.json(
                            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "rune"))),
                    DataProvider.saveStable(output, stellarDropJson, materials.json(stellarDropId)),
                    DataProvider.saveStable(output, gemTagJson, tags.json(
                            ResourceLocation.fromNamespaceAndPath("irons_jewelry", "gem"))));
        });
    }

    @Override
    public @NotNull String getName() {
        return "Constellation 'n Spells Iron's Gems Materials";
    }
}
