package wyspr.BTE.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.net.packet.PacketChat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.command.ServerCommandSource;
import wyspr.BTE.commands.arguments.ArgumentTypeCommand;
import wyspr.BTE.commands.arguments.ArgumentTypeOnlineUser;

@SuppressWarnings("ALL")
public class SudoCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		String[] literals = {"sudo", "doas"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin())
				.then(ArgumentBuilderRequired
					.argument("player", ArgumentTypeOnlineUser.online())
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
			player.playerNetServerHandler.handleChat(new PacketChat(command));
			sender.sendMessage((TextFormatting.YELLOW + "Sent \"" + TextFormatting.LIGHT_BLUE + command + TextFormatting.YELLOW + "\" as " + TextFormatting.RESET + player.getDisplayName()));
			return 1;
		}

		MinecraftServer     mcServer            = MinecraftServer.getInstance();
		ServerCommandSource playerCommandSource = new ServerCommandSource(mcServer, player);
		CommandDispatcher<CommandSource> dispatcher = mcServer
			.getDimensionWorld(player.dimension)
			.getCommandManager()
			.getDispatcher();

		try {
			command = command.substring(1);
			dispatcher.execute(command, playerCommandSource);
			sender.sendMessage((TextFormatting.YELLOW + "Ran " + TextFormatting.LIGHT_BLUE + "/" + command + TextFormatting.YELLOW + " as " + TextFormatting.RESET + player.getDisplayName()));
		} catch (CommandSyntaxException e) {
			sender.sendMessage((TextFormatting.ORANGE + "Failed to run " + TextFormatting.LIGHT_BLUE + "/" + command + TextFormatting.ORANGE + " as " + TextFormatting.RESET + player.getDisplayName()));
			sender.sendMessage((TextFormatting.RED + "Error: " + TextFormatting.WHITE + e.getMessage()));
		}

		return 1;
	}
}
