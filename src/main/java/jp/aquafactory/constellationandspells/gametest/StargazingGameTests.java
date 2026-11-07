package jp.aquafactory.constellationandspells.gametest;

import com.mojang.authlib.GameProfile;
import com.google.gson.JsonParser;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.SpellRegistry;
import jp.aquafactory.constellationandspells.spell.stargazing.StargazingArea;
import jp.aquafactory.constellationandspells.spell.stargazing.StargazingCastData;
import jp.aquafactory.constellationandspells.spell.stargazing.StargazingChart;
import jp.aquafactory.constellationandspells.spell.stargazing.StargazingDamage;
import jp.aquafactory.constellationandspells.spell.stargazing.StargazingField;
import jp.aquafactory.constellationandspells.spell.stargazing.StargazingTargeting;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
@SuppressWarnings("unused") // GameTest がアノテーションから探索する。
public final class StargazingGameTests {
    private StargazingGameTests() {}

    @GameTest(template = "gametest/basic_floor")
    public static void bundledChartResourcesProduceDrawableConstellations(GameTestHelper helper) {
        for (var id : StargazingChart.IDS) {
            var path = "/assets/constellationandspells/stargazing/" + id + ".json";
            try (var stream = Objects.requireNonNull(StargazingGameTests.class.getResourceAsStream(path), "Missing chart " + id);
                 var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                // client の GL 初期化には依存せず、配布資産を実際の parser に通して線分の接続を検証する。
                var chart = StargazingChart.parse(JsonParser.parseReader(reader));
                var area = new StargazingArea(Vec3.ZERO, 1.5f, 0);
                for (var edge : chart.edges()) {
                    var a = chart.stars().get(edge.a());
                    var b = chart.stars().get(edge.b());
                    for (var star : List.of(a, b)) {
                        var position = new Vec3(star.x() * area.halfExtent(), 0.5, star.z() * area.halfExtent());
                        helper.assertTrue(area.bounds().contains(position), "Chart connection must fit inside the field: " + id);
                    }
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to read chart " + id, exception);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void groundWinsOverAnEntityInFront(GameTestHelper helper) {
        floor(helper);
        var caster = cow(helper, 1.5, 10, 1.5);
        cow(helper, 1.5, 10, 3.5);
        caster.setYRot(0);
        caster.setXRot(20);
        var target = StargazingTargeting.resolve(helper.getLevel(), caster, 8).orElseThrow();
        helper.assertTrue(target.z > helper.absoluteVec(new Vec3(1.5, 10, 4)).z,
                "Ground behind the aimed entity must take priority over its feet");
        helper.assertTrue(Math.abs(target.y - helper.absolutePos(new BlockPos(0, 10, 0)).getY()) < 0.0001,
                "Ground targeting must use the support surface");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void horizontalAimFallsBackToFeetAndSearchIsLimited(GameTestHelper helper) {
        var caster = cow(helper, 1.5, 10, 1.5);
        var target = cow(helper, 1.5, 10, 3.5);
        helper.setBlock(new BlockPos(1, 9, 3), Blocks.STONE);
        caster.setYRot(0);
        caster.setXRot(0);
        var ground = StargazingTargeting.resolve(helper.getLevel(), caster, 8).orElseThrow();
        helper.assertTrue(ground.distanceToSqr(helper.absoluteVec(new Vec3(1.5, 10, 3.5))) < 0.0001,
                "Horizontal aim must fall back to the target's ground");
        target.setPos(target.position().add(0, 3, 0));
        caster.setPos(caster.position().add(0, 3, 0));
        helper.assertTrue(StargazingTargeting.resolve(helper.getLevel(), caster, 8).isPresent(),
                "Ground three blocks below the target's feet must resolve");
        target.setPos(target.position().add(0, 1, 0));
        caster.setPos(caster.position().add(0, 1, 0));
        helper.assertTrue(StargazingTargeting.resolve(helper.getLevel(), caster, 8).isEmpty(),
                "Ground four blocks below the target's feet must not resolve");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void fallbackGroundMayExceedTheOriginalRange(GameTestHelper helper) {
        var caster = cow(helper, 1.5, 10, 1.5);
        cow(helper, 1.5, 10, 9.2);
        helper.setBlock(new BlockPos(1, 6, 9), Blocks.STONE);
        caster.setYRot(0);
        caster.setXRot(0);
        var ground = StargazingTargeting.resolve(helper.getLevel(), caster, 8).orElseThrow();
        helper.assertTrue(ground.distanceTo(caster.getEyePosition()) > 8,
                "Resolving ground must not impose a second range limit after acquiring an entity");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void slabSurfaceResolvesWithoutClearingTheArea(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 9, 3), Blocks.STONE_SLAB);
        helper.setBlock(new BlockPos(2, 10, 3), Blocks.STONE);
        var ground = StargazingTargeting.groundBelow(helper.getLevel(), helper.absolutePos(new BlockPos(1, 10, 3)), 3).orElseThrow();
        helper.assertTrue(Math.abs(ground.y - helper.absoluteVec(new Vec3(1.5, 9.5, 3.5)).y) < 0.0001,
                "Partial blocks must use the collision surface and nearby walls must not reject placement");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void rotatedAreaRejectsOuterCornersAndIncludesEdges(GameTestHelper helper) {
        var area = new StargazingArea(Vec3.ZERO, 1, 45);
        helper.assertTrue(area.intersects(new AABB(1.2, 0, -0.1, 1.4, 1, 0.1)),
                "A rotated corner must hit an overlapping target even when its center is outside the square");
        helper.assertTrue(!area.intersects(new AABB(1.1, 0, 1.1, 1.3, 1, 1.3)),
                "A target inside only the enclosing AABB must not be hit");
        helper.assertTrue(!area.intersects(new AABB(-0.1, 1.01, -0.1, 0.1, 2, 0.1)),
                "Targets above the one-block field must not be hit");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void glassPaneBlocksProtrudingTargetsButWaterDoesNot(GameTestHelper helper) {
        var base = helper.absoluteVec(new Vec3(1.5, 10, 1.5));
        var area = new StargazingArea(base, 2, 0);
        var target = cow(helper, 1.5, 10, 2.75);
        helper.setBlock(new BlockPos(1, 10, 2), Blocks.GLASS_PANE);
        helper.setBlock(new BlockPos(1, 11, 2), Blocks.GLASS_PANE);
        helper.assertTrue(area.intersects(target.getBoundingBox()) && !area.canReach(helper.getLevel(), target),
                "A protruding body must not take damage through a glass pane blocking its center");
        helper.setBlock(new BlockPos(1, 10, 2), Blocks.WATER);
        helper.setBlock(new BlockPos(1, 11, 2), Blocks.AIR);
        helper.assertTrue(area.canReach(helper.getLevel(), target), "Fluids must not occlude the center ray");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void castKeepsInitialAreaAndCancelsItsPreview(GameTestHelper helper) {
        floor(helper);
        var caster = player(helper, "stargazing_cast");
        caster.setYRot(0);
        caster.setXRot(35);
        caster.setYHeadRot(37);
        var spell = SpellRegistry.STARGAZING.get();
        var magic = MagicData.getPlayerMagicData(caster);
        helper.assertTrue(spell.checkPreCastConditions(helper.getLevel(), 2, caster, magic), "Cast must acquire ground");
        var data = (StargazingCastData) Objects.requireNonNull(magic.getAdditionalCastData());
        var original = data.area();
        magic.initiateCast(spell, 2, 15, CastSource.SPELLBOOK, "mainhand");
        spell.onServerPreCast(helper.getLevel(), 2, caster, magic);
        var field = (StargazingField) Objects.requireNonNull(helper.getLevel().getEntity(data.preview()));
        helper.assertTrue(!field.active(), "Casting must create a preview");
        caster.setPos(caster.position().add(20, 0, 0));
        caster.setYHeadRot(180);
        spell.onCast(helper.getLevel(), 2, caster, CastSource.SPELLBOOK, magic);
        helper.assertTrue(field.active() && field.area().equals(original) && original.yaw() == 37,
                "Cast completion must retain the initial position, head yaw and size outside the original range");
        spell.onServerCastComplete(helper.getLevel(), 2, caster, magic, false);
        helper.assertTrue(!field.isRemoved() && magic.getAdditionalCastData() == null,
                "Cast cleanup must retain an active field and release temporary cast data");
        field.discard();
        caster.discard();

        var nextCaster = player(helper, "stargazing_cancel");
        nextCaster.setXRot(35);
        var nextMagic = MagicData.getPlayerMagicData(nextCaster);
        helper.assertTrue(spell.checkPreCastConditions(helper.getLevel(), 1, nextCaster, nextMagic), "Second cast must acquire ground");
        nextMagic.initiateCast(spell, 1, 15, CastSource.SPELLBOOK, "mainhand");
        spell.onServerPreCast(helper.getLevel(), 1, nextCaster, nextMagic);
        var nextData = (StargazingCastData) Objects.requireNonNull(nextMagic.getAdditionalCastData());
        var preview = helper.getLevel().getEntity(nextData.preview());
        spell.onServerCastComplete(helper.getLevel(), 1, nextCaster, nextMagic, true);
        helper.assertTrue(preview != null && preview.isRemoved(), "Cancellation must remove the preview immediately");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor", timeoutTicks = 120)
    public static void pulsesUseStoredDamageAtTheScheduledTimes(GameTestHelper helper) {
        var caster = player(helper, "stargazing_pulses");
        var target = cow(helper, 2.5, 10, 2.5);
        Objects.requireNonNull(target.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20);
        target.setHealth(20);
        var spell = SpellRegistry.STARGAZING.get();
        var magic = MagicData.getPlayerMagicData(caster);
        var data = new StargazingCastData(new StargazingArea(target.position(), 1.5f, 23));
        magic.setAdditionalCastData(data);
        magic.initiateCast(spell, 2, 15, CastSource.SPELLBOOK, "mainhand");
        spell.onServerPreCast(helper.getLevel(), 2, caster, magic);
        var field = (StargazingField) Objects.requireNonNull(helper.getLevel().getEntity(data.preview()));
        spell.onCast(helper.getLevel(), 2, caster, CastSource.SPELLBOOK, magic);
        spell.onServerCastComplete(helper.getLevel(), 2, caster, magic, false);
        long start = helper.getLevel().getGameTime();
        var times = new ArrayList<Long>();
        var amounts = new ArrayList<Float>();
        Consumer<SpellDamageEvent> observer = event -> {
            if (event.getEntity() == target) {
                times.add(helper.getLevel().getGameTime() - start);
                amounts.add(event.getAmount());
            }
        };
        NeoForge.EVENT_BUS.addListener(observer);
        Objects.requireNonNull(caster.getAttribute(AttributeRegistry.SPELL_POWER)).setBaseValue(10);
        helper.runAfterDelay(101, () -> {
            NeoForge.EVENT_BUS.unregister(observer);
            helper.assertTrue(times.equals(List.of(20L, 35L, 50L, 65L, 80L, 95L)), "Damage must occur at the six scheduled world ticks");
            helper.assertTrue(amounts.stream().allMatch(amount -> Math.abs(amount - 1.8f) < 0.0001),
                    "Caster power changes must not affect stored field damage");
            helper.assertTrue(Math.abs(target.getHealth() - (20 - 6 * 1.8f)) < 0.001, "Each scheduled pulse must actually damage the target");
            helper.assertTrue(field.isRemoved(), "Field must expire after one hundred ticks");
            caster.discard();
            helper.succeed();
        });
    }

    @GameTest(template = "gametest/basic_floor")
    public static void damageRespectsImmunityAndOnlySuppressesItsOwnKnockback(GameTestHelper helper) {
        var caster = cow(helper, 1.5, 10, 1.5);
        var target = cow(helper, 1.5, 10, 3.5);
        target.hurt(helper.getLevel().damageSources().generic(), 1);
        target.setDeltaMovement(Vec3.ZERO);
        float health = target.getHealth();
        helper.assertTrue(!StargazingDamage.apply(caster, caster, target, 1) && target.getHealth() == health,
                "Stargazing must respect existing hurt immunity");
        target.knockback(0.4, 1, 0);
        helper.assertTrue(target.getDeltaMovement().lengthSqr() > 0, "A rejected hit must not suppress unrelated knockback");
        target.setDeltaMovement(Vec3.ZERO);
        target.invulnerableTime = 0;
        helper.assertTrue(StargazingDamage.apply(caster, caster, target, 1) && target.getDeltaMovement().equals(Vec3.ZERO),
                "Successful Stargazing damage must not produce knockback");
        target.knockback(0.4, 1, 0);
        helper.assertTrue(target.getDeltaMovement().lengthSqr() > 0, "Successful damage must release knockback suppression immediately");
        helper.assertTrue(!StargazingDamage.apply(caster, caster, caster, 1), "Caster must never damage itself");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void standardFriendlyFireProtectionIsPreserved(GameTestHelper helper) {
        var caster = cow(helper, 1.5, 10, 1.5);
        var target = cow(helper, 1.5, 10, 3.5);
        var scoreboard = helper.getLevel().getScoreboard();
        var team = scoreboard.addPlayerTeam("stargazing_test");
        team.setAllowFriendlyFire(false);
        scoreboard.addPlayerToTeam(caster.getScoreboardName(), team);
        scoreboard.addPlayerToTeam(target.getScoreboardName(), team);
        float health = target.getHealth();
        try {
            helper.assertTrue(!StargazingDamage.apply(caster, caster, target, 1) && target.getHealth() == health,
                    "Iron's standard friendly-fire protection must remain active");
        } finally {
            scoreboard.removePlayerTeam(team);
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void invalidOwnersRemoveFieldsAndFieldsAreNotSaved(GameTestHelper helper) {
        var caster = cow(helper, 1.5, 10, 1.5);
        var field = StargazingField.preview(helper.getLevel(), caster, new StargazingArea(caster.position(), 1.5f, 0));
        field.activate(1, 100);
        helper.assertTrue(!field.save(new CompoundTag()), "Field must never be persisted to chunk NBT");
        caster.setHealth(0);
        field.tick();
        helper.assertTrue(field.isRemoved(), "A dead owner must remove the field");
        var removedCaster = cow(helper, 1.5, 10, 1.5);
        var removedField = StargazingField.preview(helper.getLevel(), removedCaster, new StargazingArea(removedCaster.position(), 1.5f, 0));
        removedField.activate(1, 100);
        removedCaster.discard();
        removedField.tick();
        helper.assertTrue(removedField.isRemoved(), "A removed owner must remove the field");
        var movingCaster = cow(helper, 1.5, 10, 1.5);
        var movingField = StargazingField.preview(helper.getLevel(), movingCaster, new StargazingArea(movingCaster.position(), 1.5f, 0));
        movingField.activate(1, 100);
        var nether = Objects.requireNonNull(helper.getLevel().getServer().getLevel(Level.NETHER));
        var moved = movingCaster.changeDimension(new DimensionTransition(nether, new Vec3(0, 100, 0), Vec3.ZERO, 0, 0, DimensionTransition.DO_NOTHING));
        helper.assertTrue(moved != null && moved.level() == nether, "Owner must change dimension for this test");
        movingField.tick();
        helper.assertTrue(movingField.isRemoved(), "Dimension changes must remove the field");
        if (moved != null) moved.discard();
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void inactiveGroundDoesNotLoadAChunk(GameTestHelper helper) {
        var caster = cow(helper, 1.5, 10, 1.5);
        var base = new Vec3(20000000, 100, 20000000);
        var field = StargazingField.preview(helper.getLevel(), caster, new StargazingArea(base, 1.5f, 0));
        helper.assertTrue(field.isRemoved() && !helper.getLevel().getChunkSource().hasChunk(1250000, 1250000),
                "Inactive ground must reject a field without loading its chunk");
        helper.succeed();
    }

    private static LivingEntity cow(GameTestHelper helper, double x, double y, double z) {
        var cow = helper.spawn(EntityType.COW, new BlockPos((int) x, (int) y, (int) z));
        cow.setPos(helper.absoluteVec(new Vec3(x, y, z)));
        cow.setNoAi(true);
        cow.setNoGravity(true);
        return cow;
    }

    private static ServerPlayer player(GameTestHelper helper, String name) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)), name));
        player.setPos(helper.absoluteVec(new Vec3(1.5, 10, 1.5)));
        player.setYRot(0);
        player.setXRot(0);
        Objects.requireNonNull(player.getAttribute(AttributeRegistry.SPELL_POWER)).setBaseValue(1);
        var data = MagicData.getPlayerMagicData(player);
        data.setServerPlayer(player);
        data.resetCastingState();
        return player;
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 6; x++) for (int z = 0; z < 8; z++) helper.setBlock(new BlockPos(x, 9, z), Blocks.STONE);
    }
}
