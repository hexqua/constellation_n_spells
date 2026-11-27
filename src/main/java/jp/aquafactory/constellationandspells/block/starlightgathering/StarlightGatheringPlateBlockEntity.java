package jp.aquafactory.constellationandspells.block.starlightgathering;

import jp.aquafactory.constellationandspells.particle.ZodiacParticleOptions;
import jp.aquafactory.constellationandspells.recipe.StarlightGatheringRecipe;
import jp.aquafactory.constellationandspells.registry.BlockEntityRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import jp.aquafactory.constellationandspells.registry.ParticleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class StarlightGatheringPlateBlockEntity extends BlockEntity {
    public static final int PLATE_CAPACITY = 8;
    private static final int FUEL_SLOT = PLATE_CAPACITY;
    private static final int DAY_LENGTH = 24_000;
    private static final int MIDNIGHT = 18_000;
    private static final int REPEAT_WINDOW = 20;
    private static final long UNSCHEDULED = Long.MIN_VALUE;

    // 固定の搬送スロットを維持しつつ、手で取り出すために投入順を保持する。
    private final LinkedHashMap<Integer, PlateEntry> contents = new LinkedHashMap<>();
    private final Map<ClickKey, RecentInsertion> recentInsertions = new HashMap<>();
    private final IItemHandler itemHandler = new PlateItemHandler();
    private ItemStack fuel = ItemStack.EMPTY;

    public StarlightGatheringPlateBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.STARLIGHT_GATHERING_PLATE.get(), pos, state);
    }

    public IItemHandler itemHandler() {
        return itemHandler;
    }

    public ItemStack displayedItem(int slot) {
        var entry = contents.get(slot);
        return entry == null ? ItemStack.EMPTY : entry.stack();
    }

    public ItemStack displayedFuel() {
        return fuel;
    }

    int insertFromHand(ServerLevel server, Player player, InteractionHand hand, ItemStack stack) {
        if (contents.size() == PLATE_CAPACITY) {
            return 0;
        }
        long now = server.getGameTime();
        var key = new ClickKey(player.getUUID(), hand);
        var previous = recentInsertions.remove(key);
        boolean repeat = previous != null && stack.is(previous.item()) && now - previous.time() <= REPEAT_WINDOW;
        int count = repeat ? Math.min(stack.getCount(), PLATE_CAPACITY - contents.size()) : 1;
        if (!repeat) {
            recentInsertions.put(key, new RecentInsertion(stack.getItem(), now));
        }
        insertMaterials(server, stack, count);
        return count;
    }

    int insertFromGround(ServerLevel server, ItemStack stack) {
        if (itemHandler.isItemValid(FUEL_SLOT, stack)) {
            return insertFuel(server, stack) ? 1 : 0;
        }
        if (contents.size() == PLATE_CAPACITY || !itemHandler.isItemValid(0, stack)) {
            return 0;
        }
        int count = Math.min(stack.getCount(), PLATE_CAPACITY - contents.size());
        insertMaterials(server, stack, count);
        return count;
    }

    private void insertMaterials(ServerLevel server, ItemStack stack, int count) {
        int remaining = count;
        for (int slot = 0; slot < PLATE_CAPACITY && remaining > 0; slot++) {
            if (!contents.containsKey(slot)) {
                insertMaterial(server, slot, stack);
                remaining--;
            }
        }
        inserted(server);
    }

    boolean insertFuel(ServerLevel server, ItemStack stack) {
        if (!fuel.isEmpty()) {
            return false;
        }
        long scheduled = nextMidnight(server.getDayTime());
        fuel = stack.copyWithCount(1);
        contents.replaceAll((slot, entry) -> entry.completed() ? entry : entry.withSchedule(scheduled));
        inserted(server);
        return true;
    }

    private void insertMaterial(ServerLevel server, int slot, ItemStack stack) {
        long scheduled = fuel.isEmpty() ? UNSCHEDULED : nextMidnight(server.getDayTime());
        contents.put(slot, new PlateEntry(stack.copyWithCount(1), false, scheduled));
    }

    private void inserted(ServerLevel server) {
        syncContents(server);
        float pitch = 1.1F + (server.random.nextFloat() * 2F - 1F) * 0.1F;
        server.playSound(null, worldPosition, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.65F, pitch);
    }

    boolean collect(ServerLevel server, Player player) {
        if (!contents.isEmpty()) {
            Item newest = contents.lastEntry().getValue().stack().getItem();
            var entries = contents.values().iterator();
            while (entries.hasNext()) {
                var entry = entries.next();
                if (entry.stack().is(newest)) {
                    entries.remove();
                    player.getInventory().placeItemBackInInventory(entry.stack());
                }
            }
        } else if (!fuel.isEmpty()) {
            var stack = fuel;
            fuel = ItemStack.EMPTY;
            player.getInventory().placeItemBackInInventory(stack);
        } else {
            return false;
        }
        syncContents(server);
        return true;
    }

    void dropContents(Level level) {
        for (var entry : contents.values()) {
            dropStack(level, entry.stack());
        }
        contents.clear();
        dropStack(level, fuel);
        fuel = ItemStack.EMPTY;
    }

    private void dropStack(Level level, ItemStack stack) {
        if (!stack.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
        }
    }

    static void tick(Level level, BlockPos pos, BlockState state, StarlightGatheringPlateBlockEntity plate) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        long gameTime = server.getGameTime();
        plate.recentInsertions.values().removeIf(previous -> gameTime - previous.time() > REPEAT_WINDOW);
        if (plate.fuel.isEmpty()) {
            return;
        }

        long now = server.getDayTime();
        boolean hasStarlight = StarlightGatheringPlateBlock.hasStarlight(server, pos);
        plate.updateSchedules(server, now, hasStarlight);
        if (!hasStarlight) {
            return;
        }

        int dayTick = Math.floorMod(now, DAY_LENGTH);
        if (plate.processDueMaterials(server, now)) {
            if (dayTick == MIDNIGHT) {
                displayParticles(server, pos, 24, 0.16F, 0.07);
            }
        } else if (dayTick >= 13_000 && dayTick <= 23_000 && server.random.nextInt(22) == 0) {
            displayParticles(server, pos, 2, 0.10F, 0.01);
        }
    }

    private static long nextMidnight(long now) {
        int wait = Math.floorMod(MIDNIGHT - Math.floorMod(now, DAY_LENGTH) - 1, DAY_LENGTH) + 1;
        return now + wait;
    }

    private void updateSchedules(ServerLevel server, long now, boolean hasStarlight) {
        long upcoming = nextMidnight(now);
        boolean changed = false;
        for (var slot : contents.entrySet()) {
            var entry = slot.getValue();
            // 時刻の巻き戻しで予定が次の夜半より先に残った場合は、新しい時刻に合わせる。
            // 予定を過ぎてから最初に確認できた時点が暗い場合も、次の夜半に回す。
            if (entry.scheduled() > upcoming || (!hasStarlight && entry.isDue(now))) {
                slot.setValue(entry.withSchedule(upcoming));
                changed = true;
            }
        }
        if (changed) {
            setChanged(server, worldPosition, getBlockState());
        }
    }

    private boolean processDueMaterials(ServerLevel server, long now) {
        boolean processed = false;
        for (var slot : contents.entrySet()) {
            var entry = slot.getValue();
            if (!entry.isDue(now)) {
                continue;
            }
            var recipe = StarlightGatheringRecipe.find(server, entry.stack());
            if (recipe.isPresent()) {
                var input = new SingleRecipeInput(entry.stack());
                var result = recipe.get().value().assemble(input, server.registryAccess());
                if (!result.isEmpty()) {
                    slot.setValue(new PlateEntry(result, true, UNSCHEDULED));
                    processed = true;
                }
            }
        }
        if (processed) {
            fuel = ItemStack.EMPTY;
            contents.replaceAll((slot, entry) -> entry.withSchedule(UNSCHEDULED));
            syncContents(server);
        }
        return processed;
    }

    private static void displayParticles(ServerLevel level, BlockPos pos, int count, float size, double speed) {
        var options = ZodiacParticleOptions.glow(ParticleRegistry.ADDITIVE_GLOWING_SPARK.get(), level.random.nextBoolean(), size);
        level.sendParticles(options, pos.getX() + 0.5, pos.getY() + 0.73, pos.getZ() + 0.5,
                count, 0.21, 0.07, 0.21, speed);
    }

    private void syncContents(ServerLevel server) {
        setChanged(server, worldPosition, getBlockState());
        var state = getBlockState();
        server.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        writeContents(tag, registries, true);
    }

    private void writeContents(CompoundTag tag, HolderLookup.Provider registries, boolean includeProcessing) {
        var items = new ListTag();
        contents.forEach((slot, entry) -> {
            var item = new CompoundTag();
            item.putInt("slot", slot);
            item.put("stack", entry.stack().save(registries));
            if (includeProcessing) {
                item.putBoolean("completed", entry.completed());
                if (entry.scheduled() != UNSCHEDULED) {
                    item.putLong("scheduled", entry.scheduled());
                }
            }
            items.add(item);
        });
        tag.put("items", items);
        if (!fuel.isEmpty()) {
            tag.put("fuel", fuel.save(registries));
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        contents.clear();
        var items = tag.getList("items", Tag.TAG_COMPOUND);
        for (int index = 0; index < items.size(); index++) {
            var item = items.getCompound(index);
            int slot = item.getInt("slot");
            var stack = ItemStack.parseOptional(registries, item.getCompound("stack"));
            if (slot >= 0 && slot < PLATE_CAPACITY && !stack.isEmpty()) {
                long scheduled = item.contains("scheduled", Tag.TAG_LONG) ? item.getLong("scheduled") : UNSCHEDULED;
                contents.put(slot, new PlateEntry(stack, item.getBoolean("completed"), scheduled));
            }
        }
        fuel = ItemStack.parseOptional(registries, tag.getCompound("fuel"));
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        var tag = new CompoundTag();
        writeContents(tag, registries, false);
        return tag;
    }

    @Override
    public @NotNull ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private record ClickKey(UUID player, InteractionHand hand) {}

    private record RecentInsertion(Item item, long time) {}

    private record PlateEntry(ItemStack stack, boolean completed, long scheduled) {
        boolean isDue(long now) {
            return !completed && scheduled != UNSCHEDULED && scheduled <= now;
        }

        PlateEntry withSchedule(long updated) {
            return new PlateEntry(stack, completed, updated);
        }
    }

    private final class PlateItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return PLATE_CAPACITY + 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot == FUEL_SLOT ? fuel : displayedItem(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!(getLevel() instanceof ServerLevel server) || isRemoved()
                    || !getStackInSlot(slot).isEmpty() || !isItemValid(slot, stack)) {
                return stack;
            }
            if (!simulate) {
                if (slot == FUEL_SLOT) {
                    insertFuel(server, stack);
                } else {
                    insertMaterial(server, slot, stack);
                    inserted(server);
                }
            }
            return stack.copyWithCount(stack.getCount() - 1);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!(getLevel() instanceof ServerLevel server) || isRemoved() || amount <= 0) {
                return ItemStack.EMPTY;
            }
            var entry = contents.get(slot);
            if (entry == null || !entry.completed()) {
                return ItemStack.EMPTY;
            }
            var result = entry.stack().copy();
            if (!simulate) {
                contents.remove(slot);
                syncContents(server);
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (stack.isEmpty() || slot < 0 || slot > FUEL_SLOT) {
                return false;
            }
            if (slot == FUEL_SLOT) {
                return stack.is(ItemRegistry.STARDUST_SHARD.get());
            }
            return level != null && !stack.is(ItemRegistry.STARDUST_SHARD.get())
                    && StarlightGatheringRecipe.find(level, stack).isPresent();
        }
    }
}
