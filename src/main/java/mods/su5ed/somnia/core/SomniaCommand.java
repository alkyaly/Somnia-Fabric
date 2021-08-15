package mods.su5ed.somnia.core;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mods.su5ed.somnia.api.capability.Components;
import mods.su5ed.somnia.api.capability.Fatigue;
import mods.su5ed.somnia.network.NetworkHandler;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class SomniaCommand {
    public static final Set<UUID> OVERRIDES = new HashSet<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("somnia")
                .requires(src -> src.hasPermission(3))

                .then(Commands.literal("fatigue")
                        .then(Commands.literal("set")
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                        .executes(ctx -> setFatigue(DoubleArgumentType.getDouble(ctx, "amount"), ctx.getSource().getPlayerOrException()))
                                        .then(Commands.argument("target", EntityArgument.players())
                                                .executes(ctx -> setFatigue(DoubleArgumentType.getDouble(ctx, "amount"), EntityArgument.getPlayer(ctx, "targets")))))))

                .then(Commands.literal("override")
                        .then(Commands.literal("add")
                                .then(Commands.argument("target", EntityArgument.players())
                                        .executes(ctx -> addOverride(EntityArgument.getPlayer(ctx, "target")))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("target", EntityArgument.players())
                                        .executes(ctx -> removeOverride(EntityArgument.getPlayer(ctx, "target")))))
                        .then(Commands.literal("list")
                                .executes(SomniaCommand::listOverrides))));

    }

    private static int setFatigue(double amount, ServerPlayer player) {
        Fatigue props = Components.get(player);

        if (props != null) {
            props.setFatigue(amount);

            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeDouble(props.getFatigue());

            ServerPlayNetworking.send(player, NetworkHandler.UPDATE_FATIGUE, buf);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int addOverride(ServerPlayer player) {
        if (!OVERRIDES.add(player.getUUID()))
            player.displayClientMessage(new TextComponent("Override already exists"), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int removeOverride(ServerPlayer target) {
        OVERRIDES.remove(target.getUUID());
        return Command.SINGLE_SUCCESS;
    }

    private static int listOverrides(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer sender = ctx.getSource().getPlayerOrException();
        List<String> overrides = OVERRIDES.stream()
                .map(sender.level::getPlayerByUUID)
                .filter(Objects::nonNull)
                .map(player -> player.getName().toString())
                .collect(Collectors.toList());

        Component chatComponent = new TextComponent(!overrides.isEmpty() ? String.join(", ", overrides) : "Nothing to see here...");
        sender.displayClientMessage(chatComponent, false);
        return Command.SINGLE_SUCCESS;
    }
}