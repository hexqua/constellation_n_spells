package jp.aquafactory.constellationandspells.gametest;

import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.api.config.SpellConfigManager;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.AttributeRegistry;
import jp.aquafactory.constellationandspells.registry.EntityRegistry;
import jp.aquafactory.constellationandspells.registry.SpellRegistry;
import jp.aquafactory.constellationandspells.spell.stardust.StardustBursts;
import jp.aquafactory.constellationandspells.spell.stardust.StardustProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
@SuppressWarnings("unused") // GameTest がアノテーションから探索する。
public final class StardustGameTests {
    private StardustGameTests() {}

    @BeforeBatch(batch = "defaultBatch")
    public static void initializeSpellConfiguration(ServerLevel level) {
        // GameTestServer はプレイヤーログインを行わず、Iron's の遅延設定初期化が実行されない。
        SpellConfigManager.INSTANCE.handleServerConfigUpdate();
        SpellConfigManager.onDatapackSync(new OnDatapackSyncEvent(level.getServer().getPlayerList(), null));
        io.redspace.ironsspellbooks.api.registry.SpellRegistry.onConfigReload();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void spellConfigurationAndCone(GameTestHelper helper) {
        var spell = SpellRegistry.STARDUST.get();
        helper.assertTrue(spell.getCastType() == CastType.LONG && spell.getCastTime(1) == 20, "Stardust must be a 20-tick LONG cast");
        helper.assertTrue(spell.getMaxLevel() == 10 && spell.getSpellCooldown() == 10, "Stardust must have ten levels and a ten-tick cooldown");
        helper.assertTrue(spell.getManaCost(1) == 10 && spell.getManaCost(10) == 28, "Mana must scale from 10 to 28");
        helper.assertTrue(spell.getSpellPower(1, null) == 80 && spell.getSpellPower(10, null) == 170, "Power must scale from 80 to 170");
        var random = RandomSource.create(917234);
        for (var forward : List.of(new Vec3(0, 1, 0), new Vec3(0, -1, 0), new Vec3(1, 2, 3).normalize())) {
            for (int i = 0; i < 256; i++) {
                var direction = StardustBursts.sampleDirection(forward, random);
                helper.assertTrue(Math.abs(direction.length() - 1) < 0.000001, "Shot direction must be normalized");
                helper.assertTrue(direction.dot(forward) >= Math.cos(StardustBursts.HALF_ANGLE) - 0.000001, "Shot direction must stay within the ten-degree cone");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor", timeoutTicks = 40)
    public static void burstsKeepOriginAimAndPower(GameTestHelper helper) {
        var caster = caster(helper);
        var origin = caster.getEyePosition().add(0, -0.075, 0);
        var shots = new ArrayList<Shot>();
        var observer = observe(caster, shots);
        NeoForge.EVENT_BUS.addListener(observer);
        long started = helper.getLevel().getGameTime();
        StardustBursts.start(helper.getLevel(), caster, 1.6f);
        helper.assertTrue(shots.size() == 1, "First shot must spawn synchronously");
        helper.runAfterDelay(1, () -> {
            caster.setPos(caster.position().add(2, 0, 0));
            caster.setXRot(0);
            caster.setYRot(90);
        });
        helper.runAfterDelay(16, () -> {
            NeoForge.EVENT_BUS.unregister(observer);
            caster.discard();
            helper.assertTrue(shots.size() == 5, "A burst must fire exactly five shots");
            for (int i = 0; i < shots.size(); i++) {
                var shot = shots.get(i);
                helper.assertTrue(shot.tick == started + i * 3L, "Shots must spawn at ticks 0, 3, 6, 9 and 12");
                helper.assertTrue(shot.position.distanceToSqr(origin) < 0.000001, "All shots must retain the original position");
                helper.assertTrue(shot.velocity.normalize().y >= Math.cos(StardustBursts.HALF_ANGLE), "All shots must retain the original aim");
                helper.assertTrue(shot.damage == 1.6f, "All shots must retain snapshotted damage");
            }
            helper.succeed();
        });
    }

    @GameTest(template = "gametest/basic_floor", timeoutTicks = 40)
    public static void overlappingBurstsRemainIndependent(GameTestHelper helper) {
        var caster = caster(helper);
        var shots = new ArrayList<Shot>();
        var observer = observe(caster, shots);
        NeoForge.EVENT_BUS.addListener(observer);
        StardustBursts.start(helper.getLevel(), caster, 1.6f);
        StardustBursts.start(helper.getLevel(), caster, 3.4f);
        helper.runAfterDelay(16, () -> {
            NeoForge.EVENT_BUS.unregister(observer);
            caster.discard();
            helper.assertTrue(shots.size() == 10, "Overlapping casts must each produce five shots");
            helper.assertTrue(shots.stream().filter(shot -> shot.damage == 1.6f).count() == 5
                    && shots.stream().filter(shot -> shot.damage == 3.4f).count() == 5, "Overlapping casts must retain independent damage");
            helper.succeed();
        });
    }

    @GameTest(template = "gametest/basic_floor", timeoutTicks = 40)
    public static void removedCasterCancelsRemainingShots(GameTestHelper helper) {
        var caster = caster(helper);
        var shots = new ArrayList<Shot>();
        var observer = observe(caster, shots);
        NeoForge.EVENT_BUS.addListener(observer);
        StardustBursts.start(helper.getLevel(), caster, 1.6f);
        caster.discard();
        helper.runAfterDelay(16, () -> {
            NeoForge.EVENT_BUS.unregister(observer);
            helper.assertTrue(shots.size() == 1, "Removing the caster must cancel all pending shots");
            helper.succeed();
        });
    }

    @GameTest(template = "gametest/basic_floor")
    public static void damageBypassesExistingCooldownAndPostsSpellEvent(GameTestHelper helper) {
        var caster = caster(helper);
        var victim = helper.spawn(EntityType.COW, new BlockPos(2, 2, 2));
        victim.setNoAi(true);
        var spell = SpellRegistry.STARDUST.get();
        var projectile = projectile(helper, caster, victim.position().add(0, 0.5, 0), Vec3.ZERO);
        var source = spell.getDamageSource(projectile, caster);
        helper.assertTrue(source.is(DamageTypeTags.BYPASSES_COOLDOWN), "Stardust must bypass existing hurt cooldowns");
        helper.assertTrue(!SpellDamageSource.source(projectile, caster, spell).is(DamageTypeTags.BYPASSES_COOLDOWN), "Shared Zodiac damage must retain normal cooldown behavior");
        victim.hurt(helper.getLevel().damageSources().generic(), 1);
        helper.assertTrue(victim.invulnerableTime > 10, "Victim must already have hurt immunity");
        float health = victim.getHealth();
        int[] events = {0};
        Consumer<SpellDamageEvent> observer = event -> { if (event.getEntity() == victim) events[0]++; };
        NeoForge.EVENT_BUS.addListener(observer);
        try {
            for (int i = 0; i < 5; i++) helper.assertTrue(DamageSources.applyDamage(victim, 1.6f, source), "Each shot must deal full damage during existing immunity");
        } finally {
            NeoForge.EVENT_BUS.unregister(observer);
        }
        helper.assertTrue(Math.abs(health - victim.getHealth() - 8) < 0.0001, "Five shots must deal eight damage before resistances");
        helper.assertTrue(events[0] == 5, "Each shot must post a spell damage event");
        Objects.requireNonNull(victim.getAttribute(AttributeRegistry.ZODIAC_MAGIC_RESIST)).setBaseValue(2);
        float resistantHealth = victim.getHealth();
        DamageSources.applyDamage(victim, 1.6f, source);
        float reduction = resistantHealth - victim.getHealth();
        helper.assertTrue(reduction > 0 && reduction < 1.599f, "Zodiac resistance must reduce Stardust damage");
        projectile.discard();
        caster.discard();
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor", timeoutTicks = 40)
    public static void deadCasterCancelsRemainingShots(GameTestHelper helper) {
        assertCancellation(helper, caster -> caster.setHealth(0));
    }

    @GameTest(template = "gametest/basic_floor", timeoutTicks = 40)
    public static void dimensionChangeCancelsRemainingShots(GameTestHelper helper) {
        assertCancellation(helper, caster -> {
            var destination = Objects.requireNonNull(helper.getLevel().getServer().getLevel(Level.NETHER));
            var moved = caster.changeDimension(new DimensionTransition(destination, new Vec3(0, 100, 0), Vec3.ZERO,
                    0, 0, DimensionTransition.DO_NOTHING));
            helper.assertTrue(moved != null && moved.level() == destination, "Caster must move to another dimension");
            if (moved != null) moved.discard();
        });
    }

    @GameTest(template = "gametest/basic_floor")
    public static void inactiveOriginDoesNotForceLoadOrFire(GameTestHelper helper) {
        var caster = caster(helper);
        caster.setPos(20000000, 100, 20000000);
        var position = BlockPos.containing(caster.position());
        helper.assertTrue(!helper.getLevel().isPositionEntityTicking(position), "Origin must be outside active chunks");
        var shots = new ArrayList<Shot>();
        var observer = observe(caster, shots);
        NeoForge.EVENT_BUS.addListener(observer);
        try {
            StardustBursts.start(helper.getLevel(), caster, 1.6f);
        } finally {
            NeoForge.EVENT_BUS.unregister(observer);
            caster.discard();
        }
        helper.assertTrue(shots.isEmpty() && !helper.getLevel().getChunkSource().hasChunk(position.getX() >> 4, position.getZ() >> 4),
                "Inactive origin must not fire or load a chunk");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void friendlyTargetsRejectDamage(GameTestHelper helper) {
        var caster = caster(helper);
        var victim = helper.spawn(EntityType.COW, new BlockPos(2, 2, 2));
        var scoreboard = helper.getLevel().getScoreboard();
        var team = scoreboard.addPlayerTeam("stardust_test");
        team.setAllowFriendlyFire(false);
        scoreboard.addPlayerToTeam(caster.getScoreboardName(), team);
        scoreboard.addPlayerToTeam(victim.getScoreboardName(), team);
        var source = SpellRegistry.STARDUST.get().getDamageSource(caster, caster);
        float health = victim.getHealth();
        try {
            helper.assertTrue(!DamageSources.applyDamage(victim, 1.6f, source) && victim.getHealth() == health,
                    "Stardust must preserve Iron's friendly-fire protection");
        } finally {
            scoreboard.removePlayerTeam(team);
            caster.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor", timeoutTicks = 20)
    public static void fastProjectileDamagesEntityOnce(GameTestHelper helper) {
        var caster = caster(helper);
        var victim = helper.spawn(EntityType.COW, new BlockPos(1, 8, 3));
        victim.setNoAi(true);
        victim.setNoGravity(true);
        float health = victim.getHealth();
        var projectile = projectile(helper, caster, helper.absoluteVec(new Vec3(1.5, 8.5, 1.5)), new Vec3(0, 0, 1));
        projectile.setDamage(1.6f);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(projectile.isRemoved(), "Entity impact must consume the projectile");
            helper.assertTrue(Math.abs(health - victim.getHealth() - 1.6f) < 0.0001, "A fast entity impact must deal damage exactly once");
            caster.discard();
            helper.succeed();
        });
    }

    @GameTest(template = "gametest/basic_floor", timeoutTicks = 40)
    public static void projectileKeepsSpeedInWaterAndExpires(GameTestHelper helper) {
        var caster = caster(helper);
        var origin = helper.absoluteVec(new Vec3(1, 4, 1));
        helper.setBlock(new BlockPos(1, 4, 1), Blocks.WATER);
        var projectile = projectile(helper, caster, origin, new Vec3(0, 1, 0));
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(projectile.getDeltaMovement().distanceTo(new Vec3(0, StardustProjectile.SPEED, 0)) < 0.00001, "Water and gravity must not change velocity");
            helper.assertTrue(Math.abs(projectile.getY() - origin.y - projectile.tickCount * StardustProjectile.SPEED) < 0.0001, "Flight must advance at constant speed");
        });
        helper.runAfterDelay(14, () -> helper.assertTrue(!projectile.isRemoved(), "Projectile must survive before its fifteenth flight tick"));
        helper.runAfterDelay(16, () -> {
            helper.assertTrue(projectile.isRemoved() && projectile.tickCount == 15, "Projectile must expire after fifteen flight ticks");
            caster.discard();
            helper.succeed();
        });
    }

    @GameTest(template = "gametest/basic_floor", timeoutTicks = 20)
    public static void fastProjectileStopsAtWall(GameTestHelper helper) {
        var caster = caster(helper);
        var origin = helper.absoluteVec(new Vec3(1.5, 8.5, 1.5));
        helper.setBlock(new BlockPos(1, 8, 2), Blocks.STONE);
        var victim = helper.spawn(EntityType.COW, new BlockPos(1, 8, 3));
        victim.setNoAi(true);
        victim.setNoGravity(true);
        float health = victim.getHealth();
        var projectile = projectile(helper, caster, origin, new Vec3(0, 0, 1));
        projectile.setDamage(1.6f);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(projectile.isRemoved(), "Fast projectile must collide with the intervening wall");
            helper.assertTrue(victim.getHealth() == health, "Projectile must not damage an entity behind a wall");
            caster.discard();
            helper.succeed();
        });
    }

    private static LivingEntity caster(GameTestHelper helper) {
        var caster = helper.spawn(EntityType.ARMOR_STAND, new BlockPos(1, 5, 1));
        caster.setNoGravity(true);
        caster.setXRot(-90);
        return caster;
    }

    private static void assertCancellation(GameTestHelper helper, Consumer<LivingEntity> cancel) {
        var caster = caster(helper);
        var shots = new ArrayList<Shot>();
        var observer = observe(caster, shots);
        NeoForge.EVENT_BUS.addListener(observer);
        StardustBursts.start(helper.getLevel(), caster, 1.6f);
        try {
            cancel.accept(caster);
        } catch (RuntimeException exception) {
            NeoForge.EVENT_BUS.unregister(observer);
            throw exception;
        }
        helper.runAfterDelay(16, () -> {
            NeoForge.EVENT_BUS.unregister(observer);
            caster.discard();
            helper.assertTrue(shots.size() == 1, "Invalidating the caster must cancel all pending shots");
            helper.succeed();
        });
    }

    private static StardustProjectile projectile(GameTestHelper helper, LivingEntity caster, Vec3 position, Vec3 direction) {
        var projectile = new StardustProjectile(EntityRegistry.STARDUST.get(), helper.getLevel());
        projectile.setOwner(caster);
        projectile.setPos(position);
        projectile.shoot(direction);
        helper.getLevel().addFreshEntity(projectile);
        return projectile;
    }

    private static Consumer<EntityJoinLevelEvent> observe(LivingEntity caster, List<Shot> shots) {
        return event -> {
            if (event.getEntity() instanceof StardustProjectile projectile && projectile.getOwner() == caster) {
                shots.add(new Shot(event.getLevel().getGameTime(), projectile.position(), projectile.getDeltaMovement(), projectile.getDamage()));
            }
        };
    }

    private record Shot(long tick, Vec3 position, Vec3 velocity, float damage) {}
}
