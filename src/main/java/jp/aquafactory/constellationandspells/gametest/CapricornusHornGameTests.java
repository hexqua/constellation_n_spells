package jp.aquafactory.constellationandspells.gametest;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.SpellRegistry;
import jp.aquafactory.constellationandspells.spell.capricornushorn.CapricornusHorn;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Objects;
import java.util.function.Consumer;

import static io.redspace.ironsspellbooks.api.registry.SpellRegistry.COUNTERSPELL_SPELL;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
@SuppressWarnings("unused") // GameTest がアノテーションから探索する。
public final class CapricornusHornGameTests {
    private CapricornusHornGameTests() {}

    @GameTest(template = "gametest/basic_floor")
    public static void hornPlacementUsesAimAndOrderedFallbacks(GameTestHelper helper) {
        var caster = cow(helper, 4.5, 10, 4.25);
        caster.setXRot(-90);
        Vec3 eye = caster.getEyePosition();
        var upward = cast(helper, caster);
        assertNear(helper, upward.getY(), eye.y + 2, "Clear upward aim must place the horn above the caster");
        upward.discard();

        var ceiling = BlockPos.containing(eye.add(0, 2, 0));
        helper.getLevel().setBlockAndUpdate(ceiling, Blocks.STONE.defaultBlockState());
        var horizontal = cast(helper, caster);
        assertNear(helper, horizontal.getZ(), eye.z + 2, "A blocked aim must retry horizontally");
        assertNear(helper, horizontal.getY(), eye.y, "Horizontal retry must stay at eye height");
        horizontal.discard();

        var wall = BlockPos.containing(eye.add(0, 0, 1));
        helper.getLevel().setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
        var shortened = cast(helper, caster);
        helper.assertTrue(shortened.getZ() > eye.z && shortened.getZ() + 0.375 <= wall.getZ() + 1.0e-6,
                "A thin wall must shorten placement and keep the whole horn on the caster side");
        shortened.discard();

        helper.getLevel().setBlockAndUpdate(BlockPos.containing(eye), Blocks.STONE.defaultBlockState());
        var fallback = cast(helper, caster);
        helper.assertTrue(fallback.position().equals(caster.position()), "Blocked candidates must fall back exactly to the caster");
        fallback.discard();
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void hornHitsBoundingBoxesAndLaunchesThroughWalls(GameTestHelper helper) {
        var caster = cow(helper, 4.5, 10, 4.5);
        var visible = cow(helper, 2.5, 10, 6.5);
        var concealed = cow(helper, 7.5, 10, 6.5);
        var resistant = cow(helper, 4.5, 10, 8.5);
        var immune = cow(helper, 3.5, 10, 8.5);
        var rising = cow(helper, 4.5, 10, 3.0);
        var outside = cow(helper, 4.5, 10, 15.5);
        var edge = cow(helper, 4.5, 10, 14.75);
        var ally = cow(helper, 4.5, 10, 1.5);
        for (int y = 10; y <= 12; y++) {
            helper.setBlock(new BlockPos(6, y, 6), Blocks.STONE);
        }
        Objects.requireNonNull(resistant.getAttribute(Attributes.KNOCKBACK_RESISTANCE)).setBaseValue(0.5);
        Objects.requireNonNull(immune.getAttribute(Attributes.KNOCKBACK_RESISTANCE)).setBaseValue(1.0);
        visible.setDeltaMovement(0.1, -0.4, -0.1);
        rising.setDeltaMovement(0, 1.2, 0);

        var scoreboard = helper.getLevel().getScoreboard();
        var team = scoreboard.addPlayerTeam("capricornus_test");
        team.setAllowFriendlyFire(false);
        scoreboard.addPlayerToTeam(caster.getScoreboardName(), team);
        scoreboard.addPlayerToTeam(ally.getScoreboardName(), team);
        try {
            var horn = cast(helper, caster);
            assertNear(helper, visible.getHealth(), 1, "Clear targets must take full base damage immediately");
            assertNear(helper, concealed.getHealth(), 5.5, "Stone must halve damage");
            assertNear(helper, visible.getDeltaMovement().y, 1, "A clear hit must lift a falling target");
            assertNear(helper, visible.getDeltaMovement().x, 0.1, "Launch must retain horizontal motion");
            assertNear(helper, visible.getDeltaMovement().z, -0.1, "Launch must retain horizontal motion");
            assertNear(helper, concealed.getDeltaMovement().y, 0.5, "The hit through stone must use weaker lift");
            assertNear(helper, resistant.getDeltaMovement().y, 0.5, "Partial knockback resistance must reduce lift");
            assertNear(helper, immune.getHealth(), 1, "Knockback immunity must still allow damage");
            assertNear(helper, immune.getDeltaMovement().y, 0, "Full knockback resistance must prevent lift");
            assertNear(helper, rising.getDeltaMovement().y, 1.2, "Launch must retain faster upward motion");
            assertNear(helper, edge.getHealth(), 1, "An intersecting AABB must count even when its center is outside");
            assertNear(helper, outside.getHealth(), 10, "Disjoint targets must not be hit");
            assertNear(helper, ally.getHealth(), 10, "Protected allies must not take damage");
            assertNear(helper, ally.getDeltaMovement().y, 0, "Protected allies must not be launched");
            assertNear(helper, caster.getHealth(), 10, "The caster must not take damage");
            horn.discard();
        } finally {
            scoreboard.removePlayerTeam(team);
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor", timeoutTicks = 70)
    public static void hornUsesOneAttemptPerTargetInItsOwnHitWindow(GameTestHelper helper) {
        var caster = cow(helper, 4.5, 10, 4.5);
        var immuneOnCast = cow(helper, 2.5, 10, 6.5);
        var entrant = cow(helper, 4.5, 10, 16.5);
        var late = cow(helper, 7.5, 10, 16.5);
        immuneOnCast.hurt(helper.getLevel().damageSources().generic(), 9);
        immuneOnCast.heal(9);
        immuneOnCast.setDeltaMovement(Vec3.ZERO);

        var horn = cast(helper, caster);
        assertNear(helper, immuneOnCast.getHealth(), 10, "Existing hurt immunity must stop initial damage");
        assertNear(helper, immuneOnCast.getDeltaMovement().y, 0, "A rejected hit must not launch the target");
        helper.assertTrue(!horn.save(new CompoundTag()), "The short-lived horn must not be saved");

        // 術者から見て角笛が正面にある状態で、依存 MOD の Counterspell を実行する。
        COUNTERSPELL_SPELL.get().onCast(helper.getLevel(), 1, caster, CastSource.MOB, MagicData.getPlayerMagicData(caster));
        helper.assertTrue(!horn.isRemoved(), "Counterspell must not remove the horn");
        caster.discard();

        helper.runAfterDelay(2, () -> {
            immuneOnCast.invulnerableTime = 0;
            entrant.setPos(helper.absoluteVec(new Vec3(4.5, 10, 9.5)));
        });
        helper.runAfterDelay(6, () -> {
            assertNear(helper, immuneOnCast.getHealth(), 10, "The horn must not retry even after immunity ends");
            assertNear(helper, entrant.getHealth(), 1, "The horn must hit a later entrant after Counterspell and owner removal");
            entrant.invulnerableTime = 0;
            entrant.setDeltaMovement(Vec3.ZERO);
            entrant.setPos(helper.absoluteVec(new Vec3(4.5, 10, 9.5)));
        });
        helper.runAfterDelay(10, () -> late.setPos(helper.absoluteVec(new Vec3(7.5, 10, 9.5))));
        helper.runAfterDelay(13, () -> {
            assertNear(helper, late.getHealth(), 10, "The expanding effect must stop hitting at age ten");
            assertNear(helper, entrant.getHealth(), 1, "A successful hit must also consume the only attempt");
            helper.assertTrue(!horn.isRemoved(), "The horn must remain visible after the hit window");
        });
        helper.runAfterDelay(42, () -> {
            helper.assertTrue(horn.isRemoved(), "The horn must expire even when its caster is gone");
            helper.succeed();
        });
    }

    @GameTest(template = "gametest/basic_floor")
    public static void verticalLaunchHonorsKnockbackEvents(GameTestHelper helper) {
        var caster = cow(helper, 4.5, 10, 4.5);
        var canceled = cow(helper, 2.5, 10, 6.5);
        var reduced = cow(helper, 4.5, 10, 9.5);
        Consumer<LivingKnockBackEvent> listener = event -> {
            // 通常のダメージによる押し出しとは別に、打ち上げを受け取れることを確認する。
            if (event.getOriginalStrength() != 1.0f) return;
            if (event.getEntity() == canceled) event.setCanceled(true);
            if (event.getEntity() == reduced) event.setStrength(0.25f);
        };
        NeoForge.EVENT_BUS.addListener(listener);
        try {
            var horn = cast(helper, caster);
            assertNear(helper, canceled.getHealth(), 1, "A canceled launch must leave damage intact");
            assertNear(helper, canceled.getDeltaMovement().lengthSqr(), 0, "A canceled launch must leave velocity intact");
            assertNear(helper, reduced.getDeltaMovement().y, 0.25, "Modified knockback strength must set launch velocity");
            horn.discard();
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
        }
        helper.succeed();
    }

    private static CapricornusHorn cast(GameTestHelper helper, Cow caster) {
        var level = helper.getLevel();
        SpellRegistry.CAPRICORNUS_HORN.get().onCast(level, 1, caster, CastSource.MOB, MagicData.getPlayerMagicData(caster));
        var horns = level.getEntitiesOfClass(CapricornusHorn.class, caster.getBoundingBox().inflate(5));
        helper.assertTrue(horns.size() == 1, "A cast must summon one horn");
        return horns.getFirst();
    }

    private static Cow cow(GameTestHelper helper, double x, double y, double z) {
        var cow = helper.spawn(EntityType.COW, BlockPos.containing(x, y, z));
        cow.setPos(helper.absoluteVec(new Vec3(x, y, z)));
        cow.setYRot(0);
        cow.setXRot(0);
        cow.setNoAi(true);
        cow.setNoGravity(true);
        return cow;
    }

    private static void assertNear(GameTestHelper helper, double actual, double expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) < 1.0e-5, message + " (expected " + expected + ", got " + actual + ")");
    }
}
