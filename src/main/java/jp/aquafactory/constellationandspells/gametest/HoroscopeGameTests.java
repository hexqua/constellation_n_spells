package jp.aquafactory.constellationandspells.gametest;

import com.mojang.authlib.GameProfile;
import io.redspace.ironsspellbooks.api.events.ChangeManaEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.SpellRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
@SuppressWarnings("unused") // GameTest がアノテーションから探索する。
public final class HoroscopeGameTests {
    private HoroscopeGameTests() {}

    @GameTest(template = "gametest/basic_floor")
    public static void firstLevelRecoveryIgnoresSpellPower(GameTestHelper helper) {
        var player = player(helper, "horoscope_lv1");
        var data = MagicData.getPlayerMagicData(player);
        cast(helper, player, 1);
        assertMana(helper, data, 130, "Level one must restore thirty mana");
        Objects.requireNonNull(player.getAttribute(AttributeRegistry.SPELL_POWER)).setBaseValue(2);
        cast(helper, player, 1);
        assertMana(helper, data, 160, "Level one recovery must remain fixed with increased spell power");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void higherLevelRecoveryScalesOnlySpellPowerPortion(GameTestHelper helper) {
        var player = player(helper, "horoscope_power");
        var data = MagicData.getPlayerMagicData(player);
        cast(helper, player, 2);
        assertMana(helper, data, 160, "Level two must restore sixty mana without bonuses");
        Objects.requireNonNull(player.getAttribute(AttributeRegistry.SPELL_POWER)).setBaseValue(2);
        cast(helper, player, 2);
        assertMana(helper, data, 250, "Double spell power must amplify only the variable thirty mana");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void recoveryClampsToMaximumMana(GameTestHelper helper) {
        var player = player(helper, "horoscope_cap");
        var data = MagicData.getPlayerMagicData(player);
        data.setMana(990);
        cast(helper, player, 1);
        assertMana(helper, data, 1000, "Recovery must stop at maximum mana");
        cast(helper, player, 1);
        assertMana(helper, data, 1000, "Casting at maximum mana must not exceed the cap");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void castingAndCancellationDoNotRecoverMana(GameTestHelper helper) {
        var player = player(helper, "horoscope_cancel");
        var data = MagicData.getPlayerMagicData(player);
        var spell = SpellRegistry.HOROSCOPE.get();
        data.initiateCast(spell, 1, spell.getEffectiveCastTime(1, player), CastSource.SPELLBOOK, "mainhand");
        spell.onServerPreCast(helper.getLevel(), 1, player, data);
        // 直接コールバックを通し、中断完了を回復処理として誤用する回帰を検出する。
        for (int i = 0; i < 10; i++) {
            data.handleCastDuration();
            spell.onServerCastTick(helper.getLevel(), 1, player, data);
        }
        assertMana(helper, data, 100, "Casting ticks must not restore mana");
        spell.onServerCastComplete(helper.getLevel(), 1, player, data, true);
        assertMana(helper, data, 100, "Cancelled casting must not restore mana");
        helper.assertTrue(!data.isCasting(), "Cancellation must clear casting state");
        cast(helper, player, 1);
        assertMana(helper, data, 130, "A subsequent successful cast must restore mana");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void recoveryRespectsManaChangeEvents(GameTestHelper helper) {
        var player = player(helper, "horoscope_event");
        var data = MagicData.getPlayerMagicData(player);
        int[] events = {0};
        Consumer<ChangeManaEvent> observer = event -> {
            if (event.getMagicData() != data) return;
            if (++events[0] == 1) {
                event.setNewMana(event.getOldMana() + 7);
            } else {
                event.setCanceled(true);
            }
        };
        NeoForge.EVENT_BUS.addListener(observer);
        try {
            cast(helper, player, 1);
            assertMana(helper, data, 107, "Recovery must use the event's adjusted mana");
            cast(helper, player, 1);
            assertMana(helper, data, 107, "Cancelled mana changes must not restore mana");
            helper.assertTrue(events[0] == 2, "Each recovery must post one mana change event");
        } finally {
            NeoForge.EVENT_BUS.unregister(observer);
        }
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper, String name) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(
                UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)), name));
        player.setPos(helper.absolutePos(new BlockPos(2, 2, 2)).getCenter());
        Objects.requireNonNull(player.getAttribute(AttributeRegistry.MAX_MANA)).setBaseValue(1000);
        Objects.requireNonNull(player.getAttribute(AttributeRegistry.SPELL_POWER)).setBaseValue(1);
        Objects.requireNonNull(player.getAttribute(
                jp.aquafactory.constellationandspells.registry.AttributeRegistry.ZODIAC_SPELL_POWER)).setBaseValue(1);
        var data = MagicData.getPlayerMagicData(player);
        data.setServerPlayer(player);
        data.resetCastingState();
        data.setMana(100);
        return player;
    }

    private static void cast(GameTestHelper helper, ServerPlayer player, int level) {
        // 発動コールバックの検証であり、クライアント入力や実通信の保証は含まない。
        SpellRegistry.HOROSCOPE.get().onCast(helper.getLevel(), level, player, CastSource.SPELLBOOK,
                MagicData.getPlayerMagicData(player));
    }

    private static void assertMana(GameTestHelper helper, MagicData data, float expected, String message) {
        helper.assertTrue(Math.abs(data.getMana() - expected) < 0.0001f,
                message + ": expected=" + expected + ", actual=" + data.getMana());
    }
}
