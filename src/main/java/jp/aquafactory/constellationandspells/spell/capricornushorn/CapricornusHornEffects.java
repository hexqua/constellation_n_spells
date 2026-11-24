package jp.aquafactory.constellationandspells.spell.capricornushorn;

import io.redspace.ironsspellbooks.particle.FallingBlockParticleOption;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

final class CapricornusHornEffects {
    private static final int TREMOR_HALF_EXTENT = 3;

    private CapricornusHornEffects() {}

    static void onSummon(ServerLevel level, Vec3 origin) {
        var horns = SoundEvents.GOAT_HORN_SOUND_VARIANTS;
        var hornSound = horns.get(level.random.nextInt(horns.size())).value();
        level.playSound(null, origin.x, origin.y, origin.z, hornSound, SoundSource.PLAYERS, 1.4f, 1.0f);
        level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.GOAT_SCREAMING_AMBIENT, SoundSource.PLAYERS, 0.7f, 1.05f);
        level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.9f, 0.88f);

        var center = BlockPos.containing(origin);
        var min = center.offset(-TREMOR_HALF_EXTENT, -TREMOR_HALF_EXTENT, -TREMOR_HALF_EXTENT);
        var max = center.offset(TREMOR_HALF_EXTENT, TREMOR_HALF_EXTENT, TREMOR_HALF_EXTENT);
        var bounds = AABB.encapsulatingFullBlocks(min, max);
        for (var player : level.players()) {
            if (bounds.contains(player.position())) {
                PacketDistributor.sendToPlayer(player, CapricornusHornShakePayload.INSTANCE);
            }
        }

        for (var pos : BlockPos.betweenClosed(min, max)) {
            if (level.isOutsideBuildHeight(pos) || !level.hasChunkAt(pos)) continue;
            var state = level.getBlockState(pos);

            // 地面の中のものが揺れないように上の空気は必須で探す.
            if (state.getRenderShape() != RenderShape.MODEL || !level.getBlockState(pos.above()).isAir()) continue;

            // 地震と同じ粒子を使い、通常の近距離配信で各ブロックを一度だけ揺らす.
            var impulse = new Vec3(0, 0.1 + level.random.nextDouble() * 0.2, 0);
            var particle = new FallingBlockParticleOption(state, impulse);
            level.sendParticles(particle, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 1, 0, 0, 0, 0);
        }
    }
}
