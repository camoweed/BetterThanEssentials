package wyspr.BTAEssentials.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeString;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.arguments.ArgumentTypeEntity;
import net.minecraft.core.net.command.helpers.EntitySelector;
import net.minecraft.core.net.packet.PacketChat;
import net.minecraft.server.entity.player.PlayerServer;

import java.util.List;

@SuppressWarnings("ALL") public class SudoCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		CommandNode<Object> cmd = commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("")
			.then(ArgumentBuilderRequired
				.argument("target", ArgumentTypeEntity.username())
				.then(ArgumentBuilderRequired
					.argument("command", ArgumentTypeString.greedyString())
					.executes(context -> {
						CommandSource source       = (CommandSource) context.getSource();
						Player        player       = source.getSender();
						String        commandParam = context.getArgument("command", String.class);
						String        command      = "/" + commandParam;

						EntitySelector entitySelector = (EntitySelector) context.getArgument(
							"target",
							EntitySelector.class
						);
						List<? extends Entity> entities = entitySelector.get(source);
						PlayerServer           target   = (PlayerServer) entities.get(0);

						target.playerNetServerHandler.handleChat(new PacketChat(command));

						return 1;
					}))));

		String[] literals = {"sudo", "doas"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin())
				.redirect(cmd));
		}
	}
}
//import java.util.Arrays;
//
//public class SudoCommand extends Command {
//
//	@Override
//	public boolean execute(CommandHandler handler, CommandSender sender, String[] args) {
//		if (args.length < 2) return false;
//
//		String username = args[0];
//		String commandTitle = args[1];
//		String[] commandArgs = null;
//		if (args.length > 2) {
//			commandArgs = Arrays.copyOfRange(args, 2, args.length);
//		}
//
//		EntityPlayerMP targetPlayer;
//
//		if (handler.playerExists(username)) {
//			targetPlayer = (EntityPlayerMP) handler.getPlayer(username);
//		} else {
//			sender.sendMessage(TextFormatting.RED + "Player not found: " + TextFormatting.ORANGE + username);
//			return false;
//		}
//		ServerPlayerCommandSender targetPlayerSender = new ServerPlayerCommandSender(handler.asServer().minecraftServer, targetPlayer);
//
//		for (Command command : Commands.commands) {
//			if (!command.isName(commandTitle)) continue;
//			if (!targetPlayer.isOperator() && command.opRequired(commandArgs)) {
//				sender.sendMessage(TextFormatting.ORANGE + username + TextFormatting.RED + "doesn't have permission to use this command!");
//				return true;
//			}
//			try {
//				boolean success = command.execute(handler, targetPlayerSender, commandArgs);
//				if (!success) {
//					sender.sendMessage(TextFormatting.RED + "Error: invalid arguments");
//					command.sendCommandSyntax(handler, sender);
//				}
//			} catch (CommandError e) {
//				sender.sendMessage(TextFormatting.RED + e.getMessage());
//			}
//			return true;
//		}
//
//		sender.sendMessage(TextFormatting.RED + "Command not found: " + TextFormatting.ORANGE + commandTitle);
//		return false;
//	}
