package wyspr.BTE.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.net.packet.PacketChat;
import net.minecraft.server.entity.player.PlayerServer;
import wyspr.BTE.commands.arguments.ArgumentTypeCommand;
import wyspr.BTE.commands.arguments.ArgumentTypeUser;

@SuppressWarnings("ALL") public class SudoCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		String[] literals = {"sudo", "doas"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin())
				.then(ArgumentBuilderRequired
					.argument("player", ArgumentTypeUser.user())
					.then(ArgumentBuilderRequired
						.argument("command", ArgumentTypeCommand.commands())
						.executes(this::exec))));
		}
	}

	private int exec(CommandContext<Object> context) {
		CommandSource source  = (CommandSource) context.getSource();
		Player        sender  = source.getSender();
		PlayerServer  player  = context.getArgument("player", PlayerServer.class);
		String        command = context.getArgument("command", String.class);

		if (!command.startsWith("/")) {
			command = "/" + command;
		}

		sender.sendMessage(TextFormatting.YELLOW + "Ran " + TextFormatting.LIGHT_BLUE + command + TextFormatting.YELLOW + " as " + TextFormatting.RESET + player.getDisplayName());
		player.playerNetServerHandler.handleChat(new PacketChat(command));

		return 1;
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
