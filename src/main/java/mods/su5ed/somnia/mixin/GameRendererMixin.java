package mods.su5ed.somnia.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mods.su5ed.somnia.util.ASMHooks;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(at = @At("HEAD"), method = "renderLevel", cancellable = true) //Original JS-Coremod: patchGameRenderer.js
    private void somnia$skipRenderLevel(float f, long l, PoseStack poseStack, CallbackInfo ci) {
        if (ASMHooks.skipRenderWorld(f, l, poseStack)) {
            ci.cancel();
        }
    }
}
