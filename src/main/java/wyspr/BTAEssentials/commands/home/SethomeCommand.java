package wyspr.BTAEssentials.commands.home;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeString;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import wyspr.BTAEssentials.BTAEssentials;
import wyspr.BTAEssentials.utils.PlayerData;

@SuppressWarnings("ALL") public class SethomeCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		CommandNode<Object> command
			= commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("sethome")
			.requires(source -> ((CommandSource) source).hasAdmin() || BTAEssentials.HomeCommand)
			.executes(context -> {
				// NO ARG
				CommandSource source      = (CommandSource) context.getSource();
				boolean       isAdmin     = source.hasAdmin();
				Player        player      = source.getSender();
				PlayerData    playerData  = PlayerData.get(player);
				int           homesAmount = playerData.getHomesAmount();

				return setHome(homesAmount, player, playerData, "home");
			})
			.then(ArgumentBuilderRequired
				.argument("homeName", ArgumentTypeString.string())
				.executes(context -> {
					CommandSource source      = (CommandSource) context.getSource();
					boolean       isAdmin     = source.hasAdmin();
					Player        player      = source.getSender();
					PlayerData    playerData  = PlayerData.get(player);
					int           homesAmount = playerData.getHomesAmount();
					String        homeName    = context.getArgument("homeName", String.class);

					if (homeName.equals("bed")) {
						player.sendMessage("§4This home is reserved for your bed.");
						return 1;
					}

					return setHome(homesAmount, player, playerData, homeName);
				})));

		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("addhome")
			.redirect(command));
	}

	private static Integer setHome(int homesAmount, Player player, PlayerData playerData, String homeName)
	{
		if (homesAmount == BTAEssentials.MaxHomes) {
			player.sendMessage("§4You've reached the max amount of homes!");
			return 1;
		}

		if (playerData.setHome(player, homeName)) {
			player.sendMessage("§4Created home: §1" + homeName);
			playerData.save();
		} else {
			player.sendMessage("§4You already have a home named: §1" + homeName);
			player.sendMessage("§4Use: §3/delhome " + homeName + "§4 to remove");
		}
		return 1;
	}
}
