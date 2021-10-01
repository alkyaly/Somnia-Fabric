package io.github.alkyaly.somnia.config;

import draylar.omegaconfig.api.Comment;
import draylar.omegaconfig.api.Config;
import io.github.alkyaly.somnia.core.Somnia;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("CanBeFinal")
public class SomniaConfig implements Config {

    public Fatigue fatigue = new Fatigue();
    public Logic logic = new Logic();
    public Options options = new Options();
    public Performance performance = new Performance();
    public Timings timings = new Timings();

    public static class Fatigue {
        //Fatigue
        @Comment("The fatigue counter's position. Accepted values: TOP_CENTER, TOP_LEFT, TOP_RIGHT, BOTTOM_CENTER, BOTTOM_LEFT, BOTTOM_RIGHT.")
        public String displayFatigue = "BOTTOM_RIGHT";
        @Comment("Simplifies the numerical fatigue counter to words")
        public boolean simpleFatigueDisplay = false;
        @Comment("The ETA and multiplier display position in Somnia's sleep gui. Accepted values: right, center, left.")
        public String displayETASleep = "left";
        @Comment("Fatigue is incremented by this number every tick")
        public double fatigueRate = 0.00208;
        @Comment("Fatigue is decreased by this number while you sleep (every tick)")
        public double fatigueReplenishRate = 0.00833;
        @Comment("Enables fatigue side effects")
        public boolean fatigueSideEffects = true;
        @Comment("The required amount of fatigue to sleep")
        public double minimumFatigueToSleep = 20;
        @Comment("""
                Definitions of each side effect stage in order:
                    //min fatigue, max fatigue, potion ID, duration, amplifier.
                    //For a permanent effect, set the duration to -1.
                """)
        public List<SideEffectStageEntry> sideEffectStages = Arrays.asList(
                new SideEffectStageEntry(70, 80, "minecraft:nausea", 150, 0),
                new SideEffectStageEntry(80, 90, "minecraft:slowness", 300, 2),
                new SideEffectStageEntry(90, 95, "minecraft:poison", 200, 1),
                new SideEffectStageEntry(95, 100, "minecraft:slowness", -1, 3)
        );
        @Comment("""
                Definitions of fatigue replenishing items.
                    //Each entry consist of an item registry name, the amount of fatigue it replenishes,
                    //and a fatigue rate modifier.
                """)
        public List<ReplenishingItemEntry> replenishingItems = Arrays.asList(
                new ReplenishingItemEntry("mod:item", 10) //I have to find a fabric mod that adds tea or coffee lol...
        );
    }

    public static class Logic {
        //Logic
        @Comment("""
                If the time difference (mc) between multiplied ticking is greater than this,
                    //the simulation multiplier is lowered. Otherwise, it's increased.
                    //Lowering this number might slow down simulation and improve performance.
                    //Don't mess around with it if you don't know what you're doing.
                    //Range: 1 ~ 50.
                """)
        public double delta = 50;
        @Comment("Minimum tick speed multiplier, activated during sleep")
        public double baseMultiplier = 1;
        @Comment("Maximum tick speed multiplier, activated during sleep")
        public double multiplierCap = 100;
    }

    public static class Options {
        //Options
        @Comment("Slightly slower sleep start/end")
        public boolean fading = true;
        @Comment("Let the player sleep even when there are monsters nearby")
        public boolean ignoreMonsters = false;
        @Comment("Deafens you while you're asleep. Mob sounds are confusing with the level sped up")
        public boolean muteSoundWhenSleeping = false;
        @Comment("Allows you to sleep with armor equipped")
        public boolean sleepWithArmor = false;
        @Comment("Provides an enhanced sleeping gui")
        public boolean somniaGui = true;
        @Comment("The display position of the clock in somnia's enhanced sleeping gui. Accepted values: right, center, left.")
        public String somniaGuiClockPosition = "right";
        @Comment("The item used to select wake time")
        public String wakeTimeSelectItem = "minecraft:clock";
        public boolean easterEgg = true;
    }

    public static class Performance {
        //Performance
        @Comment("Disables mob spawning while you sleep")
        public boolean disableCreatureSpawning = false;
        @Comment("Disable rendering while you're asleep")
        public boolean disableRendering = false;
    }

    public static class Timings {
        //Timings
        @Comment("Specifies the start of the period in which the player can enter sleep. Range: 0 ~ 24000.")
        public int enterSleepStart = 0;
        @Comment("Specifies the end of the period in which the player can enter sleep. Range: 0 ~ 24000.")
        public int enterSleepEnd = 24000;
        @Comment("Specifies the start of the valid sleep period. Range: 0 ~ 24000.")
        public int validSleepStart = 0;
        @Comment("Specifies the end of the valid sleep period. Range: 0 ~ 24000.")
        public int validSleepEnd = 24000;
    }

    @Override
    public @Nullable String getModid() {
        return Somnia.MOD_ID;
    }

    @Override
    public String getName() {
        return "somnia";
    }

    @Override
    public String getExtension() {
        return "json5";
    }
}
