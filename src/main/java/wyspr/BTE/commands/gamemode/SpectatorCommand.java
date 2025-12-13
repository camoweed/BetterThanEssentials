package wyspr.BTE.commands.gamemode;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.player.gamemode.Gamemode;
import net.minecraft.server.entity.player.PlayerServer;
import wyspr.BTE.Essentials;
import wyspr.BTE.commands.arguments.ArgumentTypeUser;

import java.text.MessageFormat;

@SuppressWarnings("ALL") public class SpectatorCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		String[] literals = {"spectator", "gmsp", "spec"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.GamemodeCommand)
				.executes(this::noArg)
				.then(ArgumentBuilderRequired
					.argument("user", ArgumentTypeUser.user())
					.requires(source -> ((CommandSource) source).hasAdmin())
					.executes(this::playerArg)));
		}
	}

	private int noArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source = (CommandSource) context.getSource();
		Player        player = source.getSender();
		player.setGamemode(Gamemode.spectator);
		player.sendMessage(TextFormatting.YELLOW + "Set own gamemode to " + TextFormatting.CYAN + "Spectator");
		return 0;
	}

	private int playerArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source = (CommandSource) context.getSource();
		Player        player = source.getSender();
		PlayerServer  user   = context.getArgument("user", PlayerServer.class);
		user.setGamemode(Gamemode.spectator);
		player.sendMessage(MessageFormat.format(
			"{0}Set gamemode to {1}Spectator{0} for " + TextFormatting.RESET + user.getDisplayName(),
			TextFormatting.YELLOW,
			TextFormatting.CYAN
		));
		user.sendMessage(TextFormatting.YELLOW + "Your gamemode was set to " + TextFormatting.CYAN + "Spectator");
		return 0;
	}
}
