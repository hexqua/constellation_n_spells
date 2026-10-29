package jp.aquafactory.constellationandspells.registry;

import com.mojang.serialization.MapCodec;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.particle.AdditiveGlowParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public final class ParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, ConstellationnSpells.MODID);
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdditiveGlowParticleOptions>> ADDITIVE_SPARK = register("additive_spark");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdditiveGlowParticleOptions>> ADDITIVE_RHOMBUS = register("additive_rhombus");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdditiveGlowParticleOptions>> FALLING_SPARK = register("falling_spark");

    private ParticleRegistry() {}

    private static DeferredHolder<ParticleType<?>, ParticleType<AdditiveGlowParticleOptions>> register(String name) {
        return PARTICLES.register(name, GlowType::new);
    }

    private static final class GlowType extends ParticleType<AdditiveGlowParticleOptions> {
        private GlowType() { super(false); }

        @Override
        public @NotNull MapCodec<AdditiveGlowParticleOptions> codec() { return AdditiveGlowParticleOptions.mapCodec(this); }

        @Override
        public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, AdditiveGlowParticleOptions> streamCodec() {
            return AdditiveGlowParticleOptions.streamCodec(this);
        }
    }
}
