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
import net.minecraft.server.entity.player.PlayerServer;
import wyspr.BTE.commands.arguments.ArgumentTypeOnlineUser;
import wyspr.BTE.utils.PlayerData;
import wyspr.BTE.utils.Utils;

@SuppressWarnings("ALL")
public class GodCommand implements CommandManager.CommandRegistry {

	@Override
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		String[] literals = {"god", "godmode"};
		for (String literal : literals) {
			dispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin())
				.executes(this::noArg)
				.then(ArgumentBuilderRequired
					.argument("player", ArgumentTypeOnlineUser.online())
					.executes(this::userArg)));
		}
	}

	private int noArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source = (CommandSource) context.getSource();
		Player        player = Utils.requirePlayer(source);
		boolean isGodMode = PlayerData
			.get(player)
			.toggleGodMode();

		if (isGodMode) {
			player.sendMessage(
				TextFormatting.YELLOW + "God mode " +
					TextFormatting.LIME + "on"
			);
		} else {
			player.sendMessage(
				TextFormatting.YELLOW + "God mode " +
					TextFormatting.RED + "off"
			);
		}

		return 1;
	}

	private int userArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source = (CommandSource) context.getSource();
		Player        player = Utils.requirePlayer(source);
		PlayerServer  target = context.getArgument("player", PlayerServer.class);
		boolean isGodMode = PlayerData
			.get(target)
			.toggleGodMode();

		if (isGodMode) {
			player.sendMessage(
				TextFormatting.YELLOW + "God mode " +
					TextFormatting.LIME + "on " +
					TextFormatting.YELLOW + "for " +
					TextFormatting.RESET + target.getDisplayName()
			);
			target.sendMessage(
				TextFormatting.YELLOW + "God mode " +
					TextFormatting.LIME + "on"
			);
		} else {
			player.sendMessage(
				TextFormatting.YELLOW + "God mode " +
					TextFormatting.RED + "off " +
					TextFormatting.YELLOW + "for " +
					TextFormatting.RESET + target.getDisplayName()
			);
			target.sendMessage(
				TextFormatting.YELLOW + "God mode " +
					TextFormatting.RED + "off"
			);
		}

		return 1;
	}
}
