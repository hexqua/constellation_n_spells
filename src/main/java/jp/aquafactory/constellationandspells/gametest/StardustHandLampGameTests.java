package jp.aquafactory.constellationandspells.gametest;

import com.mojang.authlib.GameProfile;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.util.Utils;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import jp.aquafactory.constellationandspells.registry.SpellRegistry;
import jp.aquafactory.constellationandspells.spell.stardust.StardustProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MAX_MANA;
import static jp.aquafactory.constellationandspells.item.stardusthandlamp.StardustHandLampCasting.SELECTION_SLOT;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
@SuppressWarnings("unused") // GameTestがアノテーションから探索する。
public final class StardustHandLampGameTests {
    private StardustHandLampGameTests() {}

    @GameTest(template = "gametest/basic_floor")
    public static void selectionCoexistsWithImbueAndTracksEquipment(GameTestHelper helper) {
        var player = player(helper, "lamp_selection");
        var sword = new ItemStack(Items.DIAMOND_SWORD);
        ISpellContainer.createImbuedContainer(SpellRegistry.STARDUST.get(), 4, sword);
        player.setItemSlot(EquipmentSlot.MAINHAND, sword);
        var lamp = lamp();
        player.setItemSlot(EquipmentSlot.OFFHAND, lamp);
        var manager = new SpellSelectionManager(player);
        helper.assertTrue(manager.getAllSpells().stream().filter(option -> option.spellData.getSpell() == SpellRegistry.STARDUST.get()).count() == 2,
                "Lamp and imbued Stardust must remain separate selections");
        helper.assertTrue(manager.getSpellForSlot(SELECTION_SLOT, 0).getLevel() == 1 && !ISpellContainer.isSpellContainer(lamp),
                "The lamp must supply a base-level spell without storing a spell container");
        player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        helper.assertTrue(new SpellSelectionManager(player).getSpellsForSlot(SELECTION_SLOT).isEmpty(),
                "Removing the lamp must remove only its selection");
        player.setItemSlot(EquipmentSlot.MAINHAND, lamp);
        helper.assertTrue(new SpellSelectionManager(player).getSpellsForSlot(SELECTION_SLOT).size() == 1,
                "Moving the lamp to the main hand must restore its selection");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void wheelAndQuickCastConsumeOnlyTheOffhandLamp(GameTestHelper helper) {
        var player = player(helper, "lamp_wheel");
        var main = lamp();
        var off = lamp();
        player.setItemSlot(EquipmentSlot.MAINHAND, main);
        player.setItemSlot(EquipmentSlot.OFFHAND, off);
        var manager = new SpellSelectionManager(player);
        helper.assertTrue(manager.getSpellsForSlot(SELECTION_SLOT).size() == 1, "Two lamps must provide only one selection");
        select(player, SELECTION_SLOT);
        helper.assertTrue(Utils.serverSideInitiateCast(player), "The wheel must start the lamp spell");
        finishCast(helper, player, true);
        helper.assertTrue(main.getDamageValue() == 0 && off.getDamageValue() == 1,
                "Only the offhand lamp must lose one durability per cast, rather than per projectile");
        ready(player);
        helper.assertTrue(Utils.serverSideInitiateQuickCast(player, new SpellSelectionManager(player).getSpellsForSlot(SELECTION_SLOT).getFirst().globalIndex),
                "Quick cast must also start the lamp spell");
        finishCast(helper, player, true);
        helper.assertTrue(main.getDamageValue() == 0 && off.getDamageValue() == 2, "Quick cast must charge the same offhand lamp");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void staffUsesSelectedSourceWithoutDirectOffhandFallback(GameTestHelper helper) {
        var player = player(helper, "lamp_staff");
        var staff = new ItemStack(ItemRegistry.CELESTIAL_SURVEY_STAFF.get());
        var lamp = lamp();
        ISpellContainer.createImbuedContainer(SpellRegistry.STARDUST.get(), 3, staff);
        player.setItemSlot(EquipmentSlot.MAINHAND, staff);
        player.setItemSlot(EquipmentSlot.OFFHAND, lamp);
        select(player, SELECTION_SLOT);
        var event = NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickItem(player, InteractionHand.MAIN_HAND));
        helper.assertTrue(event.isCanceled() && MagicData.getPlayerMagicData(player).isCasting(),
                "Iron's right-click event must start the selected lamp spell through the staff");
        finishCast(helper, player, true);
        helper.assertTrue(lamp.getDamageValue() == 1 && staff.getDamageValue() == 0, "Staff casting must charge the lamp, not the staff");
        ready(player);
        var result = lamp.use(helper.getLevel(), player, InteractionHand.OFF_HAND);
        helper.assertTrue(result.getResult() == InteractionResult.PASS && !MagicData.getPlayerMagicData(player).isCasting(),
                "Offhand direct use must not fall through from a mainhand casting implement");
        select(player, SpellSelectionManager.MAINHAND);
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickItem(player, InteractionHand.MAIN_HAND));
        finishCast(helper, player, true);
        helper.assertTrue(lamp.getDamageValue() == 1, "The staff's own imbued Stardust must not consume lamp durability");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void directUseIgnoresSelectionAndReceivesAffinity(GameTestHelper helper) {
        var player = player(helper, "lamp_direct");
        var lamp = lamp();
        var sword = new ItemStack(Items.DIAMOND_SWORD);
        ISpellContainer.createImbuedContainer(SpellRegistry.HOROSCOPE.get(), 1, sword);
        player.setItemSlot(EquipmentSlot.MAINHAND, sword);
        player.setItemSlot(EquipmentSlot.OFFHAND, lamp);
        select(player, SpellSelectionManager.MAINHAND);
        var ring = new ItemStack(io.redspace.ironsspellbooks.registries.ItemRegistry.AFFINITY_RING.get());
        AffinityData.setAffinityData(ring, SpellRegistry.STARDUST.get(), 2);
        CuriosApi.getCuriosInventory(player).orElseThrow().getStacksHandler("ring").orElseThrow().getStacks().setStackInSlot(0, ring);
        helper.assertTrue(lamp.use(helper.getLevel(), player, InteractionHand.OFF_HAND).getResult().consumesAction(),
                "Direct lamp use must start independently of the selected spell");
        var data = MagicData.getPlayerMagicData(player);
        helper.assertTrue(data.getCastingSpellId().equals(SpellRegistry.STARDUST.get().getSpellId()) && data.getCastingSpellLevel() == 3,
                "Direct use must cast base-level one Stardust plus the equipped affinity bonus");
        finishCast(helper, player, true);
        helper.assertTrue(lamp.getDamageValue() == 1, "Direct casting must consume one durability");
        ready(player);
        select(player, SELECTION_SLOT);
        helper.assertTrue(Utils.serverSideInitiateCast(player) && data.getCastingSpellLevel() == 3,
                "Wheel casting must apply the same affinity bonus");
        finishCast(helper, player, true);
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void rejectedCancelledAndReplacedCastsDoNotConsumeLamp(GameTestHelper helper) {
        var player = player(helper, "lamp_rejected");
        var lamp = lamp();
        player.setItemSlot(EquipmentSlot.MAINHAND, lamp);
        select(player, SELECTION_SLOT);
        var spell = SpellRegistry.STARDUST.get();
        var data = MagicData.getPlayerMagicData(player);
        data.getPlayerCooldowns().addCooldown(spell, 100);
        helper.assertTrue(!Utils.serverSideInitiateCast(player) && lamp.getDamageValue() == 0,
                "A cooldown-rejected cast must not damage the lamp");
        ready(player);
        Consumer<SpellPreCastEvent> reject = event -> { if (event.getEntity() == player) event.setCanceled(true); };
        NeoForge.EVENT_BUS.addListener(reject);
        try {
            helper.assertTrue(!Utils.serverSideInitiateCast(player), "A cancelled pre-cast event must reject lamp casting");
        } finally {
            NeoForge.EVENT_BUS.unregister(reject);
        }
        helper.assertTrue(lamp.getDamageValue() == 0, "Pre-cast cancellation must preserve durability");
        helper.assertTrue(Utils.serverSideInitiateCast(player), "Casting must resume after rejection is removed");
        spell.onServerCastComplete(helper.getLevel(), 1, player, data, true);
        helper.assertTrue(lamp.getDamageValue() == 0, "Cancelling an initiated cast must preserve durability");
        helper.assertTrue(Utils.serverSideInitiateCast(player), "A fresh cast must start after cancellation");
        var replacement = lamp();
        player.setItemSlot(EquipmentSlot.MAINHAND, replacement);
        float mana = data.getMana();
        finishCast(helper, player, false);
        helper.assertTrue(lamp.getDamageValue() == 0 && replacement.getDamageValue() == 0 && data.getMana() == mana,
                "Replacing the casting lamp must reject the effect without charging either item or mana");
        player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        helper.assertTrue(!spell.attemptInitiateCast(ItemStack.EMPTY, 1, helper.getLevel(), player, CastSource.SWORD, true, SELECTION_SLOT),
                "A stale lamp selection must not cast without a held lamp");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void lastDurabilityBreaksLampAndRemovesSelection(GameTestHelper helper) {
        var player = player(helper, "lamp_break");
        var lamp = lamp();
        lamp.setDamageValue(lamp.getMaxDamage() - 1);
        player.setItemSlot(EquipmentSlot.MAINHAND, lamp);
        select(player, SELECTION_SLOT);
        helper.assertTrue(Utils.serverSideInitiateCast(player), "The final durability point must allow a cast");
        finishCast(helper, player, true);
        helper.assertTrue(lamp.isEmpty() && new SpellSelectionManager(player).getSpellsForSlot(SELECTION_SLOT).isEmpty(),
                "A broken lamp must disappear and stop providing a spell selection");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void physicalOffhandStillWorksWhenLogicalOffhandIsHidden(GameTestHelper helper) {
        // BetterCombatと同じ論理/物理の分離を再現し、optional MOD不在でも境界を検証する。
        var player = new HiddenOffhandPlayer(helper.getLevel(), profile("lamp_hidden"));
        prepare(helper, player);
        var main = lamp();
        var off = lamp();
        player.setItemSlot(EquipmentSlot.MAINHAND, main);
        player.getInventory().offhand.set(0, off);
        helper.assertTrue(player.getOffhandItem().isEmpty() && new SpellSelectionManager(player).getSpellsForSlot(SELECTION_SLOT).size() == 1,
                "A hidden logical offhand must not remove the physical lamp selection");
        select(player, SELECTION_SLOT);
        helper.assertTrue(Utils.serverSideInitiateCast(player), "The hidden offhand lamp must remain castable");
        finishCast(helper, player, true);
        helper.assertTrue(main.getDamageValue() == 0 && off.getDamageValue() == 1, "The physical offhand lamp must pay the durability cost");
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void repairsKeepIntrinsicSpellAndRespectEnchantments(GameTestHelper helper) {
        var player = player(helper, "lamp_repair");
        var enchantments = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        var first = lamp();
        first.setDamageValue(45);
        var second = first.copy();
        var anvil = new AnvilMenu(0, player.getInventory(), ContainerLevelAccess.NULL);
        anvil.getSlot(0).set(first);
        anvil.getSlot(1).set(new ItemStack(Items.AMETHYST_SHARD));
        helper.assertTrue(anvil.getSlot(2).getItem().isEmpty(), "An anvil must reject material-based lamp repair");
        anvil.getSlot(1).set(second);
        var anvilResult = anvil.getSlot(2).getItem().copy();
        assertRepaired(helper, player, anvilResult, first);
        for (var key : List.of(Enchantments.UNBREAKING, Enchantments.MENDING, Enchantments.VANISHING_CURSE, Enchantments.SHARPNESS)) {
            var book = new ItemStack(Items.ENCHANTED_BOOK);
            var holder = enchantments.getHolderOrThrow(key);
            EnchantmentHelper.updateEnchantments(book, mutable -> mutable.set(holder, 1));
            anvil.getSlot(0).set(first.copy());
            anvil.getSlot(1).set(book);
            helper.assertTrue(!anvil.getSlot(2).getItem().isEmpty() == !key.equals(Enchantments.SHARPNESS),
                    "Anvil enchantment eligibility must match the lamp allowlist: " + key.location());
        }
        first.enchant(enchantments.getHolderOrThrow(Enchantments.UNBREAKING), 1);
        first.enchant(enchantments.getHolderOrThrow(Enchantments.VANISHING_CURSE), 1);
        var grindstone = new GrindstoneMenu(0, player.getInventory(), ContainerLevelAccess.NULL);
        grindstone.getSlot(0).set(first.copy());
        grindstone.getSlot(1).set(second.copy());
        var ground = grindstone.getSlot(2).getItem().copy();
        assertRepaired(helper, player, ground, first);
        helper.assertTrue(ground.getEnchantmentLevel(enchantments.getHolderOrThrow(Enchantments.UNBREAKING)) == 0
                        && ground.getEnchantmentLevel(enchantments.getHolderOrThrow(Enchantments.VANISHING_CURSE)) == 1,
                "Grindstone repair must remove ordinary enchantments and preserve curses");
        var recipe = new RepairItemRecipe(CraftingBookCategory.MISC);
        var input = CraftingInput.of(2, 1, List.of(first.copy(), second.copy()));
        helper.assertTrue(recipe.matches(input, helper.getLevel()), "Crafting must accept two damaged lamps");
        assertRepaired(helper, player, recipe.assemble(input, helper.getLevel().registryAccess()), first);
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void enchantingTableAndMendingUseVanillaMechanisms(GameTestHelper helper) {
        var player = player(helper, "lamp_enchants");
        var lamp = lamp();
        var registry = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        helper.assertTrue(lamp.isEnchantable(), "The enchanting table must accept the lamp");
        var choices = EnchantmentHelper.getAvailableEnchantmentResults(30, lamp, registry.holders().map(holder -> holder));
        helper.assertTrue(!choices.isEmpty() && choices.stream().allMatch(choice -> choice.enchantment.is(Enchantments.UNBREAKING)),
                "Table enchantment candidates must contain only Unbreaking");
        lamp.enchant(registry.getHolderOrThrow(Enchantments.MENDING), 1);
        lamp.setDamageValue(20);
        player.setItemSlot(EquipmentSlot.OFFHAND, lamp);
        player.takeXpDelay = 0;
        new ExperienceOrb(helper.getLevel(), player.getX(), player.getY(), player.getZ(), 5).playerTouch(player);
        helper.assertTrue(lamp.getDamageValue() == 10, "An XP orb must repair the held lamp through vanilla Mending");
        helper.succeed();
    }

    private static ItemStack lamp() {
        return new ItemStack(ItemRegistry.STARDUST_HAND_LAMP.get());
    }

    private static void assertRepaired(GameTestHelper helper, ServerPlayer player, ItemStack result, ItemStack original) {
        helper.assertTrue(result.is(ItemRegistry.STARDUST_HAND_LAMP.get()) && result.getDamageValue() < original.getDamageValue()
                        && !ISpellContainer.isSpellContainer(result), "Repair must restore durability without storing a spell container");
        player.setItemSlot(EquipmentSlot.MAINHAND, result);
        helper.assertTrue(new SpellSelectionManager(player).getSpellForSlot(SELECTION_SLOT, 0).getLevel() == 1,
                "A repaired lamp must still provide its intrinsic level-one Stardust");
    }

    private static ServerPlayer player(GameTestHelper helper, String name) {
        var player = FakePlayerFactory.get(helper.getLevel(), profile(name));
        prepare(helper, player);
        return player;
    }

    private static GameProfile profile(String name) {
        return new GameProfile(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)), name);
    }

    private static void prepare(GameTestHelper helper, ServerPlayer player) {
        player.setPos(helper.absolutePos(new BlockPos(2, 2, 2)).getCenter());
        player.setXRot(-90);
        player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        player.getInventory().offhand.set(0, ItemStack.EMPTY);
        Objects.requireNonNull(player.getAttribute(MAX_MANA)).setBaseValue(1000);
        var data = MagicData.getPlayerMagicData(player);
        data.setServerPlayer(player);
        ready(player);
    }

    private static void ready(ServerPlayer player) {
        var data = MagicData.getPlayerMagicData(player);
        data.resetCastingState();
        data.getPlayerCooldowns().clearCooldowns();
        data.setMana(1000);
    }

    private static void select(ServerPlayer player, String slot) {
        MagicData.getPlayerMagicData(player).getSyncedData().getSpellSelection().makeSelection(slot, 0);
    }

    private static void finishCast(GameTestHelper helper, ServerPlayer player, boolean expectProjectile) {
        var data = MagicData.getPlayerMagicData(player);
        var spell = SpellRegistry.STARDUST.get();
        int[] projectiles = {0};
        Consumer<EntityJoinLevelEvent> observer = event -> {
            if (event.getEntity() instanceof StardustProjectile projectile && projectile.getOwner() == player) projectiles[0]++;
        };
        NeoForge.EVENT_BUS.addListener(observer);
        try {
            // 実通信とclient入力を除き、Iron'sの開始処理から発動・終了までを通す。
            spell.castSpell(helper.getLevel(), data.getCastingSpellLevel(), player, data.getCastSource(), true);
            spell.onServerCastComplete(helper.getLevel(), data.getCastingSpellLevel(), player, data, false);
        } finally {
            NeoForge.EVENT_BUS.unregister(observer);
        }
        helper.assertTrue(projectiles[0] == (expectProjectile ? 1 : 0), "Casting must emit a projectile only for a valid source");
    }

    private static final class HiddenOffhandPlayer extends FakePlayer {
        private HiddenOffhandPlayer(ServerLevel level, GameProfile profile) {
            super(level, profile);
        }

        @Override
        public @NotNull ItemStack getOffhandItem() {
            return ItemStack.EMPTY;
        }
    }
}
