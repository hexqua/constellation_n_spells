package jp.aquafactory.constellationandspells.gametest;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
// GameTest がアノテーションから探索して呼び出す。
@SuppressWarnings("unused")
public class OreMiningGameTests {
    private static List<Block> ores() {
        return List.of(BlockRegistry.STELLAR_DROP_ORE.get(), BlockRegistry.DEEPSLATE_STELLAR_DROP_ORE.get());
    }

    @GameTest(template = "gametest/basic_floor")
    public static void miningRequiresStonePickaxeAndRespectsSilkTouch(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = FakePlayerFactory.getMinecraft(level);
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        var explosion = new Explosion(level, player, pos.getX(), pos.getY(), pos.getZ(),
                1.0F, false, Explosion.BlockInteraction.DESTROY);
        for (var ore : ores()) {
            helper.assertTrue(ore.defaultBlockState().getDestroySpeed(level, pos)
                            == (ore == BlockRegistry.STELLAR_DROP_ORE.get() ? 3.0F : 4.5F),
                    "Ore hardness must match its stone variant");
            helper.assertTrue(ore.defaultBlockState().getExplosionResistance(level, pos, explosion) == 3.0F,
                    "Ore blast resistance must be 3");
            helper.assertTrue(ore.defaultBlockState().getSoundType(level, pos, player) ==
                            (ore == BlockRegistry.STELLAR_DROP_ORE.get() ? SoundType.STONE : SoundType.DEEPSLATE),
                    "Ore sound must match its stone variant");
            mineAndCheck(helper, ore, ItemStack.EMPTY, ItemStack.EMPTY, false);
            mineAndCheck(helper, ore, new ItemStack(Items.WOODEN_PICKAXE), ItemStack.EMPTY, false);
            mineAndCheck(helper, ore, new ItemStack(Items.GOLDEN_PICKAXE), ItemStack.EMPTY, false);
            mineAndCheck(helper, ore, new ItemStack(Items.IRON_AXE), ItemStack.EMPTY, false);
            for (var tool : List.of(Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE)) {
                mineAndCheck(helper, ore, new ItemStack(tool), new ItemStack(ItemRegistry.STARDUST_SHARD.get()), true);
            }
            var silk = enchantedPickaxe(helper, Enchantments.SILK_TOUCH, 1);
            mineAndCheck(helper, ore, silk, new ItemStack(ore), false);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.succeed();
    }

    private static void mineAndCheck(GameTestHelper helper, Block ore, ItemStack tool, ItemStack expected, boolean experience) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        var area = new AABB(pos).inflate(0.5);
        var player = FakePlayerFactory.getMinecraft(level);
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
        level.setBlockAndUpdate(pos, ore.defaultBlockState());
        helper.assertTrue(player.gameMode.destroyBlock(pos), "Survival mining must destroy the ore");
        var drops = level.getEntitiesOfClass(ItemEntity.class, area);
        if (expected.isEmpty()) {
            helper.assertTrue(drops.isEmpty(), "Unsuitable tools must not drop items");
        } else {
            helper.assertTrue(drops.size() == 1 && ItemStack.isSameItemSameComponents(drops.getFirst().getItem(), expected)
                            && drops.getFirst().getItem().getCount() == 1,
                    "Mining must drop exactly one expected item");
        }
        var orbs = level.getEntitiesOfClass(ExperienceOrb.class, area);
        int xp = orbs.stream().mapToInt(ExperienceOrb::getValue).sum();
        helper.assertTrue(experience ? xp >= 4 && xp <= 8 : xp == 0,
                "Mining experience must be 4-8 only for suitable tools without Silk Touch, got " + xp);
        drops.forEach(ItemEntity::discard);
        orbs.forEach(ExperienceOrb::discard);
    }

    @GameTest(template = "gametest/basic_floor")
    public static void fortuneUsesVanillaOreBonuses(GameTestHelper helper) {
        var level = helper.getLevel();
        for (var ore : ores()) {
            var table = level.getServer().reloadableRegistries().getLootTable(ore.getLootTable());
            for (int fortune = 0; fortune <= 3; fortune++) {
                var tool = fortune == 0 ? new ItemStack(Items.DIAMOND_PICKAXE)
                        : enchantedPickaxe(helper, Enchantments.FORTUNE, fortune);
                var params = new LootParams.Builder(level)
                        .withParameter(LootContextParams.BLOCK_STATE, ore.defaultBlockState())
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
                        .withParameter(LootContextParams.TOOL, tool).create(LootContextParamSets.BLOCK);
                var random = RandomSource.create(7351L);
                int maximum = 0;
                for (int sample = 0; sample < 256; sample++) {
                    var drops = table.getRandomItems(params, random);
                    helper.assertTrue(drops.size() == 1 && drops.getFirst().is(ItemRegistry.STARDUST_SHARD.get()),
                            "Fortune must only drop stardust shards");
                    int count = drops.getFirst().getCount();
                    helper.assertTrue(count >= 1 && count <= fortune + 1, "Fortune drop count is out of bounds");
                    maximum = Math.max(maximum, count);
                }
                helper.assertTrue(maximum == fortune + 1, "Fortune must reach its vanilla maximum drop count");
            }
        }
        helper.succeed();
    }

    private static ItemStack enchantedPickaxe(GameTestHelper helper, ResourceKey<Enchantment> enchantment, int level) {
        var tool = new ItemStack(Items.DIAMOND_PICKAXE);
        tool.enchant(helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(enchantment), level);
        return tool;
    }
}
