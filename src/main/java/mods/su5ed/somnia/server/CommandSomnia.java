package mods.su5ed.somnia.server;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mods.su5ed.somnia.Somnia;
import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import mods.su5ed.somnia.common.network.NetworkHandler;
import mods.su5ed.somnia.common.network.packet.PacketUpdateFatigue;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommandSomnia {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("somnia")
				.requires(src -> src.hasPermissionLevel(3))
				.then(Commands.literal("fatigue")
                    .then(Commands.literal("set")
						.then(Commands.argument("amount", DoubleArgumentType.doubleArg())
							.executes(ctx -> CommandSomnia.setFatigue(DoubleArgumentType.getDouble(ctx, "amount"), ctx.getSource().asPlayer()))
								.then(Commands.argument("target", EntityArgument.players())
									.executes(ctx -> CommandSomnia.setFatigue(DoubleArgumentType.getDouble(ctx, "amount"), EntityArgument.getPlayer(ctx, "targets")))))))
				.then(Commands.literal("override")
					.then(Commands.literal("add")
						.then(Commands.argument("target", EntityArgument.players())
							.executes(ctx -> addOverride(EntityArgument.getPlayer(ctx, "target")))))
					.then(Commands.literal("remove")
						.then(Commands.argument("target", EntityArgument.players())
							.executes(ctx -> removeOverride(EntityArgument.getPlayer(ctx, "target")))))
					.then(Commands.literal("list")
						.executes(CommandSomnia::listOverrides))));

	}

	private static int setFatigue(double amount, ServerPlayerEntity player) {
		player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null).ifPresent(props -> {
			props.setFatigue(amount);
			NetworkHandler.sendToClient(new PacketUpdateFatigue(props.getFatigue()), player);
		});
		return Command.SINGLE_SUCCESS;
	}

	private static int addOverride(ServerPlayerEntity player) {
		if (!Somnia.instance.ignoreList.add(player.getUniqueID())) player.sendStatusMessage(new StringTextComponent("Override already exists"), true);

		return Command.SINGLE_SUCCESS;
	}

	private static int removeOverride(ServerPlayerEntity target) {
		Somnia.instance.ignoreList.remove(target.getUniqueID());
		return Command.SINGLE_SUCCESS;
	}

	private static int listOverrides(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
		ServerPlayerEntity sender = ctx.getSource().asPlayer();
		List<String> overrides = Somnia.instance.ignoreList.stream()
				.map(sender.world::getPlayerByUuid)
				.filter(Objects::nonNull)
				.map(player -> player.getName().toString())
				.collect(Collectors.toList());

		ITextComponent chatComponent = new StringTextComponent(!overrides.isEmpty() ? String.join(", ", overrides) : "Nothing to see here...");
		sender.sendStatusMessage(chatComponent, false);
		return Command.SINGLE_SUCCESS;
	}
}