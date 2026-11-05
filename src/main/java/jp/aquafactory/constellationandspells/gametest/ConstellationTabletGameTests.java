package jp.aquafactory.constellationandspells.gametest;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.gui.inscription_table.InscriptionTableMenu;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.registry.AttributeRegistry;
import jp.aquafactory.constellationandspells.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Comparator;
import java.util.Objects;

import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MAX_MANA;
import static io.redspace.ironsspellbooks.registries.ItemRegistry.SCROLL;
import static io.redspace.ironsspellbooks.registries.ItemRegistry.DIAMOND_SPELL_BOOK;

@GameTestHolder(ConstellationnSpells.MODID)
@PrefixGameTestTemplate(false)
// GameTestがアノテーションから探索して呼び出す。
@SuppressWarnings("unused")
public final class ConstellationTabletGameTests {
    @GameTest(template = "gametest/basic_floor")
    public static void attributesFollowCuriosEquipmentChanges(GameTestHelper helper) {
        var player = player(helper);
        var mana = Objects.requireNonNull(player.getAttribute(MAX_MANA), "Player must have a mana attribute");
        var power = Objects.requireNonNull(player.getAttribute(AttributeRegistry.ZODIAC_SPELL_POWER),
                "Player must have a Zodiac power attribute");
        mana.setBaseValue(300);
        power.setBaseValue(2);
        // 加算補正と既存のBASE補正を併用し、TOTAL乗算や単純加算との違いも検出する。
        power.addTransientModifier(new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "tablet_test_bonus"),
                0.5, AttributeModifier.Operation.ADD_VALUE));
        power.addTransientModifier(new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, "tablet_test_base_bonus"),
                0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        var tablet = new ItemStack(ItemRegistry.CONSTELLATION_TABLET.get());
        var inventory = CuriosApi.getCuriosInventory(player).orElseThrow();
        var slots = inventory.getStacksHandler("spellbook").orElseThrow().getStacks();
        helper.assertTrue(slots.isItemValid(0, tablet), "The spellbook slot must accept the tablet through its tag");

        player.setItemSlot(EquipmentSlot.MAINHAND, tablet);
        tickEquipment(player);
        assertAttributes(helper, player, 300, 3);
        player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.OFFHAND, tablet);
        tickEquipment(player);
        assertAttributes(helper, player, 300, 3);
        player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);

        slots.setStackInSlot(0, tablet);
        tickEquipment(player);
        assertAttributes(helper, player, 500, 3.25);
        slots.setStackInSlot(0, tablet.copy());
        tickEquipment(player);
        assertAttributes(helper, player, 500, 3.25);

        slots.setStackInSlot(0, new ItemStack(DIAMOND_SPELL_BOOK.get()));
        tickEquipment(player);
        assertAttributes(helper, player, 400, 3);
        slots.setStackInSlot(0, tablet);
        tickEquipment(player);
        assertAttributes(helper, player, 500, 3.25);
        slots.setStackInSlot(0, ItemStack.EMPTY);
        tickEquipment(player);
        assertAttributes(helper, player, 300, 3);
        helper.succeed();
    }

    @GameTest(template = "gametest/basic_floor")
    public static void inscriptionUsesAllSlotsAndSurvivesSaving(GameTestHelper helper) {
        var player = player(helper);
        var tablet = new ItemStack(ItemRegistry.CONSTELLATION_TABLET.get());
        var initial = ISpellContainer.get(tablet);
        helper.assertTrue(initial != null && initial.isEmpty() && initial.mustEquip() && initial.isSpellWheel(),
                "A fresh tablet must initialize an empty equipped spell wheel container");
        var spells = SpellRegistry.getEnabledSpells().stream()
                .sorted(Comparator.comparing(spell -> spell.getSpellResource().toString()))
                .limit(13).toList();
        helper.assertTrue(spells.size() == 13, "The inscription test requires thirteen distinct spells");
        var menu = new InscriptionTableMenu(0, player.getInventory(), ContainerLevelAccess.NULL);
        helper.assertTrue(menu.getSpellBookSlot().mayPlace(tablet), "The inscription table must accept the tablet");
        menu.getSpellBookSlot().set(tablet);
        for (int i = 0; i < 12; i++) {
            var scroll = new ItemStack(SCROLL.get());
            ISpellContainer.createScrollContainer(spells.get(i), 1, scroll);
            menu.getScrollSlot().set(scroll);
            menu.doInscription(i);
            helper.assertTrue(menu.getScrollSlot().getItem().isEmpty()
                            && ISpellContainer.get(tablet).getSpellAtIndex(i).getSpell().equals(spells.get(i)),
                    "Inscription must consume the scroll and fill tablet slot " + i);
        }
        var extraScroll = new ItemStack(SCROLL.get());
        ISpellContainer.createScrollContainer(spells.get(12), 1, extraScroll);
        menu.getScrollSlot().set(extraScroll);
        menu.doInscription(12);
        helper.assertTrue(menu.getScrollSlot().getItem().getCount() == 1
                        && ISpellContainer.get(tablet).getActiveSpellCount() == 12,
                "A full tablet must reject a thirteenth spell without consuming its scroll");

        var restored = ItemStack.parse(helper.getLevel().registryAccess(),
                tablet.save(helper.getLevel().registryAccess())).orElseThrow();
        helper.assertTrue(ItemStack.isSameItemSameComponents(tablet, restored),
                "Saving and loading must preserve all twelve inscribed spells and their slot data");
        menu.getSpellBookSlot().set(restored);
        menu.getScrollSlot().set(ItemStack.EMPTY);
        menu.setSelectedSpell(11);
        var extracted = menu.getResultSlot().remove(1);
        helper.assertTrue(extracted.is(SCROLL.get())
                        && ISpellContainer.get(extracted).getSpellAtIndex(0).getSpell().equals(spells.get(11)),
                "The last tablet slot must produce its original spell scroll");
        menu.getResultSlot().onTake(player, extracted);
        helper.assertTrue(ISpellContainer.get(restored).getActiveSpellCount() == 11
                        && ISpellContainer.get(restored).getSpellAtIndex(11) == SpellData.EMPTY,
                "Taking the scroll must clear only the selected tablet slot");
        menu.getScrollSlot().set(extracted);
        menu.doInscription(11);
        helper.assertTrue(menu.getScrollSlot().getItem().isEmpty()
                        && ISpellContainer.get(restored).getActiveSpellCount() == 12,
                "An extracted slot must accept inscription again");
        helper.succeed();
    }

    private static Player player(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        player.setPos(pos.getX(), pos.getY(), pos.getZ());
        return player;
    }

    private static void tickEquipment(Player player) {
        player.tick();
        // mock playerはlevelのentity tickを通らないため、Curiosの標準更新イベントを明示的に送る。
        NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(player));
    }

    private static void assertAttributes(GameTestHelper helper, Player player, double mana, double power) {
        helper.assertTrue(Math.abs(player.getAttributeValue(MAX_MANA) - mana) < 0.000001
                        && Math.abs(player.getAttributeValue(AttributeRegistry.ZODIAC_SPELL_POWER) - power) < 0.000001,
                "Tablet attributes must follow the equipped spellbook without duplication: mana="
                        + player.getAttributeValue(MAX_MANA) + ", Zodiac power="
                        + player.getAttributeValue(AttributeRegistry.ZODIAC_SPELL_POWER));
    }
}
