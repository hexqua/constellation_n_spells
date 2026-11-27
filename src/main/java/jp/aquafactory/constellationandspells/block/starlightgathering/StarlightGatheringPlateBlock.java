package jp.aquafactory.constellationandspells.block.starlightgathering;

import com.mojang.serialization.MapCodec;
import jp.aquafactory.constellationandspells.recipe.StarlightGatheringRecipe;
import jp.aquafactory.constellationandspells.registry.BlockEntityRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StarlightGatheringPlateBlock extends BaseEntityBlock {
    private static final MapCodec<StarlightGatheringPlateBlock> CODEC = simpleCodec(StarlightGatheringPlateBlock::new);
    private static final String MESSAGE_KEY = "block.constellationandspells.starlight_gathering_plate.";
    private static final VoxelShape SHAPE = box(1, 0, 1, 15, 11, 15);

    public StarlightGatheringPlateBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<StarlightGatheringPlateBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                         @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new StarlightGatheringPlateBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                           @NotNull BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, BlockEntityRegistry.STARLIGHT_GATHERING_PLATE.get(),
                StarlightGatheringPlateBlockEntity::tick);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level,
                                                       @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand,
                                                       @NotNull BlockHitResult hit) {
        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        boolean fuel = stack.is(ItemRegistry.STARDUST_SHARD.get());
        if (!fuel && StarlightGatheringRecipe.find(level, stack).isEmpty()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable(MESSAGE_KEY + "invalid_recipe", stack.getHoverName())
                        .withStyle(ChatFormatting.YELLOW), true);
            }
            return ItemInteractionResult.CONSUME_PARTIAL;
        }
        if (level instanceof ServerLevel server && server.getBlockEntity(pos) instanceof StarlightGatheringPlateBlockEntity plate) {
            if (fuel) {
                if (!hasStarlight(server, pos) || !plate.insertFuel(server, stack)) {
                    showStarlight(player, server, pos);
                } else {
                    stack.consume(1, player);
                }
            } else {
                int inserted = plate.insertFromHand(server, player, hand, stack);
                if (inserted == 0) {
                    player.displayClientMessage(Component.translatable(MESSAGE_KEY + "full").withStyle(ChatFormatting.YELLOW), true);
                } else {
                    stack.consume(inserted, player);
                }
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                       @NotNull Player player, @NotNull BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server && server.getBlockEntity(pos) instanceof StarlightGatheringPlateBlockEntity plate
                && !plate.collect(server, player)) {
            showStarlight(player, server, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void stepOn(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Entity entity) {
        if (level instanceof ServerLevel server && entity instanceof ItemEntity item && item.isAlive()
                && server.getBlockEntity(pos) instanceof StarlightGatheringPlateBlockEntity plate) {
            var stack = item.getItem();
            int inserted = plate.insertFromGround(server, stack);
            if (inserted > 0) {
                var remaining = stack.copyWithCount(stack.getCount() - inserted);
                item.setItem(remaining);
                if (remaining.isEmpty()) {
                    item.discard();
                }
            }
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                            @NotNull BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof StarlightGatheringPlateBlockEntity plate) {
            plate.dropContents(level);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    static boolean hasStarlight(Level level, BlockPos pos) {
        // 空の暗さ補正は夜半に最大11となるため、空の光が12あれば1以上残る。
        return level.getBrightness(LightLayer.SKY, pos.above()) >= 12;
    }

    private static void showStarlight(Player player, Level level, BlockPos pos) {
        boolean enough = hasStarlight(level, pos);
        player.displayClientMessage(Component.translatable(MESSAGE_KEY + (enough ? "enough_starlight" : "insufficient_starlight"))
                .withStyle(enough ? ChatFormatting.GREEN : ChatFormatting.YELLOW), true);
    }
}
