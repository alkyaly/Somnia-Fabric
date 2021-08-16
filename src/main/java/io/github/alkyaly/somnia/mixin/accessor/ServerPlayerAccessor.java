package io.github.alkyaly.somnia.mixin.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerPlayer.class)
public interface ServerPlayerAccessor {
    @Invoker("bedInRange")
    boolean somnia$invokeBedInRange(BlockPos pos, Direction direction);
}
