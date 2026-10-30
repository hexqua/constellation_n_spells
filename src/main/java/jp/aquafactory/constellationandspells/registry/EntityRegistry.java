package jp.aquafactory.constellationandspells.registry;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.spell.stardust.StardustProjectile;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class EntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, ConstellationnSpells.MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<StardustProjectile>> STARDUST = ENTITIES.register("stardust",
            () -> EntityType.Builder.of(StardustProjectile::new, MobCategory.MISC)
                    .sized(0.15f, 0.15f).clientTrackingRange(8).updateInterval(1)
                    .build(ConstellationnSpells.MODID + ":stardust"));

    private EntityRegistry() {}
}
