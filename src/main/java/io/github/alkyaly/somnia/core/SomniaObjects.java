package io.github.alkyaly.somnia.core;

import io.github.alkyaly.somnia.mixin.accessor.PotionBrewingAccessor;
import io.github.alkyaly.somnia.object.AwakeningEffect;
import io.github.alkyaly.somnia.object.InsomniaEffect;
import net.minecraft.core.Registry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

public final class SomniaObjects {
    public static final MobEffect AWAKENING_EFFECT = new AwakeningEffect();
    public static final MobEffect INSOMNIA_EFFECT = new InsomniaEffect();
    public static final Potion AWAKENING_POTION = new Potion("awakening", new MobEffectInstance(AWAKENING_EFFECT, 2400));
    public static final Potion LONG_AWAKENING_POTION = new Potion("long_awakening", new MobEffectInstance(AWAKENING_EFFECT, 3600));
    public static final Potion STRONG_AWAKENING_POTION = new Potion("strong_awakening", new MobEffectInstance(AWAKENING_EFFECT, 2400, 1));

    public static final Potion INSOMNIA_POTION = new Potion("insomnia", new MobEffectInstance(INSOMNIA_EFFECT, 1800));
    public static final Potion LONG_INSOMNIA_POTION = new Potion("long_insomnia", new MobEffectInstance(INSOMNIA_EFFECT, 3000));
    public static final Potion STRONG_INSOMNIA_POTION = new Potion("strong_insomnia", new MobEffectInstance(INSOMNIA_EFFECT, 1800, 1));

    public static void init() {
        registerEffect("awakening", AWAKENING_EFFECT);
        registerEffect("insomnia", INSOMNIA_EFFECT);
        registerPotion(AWAKENING_POTION);
        registerPotion(LONG_AWAKENING_POTION);
        registerPotion(STRONG_AWAKENING_POTION);
        registerPotion(INSOMNIA_POTION);
        registerPotion(LONG_INSOMNIA_POTION);
        registerPotion(STRONG_INSOMNIA_POTION);
    }

    private static void registerPotion(Potion potion) {
        Registry.register(Registry.POTION, Somnia.locate(potion.getName("")), potion);
    }

    private static void registerEffect(String id, MobEffect effect) {
        Registry.register(Registry.MOB_EFFECT, Somnia.locate(id), effect);
    }

    static void setupPotions() {
        PotionBrewingAccessor.somnia$invokeAddMix(Potions.NIGHT_VISION, Items.GLISTERING_MELON_SLICE, AWAKENING_POTION);
        PotionBrewingAccessor.somnia$invokeAddMix(Potions.LONG_NIGHT_VISION, Items.GLISTERING_MELON_SLICE, LONG_AWAKENING_POTION);
        PotionBrewingAccessor.somnia$invokeAddMix(Potions.NIGHT_VISION, Items.BLAZE_POWDER, STRONG_AWAKENING_POTION);
        PotionBrewingAccessor.somnia$invokeAddMix(AWAKENING_POTION, Items.FERMENTED_SPIDER_EYE, INSOMNIA_POTION);
        PotionBrewingAccessor.somnia$invokeAddMix(LONG_AWAKENING_POTION, Items.FERMENTED_SPIDER_EYE, LONG_INSOMNIA_POTION);
        PotionBrewingAccessor.somnia$invokeAddMix(STRONG_AWAKENING_POTION, Items.FERMENTED_SPIDER_EYE, STRONG_INSOMNIA_POTION);
    }
}
