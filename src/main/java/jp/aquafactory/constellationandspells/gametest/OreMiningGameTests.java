package jp.aquafactory.constellationandspells.gametest;

import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.BlockRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
// GameTest がアノテーションから探索して呼び出す。
@SuppressWarnings("unused")
public final class OreMiningGameTests {
    private static List<Block> ores() {
        return List.of(BlockRegistry.STELLAR_DROP_ORE.get(), BlockRegistry.DEEPSLATE_STELLAR_DROP_ORE.get());
    }

    @GameTest(template = "gametest/basic_floor")
    public static void miningRequiresStonePickaxeAndRespectsSilkTouch(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = FakePlayerFactory.getMinecraft(level);
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        for (var ore : ores()) {
            mineAndCheck(helper, ore, ItemStack.EMPTY, ItemStack.EMPTY, false);
            mineAndCheck(helper, ore, new ItemStack(Items.WOODEN_PICKAXE), ItemStack.EMPTY, false);
            mineAndCheck(helper, ore, new ItemStack(Items.GOLDEN_PICKAXE), ItemStack.EMPTY, false);
            mineAndCheck(helper, ore, new ItemStack(Items.IRON_AXE), ItemStack.EMPTY, false);
            for (var tool : List.of(Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE)) {
                mineAndCheck(helper, ore, new ItemStack(tool), new ItemStack(ItemRegistry.STARDUST_SHARD.get()), true);
            }
            var silk = silkTouchPickaxe(helper);
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
        int xp = totalExperience(orbs);
        helper.assertTrue(experience ? xp >= 4 && xp <= 8 : xp == 0,
                "Mining experience must be 4-8 only for suitable tools without Silk Touch, got " + xp
                        + "; ore=" + ore + "; tool=" + tool + "; orbs(value x count)="
                        + orbs.stream().map(orb -> orb.getValue() + " x " + orbCount(orb)).toList());
        drops.forEach(ItemEntity::discard);
        orbs.forEach(ExperienceOrb::discard);
    }

    private static int totalExperience(List<ExperienceOrb> orbs) {
        return orbs.stream().mapToInt(orb -> orb.getValue() * orbCount(orb)).sum();
    }

    private static int orbCount(ExperienceOrb orb) {
        // 生成時に同額オーブが統合されると value は変わらず count が増える。公開 getter がないため保存値を使う。
        var data = new CompoundTag();
        orb.addAdditionalSaveData(data);
        return data.getInt("Count");
    }

    @GameTest(template = "gametest/basic_floor")
    public static void experienceAccountingIsInvariantUnderOrbMerging(GameTestHelper helper) {
        var firstOrb = new ExperienceOrb(helper.getLevel(), 0, 0, 0, 3);
        var secondOrb = new ExperienceOrb(helper.getLevel(), 0, 0, 0, 3);
        int unmergedExperience = totalExperience(List.of(firstOrb, secondOrb));
        helper.assertTrue(unmergedExperience == 6, "Two unmerged 3 XP orbs must represent 6 XP");
        var mergedOrb = new ExperienceOrb(helper.getLevel(), 0, 0, 0, 3);
        var data = new CompoundTag();
        mergedOrb.addAdditionalSaveData(data);
        // 統合時は entity 数と value だけでは合計を求められず、採掘テストが不安定になる。
        // 統合自体の乱数に依存せず、3 XP × 2 個の統合状態を再現して集計の前提を保証する。
        data.putInt("Count", 2);
        mergedOrb.readAdditionalSaveData(data);
        helper.assertTrue(totalExperience(List.of(mergedOrb)) == unmergedExperience,
                "Orb merging must preserve the total observed XP");
        var singleOrb = new ExperienceOrb(helper.getLevel(), 0, 0, 0, 1);
        helper.assertTrue(totalExperience(List.of(mergedOrb, singleOrb)) == 7,
                "Merged and unmerged orbs must both contribute their full XP");
        helper.succeed();
    }

    private static ItemStack silkTouchPickaxe(GameTestHelper helper) {
        var tool = new ItemStack(Items.DIAMOND_PICKAXE);
        tool.enchant(helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.SILK_TOUCH), 1);
        return tool;
    }
}
