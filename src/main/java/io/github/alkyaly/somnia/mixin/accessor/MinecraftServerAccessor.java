package io.github.alkyaly.somnia.mixin.accessor;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
    @Invoker("haveTime")
    boolean somnia$invokeHaveTime();
}
