package jp.aquafactory.constellationandspells.gametest;

import com.mojang.authlib.GameProfile;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.EffectRegistry;
import jp.aquafactory.constellationandspells.registry.SpellRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Objects;
import java.util.UUID;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
public final class VirgoHealGameTests {
    private VirgoHealGameTests() {}

    @GameTest(template = "gametest/basic_floor")
    public static void pulseFollowsTheCasterAndUsesCylindricalHitboxes(GameTestHelper helper) {
        var caster = spawn(helper, EntityType.COW, 1.5, 10, 1.5);
        var behindWall = spawn(helper, EntityType.COW, 1.5, 10, 3.5);
        var edge = spawn(helper, EntityType.COW, 4.7, 10, 1.5);
        var corner = spawn(helper, EntityType.COW, 4.4, 10, 4.4);
        var upperInside = spawn(helper, EntityType.COW, 1.5, 12.9, 2.5);
        var upperOutside = spawn(helper, EntityType.COW, 1.5, 13.1, 2.5);
        var lowerInside = spawn(helper, EntityType.COW, 2.5, 8, 1.5);
        var lowerOutside = spawn(helper, EntityType.COW, 2.5, 7.4, 1.5);
        helper.setBlock(new BlockPos(1, 10, 2), Blocks.STONE);
        helper.setBlock(new BlockPos(1, 11, 2), Blocks.STONE);

        var spell = SpellRegistry.VIRGO_HEAL.get();
        var magic = MagicData.getPlayerMagicData(caster);
        spell.onCast(helper.getLevel(), 1, caster, CastSource.MOB, magic);
        helper.assertTrue(caster.getHealth() == 8.5f, "The caster must be included in its own pulse");
        helper.assertTrue(behindWall.getHealth() == 8.5f, "A wall must not block healing");
        helper.assertTrue(edge.getHealth() == 8.5f && corner.getHealth() == 8.0f,
                "The XZ circle must include intersecting hitboxes and reject AABB corners");
        helper.assertTrue(upperInside.getHealth() == 8.5f && upperOutside.getHealth() == 8.0f,
                "The top of the cylinder must limit airborne targets");
        helper.assertTrue(lowerInside.getHealth() == 8.5f && lowerOutside.getHealth() == 8.0f,
                "The bottom of the cylinder must use overlapping hitboxes");

        caster.setPos(upperOutside.position());
        spell.onCast(helper.getLevel(), 1, caster, CastSource.MOB, magic);
        helper.assertTrue(upperOutside.getHealth() == 8.5f && lowerInside.getHealth() == 8.5f,
                "Each pulse must use the caster's current position");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void pulseDamagesInvertedTargetsThroughArmorAndHurtCooldown(GameTestHelper helper) {
        var caster = spawn(helper, EntityType.COW, 2.5, 10, 2.5);
        var zombie = spawn(helper, EntityRegistry.SUMMONED_ZOMBIE.get(), 2.5, 10, 4.5);
        var skeleton = spawn(helper, EntityRegistry.SUMMONED_SKELETON.get(), 4.5, 10, 2.5);
        var necromancer = spawn(helper, EntityRegistry.NECROMANCER.get(), 0.5, 10, 2.5);
        var protectedZombie = spawn(helper, EntityRegistry.SUMMONED_ZOMBIE.get(), 2.5, 10, 0.5);
        var protectedSelf = spawn(helper, EntityType.ZOMBIE, 2.5, 10, 1.5);

        var generic = helper.getLevel().damageSources().generic();
        helper.assertTrue(zombie.hurt(generic, 3.0f), "The first hit must create hurt cooldown");
        zombie.setHealth(8.0f);
        zombie.invulnerableTime = 12;
        Objects.requireNonNull(zombie.getAttribute(Attributes.ARMOR)).setBaseValue(20.0);
        zombie.setDeltaMovement(Vec3.ZERO);

        var scoreboard = helper.getLevel().getScoreboard();
        var team = scoreboard.addPlayerTeam("virgo_heal_test");
        team.setAllowFriendlyFire(false);
        scoreboard.addPlayerToTeam(caster.getScoreboardName(), team);
        scoreboard.addPlayerToTeam(protectedZombie.getScoreboardName(), team);
        try {
            var spell = SpellRegistry.VIRGO_HEAL.get();
            spell.onCast(helper.getLevel(), 1, caster, CastSource.MOB, MagicData.getPlayerMagicData(caster));
            helper.assertTrue(zombie.getHealth() == 7.5f,
                    "The pulse must deal full damage through armor and existing hurt cooldown");
            helper.assertTrue(zombie.getDeltaMovement().equals(Vec3.ZERO), "The pulse must not knock targets back");
            helper.assertTrue(zombie.invulnerableTime == zombie.invulnerableDuration
                            && !zombie.hurt(generic, 0.25f) && zombie.getHealth() == 7.5f,
                    "The pulse must create normal hurt cooldown and reject an immediate weaker hit");
            helper.assertTrue(skeleton.getHealth() == 7.5f && necromancer.getHealth() == 7.5f,
                    "Skeleton summons and necromancers must also be damaged");
            helper.assertTrue(protectedZombie.getHealth() == 8.0f, "A protected undead ally must neither heal nor take damage");

            protectedSelf.setHealth(8.0f);
            spell.onCast(helper.getLevel(), 1, protectedSelf, CastSource.MOB, MagicData.getPlayerMagicData(protectedSelf));
            helper.assertTrue(protectedSelf.getHealth() == 8.0f, "An inverted caster must not heal itself");
        } finally {
            scoreboard.removePlayerTeam(team);
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void castingMobilityFillsOnlyTheMissingSpeedAndClearsOnCompletion(GameTestHelper helper) {
        var caster = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "virgo_heal_test"));
        caster.setPos(helper.absoluteVec(new Vec3(4.5, 10, 4.5)));
        var magic = MagicData.getPlayerMagicData(caster);
        magic.setServerPlayer(caster);
        magic.resetCastingState();
        var spell = SpellRegistry.VIRGO_HEAL.get();

        var speed = Objects.requireNonNull(caster.getAttribute(AttributeRegistry.CASTING_MOVESPEED));
        var external = ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "virgo_heal_test");
        speed.addTransientModifier(new AttributeModifier(external, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        for (boolean cancelled : new boolean[]{true, false}) {
            magic.initiateCast(spell, 1, 100, CastSource.SPELLBOOK, "mainhand");
            spell.onServerPreCast(helper.getLevel(), 1, caster, magic);
            helper.assertTrue(caster.hasEffect(EffectRegistry.VIRGO_HEAL_MOBILITY)
                            && Math.abs(speed.getValue() - 1.8) < 1.0e-6,
                    "Virgo Heal must compensate for the remaining slowdown at cast start");
            speed.addOrUpdateTransientModifier(
                    new AttributeModifier(external, 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            spell.onServerCastTick(helper.getLevel(), 1, caster, magic);
            helper.assertTrue(speed.getValue() == 2.0, "The mobility effect must preserve higher external speed");
            speed.addOrUpdateTransientModifier(
                    new AttributeModifier(external, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            spell.onServerCastTick(helper.getLevel(), 1, caster, magic);
            spell.onServerCastComplete(helper.getLevel(), 1, caster, magic, cancelled);
            helper.assertTrue(!caster.hasEffect(EffectRegistry.VIRGO_HEAL_MOBILITY) && speed.getValue() == 1.25,
                    "Completion and cancellation must immediately remove only the Virgo Heal modifier");
        }
        helper.succeed();
    }

    private static <T extends Mob> T spawn(GameTestHelper helper, EntityType<T> type, double x, double y, double z) {
        var target = helper.spawn(type, BlockPos.containing(x, y, z));
        target.setPos(helper.absoluteVec(new Vec3(x, y, z)));
        target.setNoAi(true);
        target.setNoGravity(true);
        target.setHealth(8.0f);
        return target;
    }
}
