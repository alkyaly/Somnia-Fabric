package mods.su5ed.somnia.api;

import mods.su5ed.somnia.config.ReplenishingItemEntry;
import mods.su5ed.somnia.core.Somnia;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class SomniaAPI {
    private static final List<ReplenishingItemEntry> REPLENISHING_ITEMS = new ArrayList<>();

    public static void addReplenishingItem(Item stack, double fatigueToReplenish) {
        addReplenishingItem(stack, fatigueToReplenish, Somnia.CONFIG.fatigue.fatigueRate);
    }

    public static void addReplenishingItem(Item item, double fatigueToReplenish, double fatigueRateModifier) {
        REPLENISHING_ITEMS.add(new ReplenishingItemEntry(Registry.ITEM.getKey(item).toString(), fatigueToReplenish, fatigueRateModifier));
    }

    public static List<ReplenishingItemEntry> getReplenishingItems() {
        return new ArrayList<>(REPLENISHING_ITEMS);
    }
}
