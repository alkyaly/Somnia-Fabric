package io.github.alkyaly.somnia.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BedBlock.class)
public class BedBlockMixin {

    //Minecraft for some reason doesn't check for null when sending a BedSleepingProblem message to Player#displayClientMessage
    //causing a non-crashing NPE when Gui#handleChat tries to decompose the received Component that is null
    //This wouldn't happen in normal circumstances because the checks for behavior are made before the trySleep return
    //
    //We need 2 methods because my dev-environment is in Mojang Official Mappings, which doesn't have actually intermediary
    //lambda names and aren't remapped either, so we need one for production (Intermediary) and one for dev (MM).
    //Also, the method name is bad because I'm bad at naming things.
    @Dynamic("Inject in lambda")
    @Inject(at = @At("HEAD"), method = "lambda$use$0", require = 0, remap = false, cancellable = true)
    private static void somnia$dontPassNullMessageToBedBlockDev(Player player, Player.BedSleepingProblem problem, CallbackInfo ci) {
        if (problem != null && problem.getMessage() == null) {
            ci.cancel();
        }
    }

    @Dynamic("Inject in lambda")
    @Inject(at = @At("HEAD"), method = "method_19283", require = 0, remap = false, cancellable = true)
    private static void somnia$dontPassNullMessageToBedBlockProd(Player player, Player.BedSleepingProblem problem, CallbackInfo ci) {
        if (problem != null && problem.getMessage() == null) {
            ci.cancel();
        }
    }
}
