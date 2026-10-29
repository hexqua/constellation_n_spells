package jp.aquafactory.constellationandspells.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.config.ServerConfig;
import jp.aquafactory.constellationandspells.magic.SchoolPowerSharing;
import jp.aquafactory.constellationandspells.magic.SchoolPowerSharingRules;
import jp.aquafactory.constellationandspells.magic.SchoolPowerSharingState;
import jp.aquafactory.constellationandspells.mixin.SchoolTypeAccessor;
import jp.aquafactory.constellationandspells.network.SyncSchoolPowerSharingPayload;
import jp.aquafactory.constellationandspells.registry.ZodiacSchoolRegistry;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static jp.aquafactory.constellationandspells.registry.AttributeRegistry.ZODIAC_SPELL_POWER;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
// GameTest がアノテーションから探索して呼び出す。
@SuppressWarnings("unused")
public class SchoolPowerSharingGameTests {
    @GameTest(template = "gametest/basic_floor")
    public static void targetsUseRegistryIdsAndExclusions(GameTestHelper helper) {
        var first = id("first:astral");
        var second = id("second:astral");
        var prefix = id("first:some_astral");
        var suffix = id("first:astral_some");
        var fire = SchoolRegistry.FIRE.get();
        var schools = Map.of(first, fixture(first, AttributeRegistry.FIRE_SPELL_POWER),
                second, fixture(second, AttributeRegistry.ICE_SPELL_POWER),
                prefix, fixture(prefix, AttributeRegistry.FIRE_SPELL_POWER),
                suffix, fixture(suffix, AttributeRegistry.FIRE_SPELL_POWER),
                fire.getId(), fire, SchoolPowerSharingRules.ZODIAC_ID, ZodiacSchoolRegistry.ZODIAC.get());
        var defaults = SchoolPowerSharingRules.DEFAULT.resolveTargets(schools);
        helper.assertTrue(defaults.size() == 2 && defaults.contains(schools.get(first))
                        && defaults.contains(schools.get(second)),
                "Only exact astral paths must be detected across namespaces");
        var rules = new SchoolPowerSharingRules(true, true,
                Set.of(first, fire.getId(), id("missing:school"), SchoolPowerSharingRules.ZODIAC_ID), Set.of(first));
        var targets = rules.resolveTargets(schools);
        helper.assertTrue(targets.size() == 2 && targets.contains(fire) && targets.contains(schools.get(second)),
                "Exclusions must override inclusions; missing schools and Zodiac itself must be ignored");
        var manual = new SchoolPowerSharingRules(true, false, Set.of(fire.getId()), Set.of());
        helper.assertTrue(manual.resolveTargets(schools).equals(List.of(fire)),
                "Manual IDs must work without automatic detection");
        helper.assertTrue(new SchoolPowerSharingRules(false, true, rules.additionalSchools(), rules.excludedSchools())
                        .resolveTargets(schools).equals(targets),
                "Disabling this direction must not change the reusable target set");
        helper.assertTrue(ServerConfig.isSchoolId("irons_spellbooks:eldritch")
                        && !ServerConfig.isSchoolId("astral") && !ServerConfig.isSchoolId(":astral")
                        && !ServerConfig.isSchoolId("first:") && !ServerConfig.isSchoolId("First:astral"),
                "Config IDs must be valid and explicitly namespaced");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void sharingUsesRawLiveValuesForPlayersOnly(GameTestHelper helper) {
        var player = player(helper, "SharingRaw");
        var zodiac = ZodiacSchoolRegistry.ZODIAC.get();
        var fire = fixture(id("first:astral"), AttributeRegistry.FIRE_SPELL_POWER);
        var ice = fixture(id("second:astral"), AttributeRegistry.ICE_SPELL_POWER);
        var overridden = fixtureWithOverriddenPower();
        var state = new SchoolPowerSharingState(SchoolPowerSharingRules.DEFAULT, List.of(fire, ice, overridden));
        setPower(player, ZODIAC_SPELL_POWER, 1.3);
        setPower(player, AttributeRegistry.FIRE_SPELL_POWER, 1.8);
        setPower(player, AttributeRegistry.ICE_SPELL_POWER, 1.5);
        assertPower(helper, state.zodiacPowerFor(player), 1.8, "The highest raw candidate must win");
        assertPower(helper, SchoolPowerSharingState.rawPowerFor(zodiac, player).orElseThrow(), 1.3,
                "The raw Zodiac attribute must remain unchanged for future reverse sharing");
        setPower(player, ZODIAC_SPELL_POWER, 2.0);
        assertPower(helper, state.zodiacPowerFor(player), 2.0, "Zodiac must win when stronger");
        setPower(player, AttributeRegistry.ICE_SPELL_POWER, 2.0);
        assertPower(helper, state.zodiacPowerFor(player), 2.0, "Equal values must not add together");
        setPower(player, ZODIAC_SPELL_POWER, 0.4);
        setPower(player, AttributeRegistry.FIRE_SPELL_POWER, 0.6);
        setPower(player, AttributeRegistry.ICE_SPELL_POWER, 0.7);
        assertPower(helper, state.zodiacPowerFor(player), 0.7, "Weakness must not be floored at one");
        var absent = fixture(id("missing:astral"), Holder.direct(new RangedAttribute("missing", 1, 0, 100)));
        helper.assertTrue(SchoolPowerSharingState.rawPowerFor(absent, player).isEmpty(),
                "Missing entity attributes must not contribute a default candidate");
        var missingState = new SchoolPowerSharingState(SchoolPowerSharingRules.DEFAULT, List.of(absent));
        assertPower(helper, missingState.zodiacPowerFor(player), 0.4, "Missing candidates must preserve Zodiac weakness");
        var disabled = new SchoolPowerSharingState(
                new SchoolPowerSharingRules(false, true, Set.of(), Set.of()), state.targets());
        assertPower(helper, disabled.zodiacPowerFor(player), 0.4, "Disabled sharing must preserve raw Zodiac");
        assertPower(helper, new SchoolPowerSharingState(SchoolPowerSharingRules.DEFAULT, List.of())
                .zodiacPowerFor(player), 0.4, "An empty target set must preserve raw Zodiac");
        var zombie = Objects.requireNonNull(EntityType.ZOMBIE.create(helper.getLevel()));
        setPower(zombie, ZODIAC_SPELL_POWER, 1.1);
        setPower(zombie, AttributeRegistry.FIRE_SPELL_POWER, 3);
        assertPower(helper, state.zodiacPowerFor(zombie), 1.1, "Mobs must not receive power sharing");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void registeredZodiacUsesLiveStateAndOptionalAstral(GameTestHelper helper) {
        var player = player(helper, "SharingLive");
        var zodiac = ZodiacSchoolRegistry.ZODIAC.get();
        setPower(player, ZODIAC_SPELL_POWER, 1.4);
        helper.assertTrue(SchoolPowerSharing.serverState().rules().equals(ServerConfig.snapshot()),
                "Server startup must publish the loaded config before spells are evaluated");
        assertPower(helper, zodiac.getPowerFor(player), SchoolPowerSharing.serverState().zodiacPowerFor(player),
                "The registered Zodiac school must use the active server snapshot");
        var astral = SchoolRegistry.getSchool(id("asterismarcanum:astral"));
        if (ModList.get().isLoaded("asterismarcanum")) {
            helper.assertTrue(astral != null, "Asterism must register its Astral school in the compat profile");
            var attribute = ((SchoolTypeAccessor) Objects.requireNonNull(astral))
                    .constellationandspells$getPowerAttribute();
            var resolved = SchoolPowerSharingRules.DEFAULT.resolveTargets(Map.of(astral.getId(), astral));
            helper.assertTrue(resolved.equals(List.of(astral)), "Asterism Astral must be automatically detected");
            var state = new SchoolPowerSharingState(SchoolPowerSharingRules.DEFAULT, resolved);
            setPower(player, attribute, 2.3);
            assertPower(helper, state.zodiacPowerFor(player), 2.3, "Asterism raw power must be reusable");
            assertPower(helper, zodiac.getPowerFor(player), SchoolPowerSharing.serverState().zodiacPowerFor(player),
                    "The registered school must observe changed Asterism power through active rules");
            setPower(player, attribute, 1.1);
            assertPower(helper, state.zodiacPowerFor(player), 1.4, "Removing Astral bonuses must be reflected immediately");
        } else {
            helper.assertTrue(astral == null, "The base profile must not require Asterism");
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void payloadAndClientLifecyclePreserveServerState(GameTestHelper helper) {
        var rules = new SchoolPowerSharingRules(true, false, Set.of(id("irons_spellbooks:fire")),
                Set.of(id("asterismarcanum:astral")));
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        SchoolPowerSharingRules decoded;
        try {
            SyncSchoolPowerSharingPayload.STREAM_CODEC.encode(buffer, new SyncSchoolPowerSharingPayload(rules));
            decoded = SyncSchoolPowerSharingPayload.STREAM_CODEC.decode(buffer).rules();
            helper.assertTrue(decoded.equals(rules) && !buffer.isReadable(), "All policy fields must round-trip");
        } finally {
            buffer.release();
        }
        var serverBefore = SchoolPowerSharing.serverState();
        var clientBefore = SchoolPowerSharing.clientState().rules();
        try {
            SchoolPowerSharing.updateClient(decoded);
            helper.assertTrue(SchoolPowerSharing.clientState().targets().equals(List.of(SchoolRegistry.FIRE.get())),
                    "Client snapshots must resolve the received target policy");
            SchoolPowerSharing.updateClient(new SchoolPowerSharingRules(false, false,
                    rules.additionalSchools(), rules.excludedSchools()));
            helper.assertTrue(!SchoolPowerSharing.clientState().rules().enabled()
                            && SchoolPowerSharing.clientState().targets().contains(SchoolRegistry.FIRE.get()),
                    "Client reloads must update enablement without losing reusable targets");
            SchoolPowerSharing.resetClient();
            helper.assertTrue(SchoolPowerSharing.clientState() == SchoolPowerSharingState.INACTIVE,
                    "Disconnect must discard the previous server's client snapshot");
            helper.assertTrue(SchoolPowerSharing.serverState() == serverBefore,
                    "Client updates and disconnects must not mutate integrated server state");
        } finally {
            SchoolPowerSharing.updateClient(clientBefore);
        }
        helper.succeed();
    }

    private static SchoolType fixture(ResourceLocation id, Holder<Attribute> attribute) {
        var fire = SchoolRegistry.FIRE.get();
        return new SchoolType(id, fire.getFocus(), Component.literal("Astral"), attribute,
                AttributeRegistry.FIRE_MAGIC_RESIST, SoundRegistry.FIRE_CAST, fire.getDamageType());
    }

    private static SchoolType fixtureWithOverriddenPower() {
        var zodiac = ZodiacSchoolRegistry.ZODIAC.get();
        // 独自 getPowerFor の結果を借りると、将来の逆転送を経由した値を拾ってしまう。
        return new SchoolType(id("third:astral"), zodiac.getFocus(), Component.literal("Fixture"),
                AttributeRegistry.FIRE_SPELL_POWER, AttributeRegistry.FIRE_MAGIC_RESIST,
                SoundRegistry.EVOCATION_CAST, zodiac.getDamageType()) {
            @Override
            public double getPowerFor(LivingEntity entity) {
                return 99;
            }
        };
    }

    private static Player player(GameTestHelper helper, String name) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(
                UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)), name));
    }

    private static void setPower(LivingEntity entity, Holder<Attribute> attribute, double value) {
        Objects.requireNonNull(entity.getAttribute(attribute)).setBaseValue(value);
    }

    private static ResourceLocation id(String value) {
        return ResourceLocation.parse(value);
    }

    private static void assertPower(GameTestHelper helper, double actual, double expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) < 0.000001, message + ": expected=" + expected + ", actual=" + actual);
    }
}
