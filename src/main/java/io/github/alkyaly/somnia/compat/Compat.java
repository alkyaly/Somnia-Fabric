package io.github.alkyaly.somnia.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

//todo: I think adorn adds non-bed sleep-able blocks? I might have to add compat for it.
public final class Compat {
    private static final boolean COMFORTS = FabricLoader.getInstance().isModLoaded("comforts");

    public static boolean isSleepingInHammock(Player player) {
        if (COMFORTS) {
            Optional<BlockPos> pos = player.getSleepingPos();
            if (pos.isPresent()) {
                Block block = player.level.getBlockState(pos.get()).getBlock();
                ResourceLocation regName = Registry.BLOCK.getKey(block);
                return regName.getNamespace().equals("comforts") && regName.getPath().startsWith("hammock");
            }
        }
        return false;
    }

    public static boolean isSleepingInBag(Player player) {
        Item item = player.getInventory().getSelected().getItem();
        ResourceLocation name = Registry.ITEM.getKey(item);

        return name.getNamespace().equals("comforts") && name.getPath().startsWith("sleeping_bag");
    }

    public static boolean isBed(BlockState state) {
        if (COMFORTS) {
            ResourceLocation regName = Registry.BLOCK.getKey(state.getBlock());
            if (regName.getNamespace().equals("comforts") && regName.getPath().startsWith("hammock")) {
                return false;
            }
        }
        return state.getBlock() instanceof BedBlock;
    }
}
