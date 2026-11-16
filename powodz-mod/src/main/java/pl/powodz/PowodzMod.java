package pl.powodz;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;

public class PowodzMod implements ModInitializer {
    public static final String ID = "powodz";

    /** Włącza skończoną fizykę wody i deszcz podnoszący wodę. */
    public static final GameRules.Key<GameRules.BooleanRule> FIZYKA =
            GameRuleRegistry.register("powodzFizyka", GameRules.Category.UPDATES, GameRuleFactory.createBooleanRule(true));
    /** Ile kropel (po 1/8 bloku) spada na tick wokół każdego gracza podczas deszczu (burza = 3x). */
    public static final GameRules.Key<GameRules.IntRule> DESZCZ =
            GameRuleRegistry.register("powodzDeszcz", GameRules.Category.UPDATES, GameRuleFactory.createIntRule(8, 0, 1000));
    /** Ile miejsc na tick odparowuje (traci 1/8) przy ładnej pogodzie. 0 = woda nie paruje. */
    public static final GameRules.Key<GameRules.IntRule> PAROWANIE =
            GameRuleRegistry.register("powodzParowanie", GameRules.Category.UPDATES, GameRuleFactory.createIntRule(2, 0, 1000));
    /** Promień wokół gracza, w którym pada deszcz i paruje woda. */
    public static final GameRules.Key<GameRules.IntRule> PROMIEN =
            GameRuleRegistry.register("powodzPromien", GameRules.Category.UPDATES, GameRuleFactory.createIntRule(64, 1, 128));

    public static final Block ODPLYW = Registry.register(Registries.BLOCK, Identifier.of(ID, "odplyw"),
            new OdplywBlock(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).strength(3.0f, 6.0f)
                    .requiresTool().sounds(BlockSoundGroup.METAL)));
    public static final Item ODPLYW_ITEM = Registry.register(Registries.ITEM, Identifier.of(ID, "odplyw"),
            new BlockItem(ODPLYW, new Item.Settings()));

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(e -> e.add(ODPLYW_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(e -> e.add(ODPLYW_ITEM));
        ServerTickEvents.END_WORLD_TICK.register(Deszcz::tick);
    }
}
