package wyspr.BTE.commands.home;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeString;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import wyspr.BTE.Essentials;
import wyspr.BTE.utils.PlayerData;
import wyspr.BTE.utils.Utils;

@SuppressWarnings("ALL")
public class SethomeCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		String[] literals = {"addhome", "sethome"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.HomeCommand)
				.executes(this::noArg)
				.then(ArgumentBuilderRequired
					.argument("homeName", ArgumentTypeString.string())
					.executes(this::homeArg)));
		}
	}

	private int noArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source      = (CommandSource) context.getSource();
		boolean       isAdmin     = source.hasAdmin();
		Player        player      = Utils.requirePlayer(source);
		PlayerData    playerData  = PlayerData.get(player);
		int           homesAmount = playerData.homes.getHomesAmount();

		return setHome(homesAmount, player, playerData, "home");
	}

	private int homeArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source      = (CommandSource) context.getSource();
		boolean       isAdmin     = source.hasAdmin();
		Player        player      = Utils.requirePlayer(source);
		PlayerData    playerData  = PlayerData.get(player);
		int           homesAmount = playerData.homes.getHomesAmount();
		String        homeName    = context.getArgument("homeName", String.class);

		if (homeName.equals("bed")) {
			player.sendMessage(TextFormatting.YELLOW + "This home is reserved for your bed.");
			return 1;
		}

		return setHome(homesAmount, player, playerData, homeName);
	}

	private static Integer setHome(
		int homesAmount, Player player, PlayerData playerData, String
			homeName
	)
	{
		if (homesAmount == Essentials.MaxHomes) {
			player.sendMessage(TextFormatting.YELLOW + "You've reached the max amount of homes!");
			return 1;
		}

		if (playerData.homes.setHome(player, homeName)) {
			player.sendMessage(TextFormatting.YELLOW + "Created home: " + TextFormatting.ORANGE + homeName);
		} else {
			player.sendMessage(TextFormatting.YELLOW + "You already have a home named: " + TextFormatting.ORANGE + homeName);
			player.sendMessage(TextFormatting.YELLOW + "Use: " + TextFormatting.LIGHT_BLUE + "/delhome " + homeName + TextFormatting.YELLOW + " to remove");
		}
		return 1;
	}
}
