package mods.su5ed.somnia.api;

import mods.su5ed.somnia.config.ReplenishingItemEntry;
import mods.su5ed.somnia.core.Somnia;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public final class SomniaAPI {
    private static final List<ReplenishingItemEntry> REPLENISHING_ITEMS = new ArrayList<>();

    public static void addReplenishingItem(Item item, double fatigueToReplenish) {
        addReplenishingItem(item, fatigueToReplenish, Somnia.CONFIG.fatigue.fatigueRate);
    }

    public static void addReplenishingItem(Item item, double fatigueToReplenish, double fatigueRateModifier) {
        addReplenishingItem(Registry.ITEM.getKey(item), fatigueToReplenish, fatigueRateModifier);
    }

    public static void addReplenishingItem(ResourceLocation item, double fatigueToReplenish, double fatigueRateModifier) {
        REPLENISHING_ITEMS.add(new ReplenishingItemEntry(item.toString(), fatigueToReplenish, fatigueRateModifier));
    }

    public static List<ReplenishingItemEntry> getReplenishingItems() {
        return new ArrayList<>(REPLENISHING_ITEMS);
    }
}
