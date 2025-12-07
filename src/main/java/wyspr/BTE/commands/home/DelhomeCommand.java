package wyspr.BTE.commands.home;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeString;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.server.entity.player.PlayerServer;
import wyspr.BTE.Essentials;
import wyspr.BTE.commands.arguments.ArgumentTypeHome;
import wyspr.BTE.commands.arguments.ArgumentTypeUser;
import wyspr.BTE.utils.PlayerData;

@SuppressWarnings("ALL") public class DelhomeCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		CommandNode<Object> command
			= commandDispatcher.register((ArgumentBuilderLiteral) (ArgumentBuilderLiteral.literal("delhome"))
			.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.HomeCommand)
			.then(ArgumentBuilderRequired
				.argument("home", ArgumentTypeHome.senderHomes())
				.executes(this::homeArg))
			.then(ArgumentBuilderRequired
				.argument("player", ArgumentTypeUser.user())
				.requires(source -> ((CommandSource) source).hasAdmin())
				.then(ArgumentBuilderRequired
					.argument("home", ArgumentTypeHome.otherHomes())
					.executes(this::playerHomeArg)))

		);

		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("rmhome")
			.redirect(command));
	}


	private int homeArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source     = (CommandSource) context.getSource();
		boolean       isAdmin    = source.hasAdmin();
		Player        player     = source.getSender();
		PlayerData    playerData = PlayerData.get(player);
		int           homes      = playerData.getHomesAmount();
		String        homeName   = context.getArgument("home", String.class);

		if (homes == 0) {
			player.sendMessage(TextFormatting.ORANGE + "You do not have any homes!");
			player.sendMessage(TextFormatting.ORANGE + "Set a home with: " + TextFormatting.LIGHT_BLUE + "/sethome [name]");
			return 1;
		}

		if (homeName.equals("bed")) {
			player.sendMessage(TextFormatting.YELLOW + "This home is reserved for your bed.");
			return 1;
		}

		if (playerData.delHome(homeName)) {
			player.sendMessage(TextFormatting.YELLOW + "Removed home: " + TextFormatting.ORANGE + homeName);
		} else {
			player.sendMessage(TextFormatting.YELLOW + "You don't have a home named: " + TextFormatting.ORANGE + homeName);
			player.sendMessage(TextFormatting.YELLOW + "Use: " + TextFormatting.LIGHT_BLUE + "/sethome " + homeName + TextFormatting.YELLOW + " to create");
		}

		return 1;
	}

	private int playerHomeArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source     = (CommandSource) context.getSource();
		Player        player     = source.getSender();
		PlayerServer  target     = context.getArgument("player", PlayerServer.class);
		String        homeName   = context.getArgument("home", String.class);
		PlayerData    playerData = PlayerData.get(target);

		if (playerData.delHome(homeName)) {
			player.sendMessage(TextFormatting.YELLOW + "You have removed " + TextFormatting.RESET + target.getDisplayName() + TextFormatting.RESET + TextFormatting.YELLOW + "'s home: " + TextFormatting.LIGHT_BLUE + homeName);
		} else {
			player.sendMessage(target.getDisplayName() + TextFormatting.RESET + TextFormatting.YELLOW + " does not have a home named: " + TextFormatting.LIGHT_BLUE + homeName);
		}

		return 1;
	}
}
