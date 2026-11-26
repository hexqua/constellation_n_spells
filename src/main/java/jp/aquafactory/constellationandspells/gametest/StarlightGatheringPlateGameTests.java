package jp.aquafactory.constellationandspells.gametest;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.BlockEntityRegistry;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.Objects;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
public final class StarlightGatheringPlateGameTests {
    private static final BlockPos PLATE = new BlockPos(2, 2, 2);

    private StarlightGatheringPlateGameTests() {}

    @GameTest(template = "gametest/basic_floor")
    public static void plateCatchesUpAfterRewindingAndSkippingNight(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(PLATE);
        helper.setBlock(PLATE, BlockRegistry.STARLIGHT_GATHERING_PLATE.get());
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(skyLight(level, pos) >= 12, "Plate must have an open sky"))
                .thenExecute(() -> {
                    long previousTime = level.getDayTime();
                    long midnight = futureMidnight(level);
                    var handler = handler(level, pos);
                    int fuelSlot = handler.getSlots() - 1;
                    try {
                        level.setDayTime(midnight + 1_000);
                        handler.insertItem(0, new ItemStack(Items.ROTTEN_FLESH), false);
                        handler.insertItem(fuelSlot, new ItemStack(ItemRegistry.STARDUST_SHARD.get()), false);

                        level.setDayTime(midnight - 1_000);
                        tickPlate(level, pos);
                        helper.assertTrue(handler.getStackInSlot(0).is(Items.ROTTEN_FLESH)
                                        && !handler.getStackInSlot(fuelSlot).isEmpty(),
                                "Rewinding before midnight must not process or consume anything yet");

                        level.setDayTime(EventHooks.onSleepFinished(level, midnight + 6_000, level.getDayTime()));
                        tickPlate(level, pos);
                        helper.assertTrue(handler.getStackInSlot(0).is(Items.LEATHER)
                                        && handler.getStackInSlot(fuelSlot).isEmpty(),
                                "Skipping to dawn after rewinding must complete the batch");
                    } finally {
                        level.setDayTime(previousTime);
                    }
                })
                .thenSucceed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void plateSkipsDarkMidnightAndCatchesUpAfterTheNextMidnight(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(PLATE);
        long midnight = futureMidnight(level);
        var savedPlate = new CompoundTag();
        helper.setBlock(PLATE, BlockRegistry.STARLIGHT_GATHERING_PLATE.get());
        helper.setBlock(PLATE.above(), Blocks.STONE);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemRegistry.STARDUST_SHARD.get()));
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(skyLight(level, pos) < 12, "Plate must be shaded by its cover"))
                .thenExecute(() -> {
                    long previousTime = level.getDayTime();
                    try {
                        level.setDayTime(midnight - 1);
                        var handler = handler(level, pos);
                        int fuelSlot = handler.getSlots() - 1;
                        useHeldItem(level, pos, player);
                        helper.assertTrue(handler.getStackInSlot(fuelSlot).isEmpty() && player.getMainHandItem().getCount() == 1,
                                "Shaded plate must reject hand-fed fuel");
                        helper.assertTrue(handler.insertItem(0, new ItemStack(Items.ROTTEN_FLESH), false).isEmpty(),
                                "Automation must accept recipe input while shaded");
                        helper.assertTrue(handler.insertItem(fuelSlot, player.getMainHandItem(), false).isEmpty(),
                                "Automation must accept fuel while shaded");

                        level.setDayTime(midnight);
                        tickPlate(level, pos);
                        helper.assertTrue(handler.getStackInSlot(0).is(Items.ROTTEN_FLESH) && !handler.getStackInSlot(fuelSlot).isEmpty(),
                                "Darkness at midnight must preserve the material and fuel");

                        var original = Objects.requireNonNull(level.getBlockEntity(pos));
                        savedPlate.merge(original.saveWithFullMetadata(level.registryAccess()));
                        level.removeBlockEntity(pos);
                    } finally {
                        level.setDayTime(previousTime);
                    }
                    helper.setBlock(PLATE.above(), Blocks.AIR);
                })
                .thenWaitUntil(() -> helper.assertTrue(skyLight(level, pos) >= 12, "Plate must receive starlight after uncovering"))
                .thenExecute(() -> {
                    long previousTime = level.getDayTime();
                    try {
                        level.setDayTime(midnight + 6_001);
                        var state = level.getBlockState(pos);
                        level.setBlockEntity(Objects.requireNonNull(BlockEntity.loadStatic(pos, state, savedPlate, level.registryAccess())));
                        var handler = handler(level, pos);
                        tickPlate(level, pos);
                        helper.assertTrue(handler.getStackInSlot(0).is(Items.ROTTEN_FLESH)
                                        && !handler.getStackInSlot(handler.getSlots() - 1).isEmpty(),
                                "A restored plate must not process that night's batch after uncovering");

                        level.setDayTime(midnight + 24_000 + 6_000);
                        handler.insertItem(1, new ItemStack(Items.ROTTEN_FLESH), false);
                        tickPlate(level, pos);
                        tickPlate(level, pos);
                        helper.assertTrue(handler.getStackInSlot(0).is(Items.LEATHER) && handler.getStackInSlot(0).getCount() == 1,
                                "Restored plate must catch up from its generated recipe exactly once");
                        helper.assertTrue(handler.getStackInSlot(1).is(Items.ROTTEN_FLESH), "Later input must wait for the next batch");
                        helper.assertTrue(handler.getStackInSlot(handler.getSlots() - 1).isEmpty(), "Catch-up must consume one fuel");
                        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                        level.getBlockState(pos).useWithoutItem(level, player, hit(pos));
                        helper.assertTrue(player.getInventory().countItem(Items.ROTTEN_FLESH) == 1
                                && handler.getStackInSlot(0).is(Items.LEATHER), "Hand collection must prefer the newest kind");
                        helper.assertTrue(handler.extractItem(0, 1, true).is(Items.LEATHER)
                                && handler.getStackInSlot(0).is(Items.LEATHER), "Simulated extraction must leave the product in place");
                        helper.setBlock(PLATE.below(), Blocks.HOPPER);
                        var hopper = (HopperBlockEntity) level.getBlockEntity(pos.below());
                        helper.assertTrue(HopperBlockEntity.suckInItems(level, hopper) && hopper.getItem(0).is(Items.LEATHER)
                                && handler.getStackInSlot(0).isEmpty(), "A vanilla hopper must collect the completed item");
                    } finally {
                        level.setDayTime(previousTime);
                    }
                })
                .thenSucceed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void plateProcessesAllDueItemsAtMidnight(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(PLATE);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.setBlock(PLATE, BlockRegistry.STARLIGHT_GATHERING_PLATE.get());
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(skyLight(level, pos) >= 12, "Plate must have an open sky"))
                .thenExecute(() -> {
                    var handler = handler(level, pos);
                    int fuelSlot = handler.getSlots() - 1;
                    long midnight = futureMidnight(level);
                    long previousTime = level.getDayTime();
                    try {
                        level.setDayTime(midnight - 1);
                        handler.insertItem(0, new ItemStack(Items.ROTTEN_FLESH), false);
                        handler.insertItem(1, new ItemStack(Items.ROTTEN_FLESH), false);
                        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemRegistry.STARDUST_SHARD.get(), 2));
                        useHeldItem(level, pos, player);
                        useHeldItem(level, pos, player);
                        helper.assertTrue(player.getMainHandItem().getCount() == 1, "An already fueled plate must not consume another shard");
                        tickPlate(level, pos);
                        helper.assertTrue(handler.getStackInSlot(0).is(Items.ROTTEN_FLESH) && !handler.getStackInSlot(fuelSlot).isEmpty(),
                                "A scheduled batch must wait until midnight");

                        level.setDayTime(midnight);
                        tickPlate(level, pos);
                        helper.assertTrue(handler.getStackInSlot(fuelSlot).isEmpty(), "Processing the whole batch must use the one stored shard");
                        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                        level.getBlockState(pos).useWithoutItem(level, player, hit(pos));
                        helper.assertTrue(player.getInventory().countItem(Items.LEATHER) == 2,
                                "The same batch must convert both materials and allow collecting them together");
                    } finally {
                        level.setDayTime(previousTime);
                    }
                })
                .thenSucceed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void plateSharesCapacityAcrossHandAndAutomationAndDropsContents(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(PLATE);
        helper.setBlock(PLATE, BlockRegistry.STARLIGHT_GATHERING_PLATE.get());
        var handler = handler(level, pos);
        int fuelSlot = handler.getSlots() - 1;
        var fuel = new ItemStack(ItemRegistry.STARDUST_SHARD.get());
        var materials = new ItemStack(Items.ROTTEN_FLESH, 10);
        for (Direction direction : Direction.values()) {
            var sided = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, direction);
            helper.assertTrue(sided != null && sided.insertItem(0, materials, true).getCount() == 9
                            && sided.insertItem(fuelSlot, fuel, true).isEmpty(),
                    "Every face must accept one ingredient or fuel in simulation");
        }
        helper.assertTrue(handler.getStackInSlot(0).isEmpty() && handler.getStackInSlot(fuelSlot).isEmpty(),
                "Simulated insertion must leave the plate unchanged");
        helper.assertTrue(handler.insertItem(0, new ItemStack(Items.COBBLESTONE), false).is(Items.COBBLESTONE),
                "The plate must reject items without a recipe");

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, materials);
        useHeldItem(level, pos, player);
        helper.assertTrue(materials.getCount() == 9 && handler.getStackInSlot(0).is(Items.ROTTEN_FLESH),
                "The first hand interaction must insert only one item");
        useHeldItem(level, pos, player);
        helper.assertTrue(materials.getCount() == 2, "The repeated hand interaction must fill the remaining seven spaces");
        useHeldItem(level, pos, player);
        helper.assertTrue(materials.getCount() == 2, "A full plate must preserve the held stack");
        helper.assertTrue(handler.insertItem(fuelSlot, fuel, false).isEmpty(), "A full plate must still have its own fuel space");
        helper.assertTrue(handler.extractItem(0, 1, false).isEmpty() && handler.extractItem(fuelSlot, 1, false).isEmpty(),
                "Automation must not extract uncooked ingredients or fuel");

        level.removeBlock(pos, false);
        helper.assertItemEntityCountIs(Items.ROTTEN_FLESH, PLATE, 1.5, 8);
        helper.assertItemEntityCountIs(ItemRegistry.STARDUST_SHARD.get(), PLATE, 1.5, 1);
        helper.succeed();
    }

    private static int skyLight(ServerLevel level, BlockPos pos) {
        return level.getBrightness(LightLayer.SKY, pos.above());
    }

    private static long futureMidnight(ServerLevel level) {
        return (Math.floorDiv(level.getDayTime(), 24_000) + 1) * 24_000 + 18_000;
    }

    private static IItemHandler handler(ServerLevel level, BlockPos pos) {
        return Objects.requireNonNull(level.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.DOWN));
    }

    private static void tickPlate(ServerLevel level, BlockPos pos) {
        var type = BlockEntityRegistry.STARLIGHT_GATHERING_PLATE.get();
        var state = level.getBlockState(pos);
        Objects.requireNonNull(state.getTicker(level, type))
                .tick(level, pos, state, level.getBlockEntity(pos, type).orElseThrow());
    }

    private static void useHeldItem(ServerLevel level, BlockPos pos, Player player) {
        level.getBlockState(pos).useItemOn(player.getMainHandItem(), level, player, InteractionHand.MAIN_HAND, hit(pos));
    }

    private static BlockHitResult hit(BlockPos pos) {
        return new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
    }
}
