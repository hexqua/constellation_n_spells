package jp.aquafactory.constellationandspells.client;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import jp.aquafactory.constellationandspells.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = ConstellationnSpells.MODID, value = Dist.CLIENT)
public final class StarcondenseSoundClientRegistration {
    private StarcondenseSoundClientRegistration() {}

    @SubscribeEvent
    public static void register(RegisterClientExtensionsEvent event) {
        event.registerBlock(new IClientBlockExtensions() {
            @Override
            public boolean playBreakSound(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
                float pitch = 0.85F + level.random.nextFloat() * 0.30F;
                level.playLocalSound(pos, SoundRegistry.STARCONDENSE_BREAK.get(), SoundSource.BLOCKS, 0.7F, pitch, false);
                // false で後続のバニラ破壊音を残し、そのランダムな音源に追加音を重ねる。
                // 破壊通知を受けた各クライアントで再生するため、サーバーからの追加送信は行わない。
                return false;
            }
        }, BlockRegistry.STELLAR_DROP_ORE.get(), BlockRegistry.DEEPSLATE_STELLAR_DROP_ORE.get(),
                BlockRegistry.SHIMMER_STARCONDENSE_BLOCK.get(), BlockRegistry.SMOOTH_SHIMMER_STARCONDENSE_BLOCK.get(),
                BlockRegistry.STARCONDENSE_GLASS.get(), BlockRegistry.STARCONDENSE_GLASS_PANE.get(),
                BlockRegistry.SHIMMER_STARCONDENSE_GLASS.get(), BlockRegistry.SHIMMER_STARCONDENSE_GLASS_PANE.get());
    }
}
