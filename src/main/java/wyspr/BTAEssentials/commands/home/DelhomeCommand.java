package wyspr.BTAEssentials.commands.home;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeString;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.arguments.ArgumentTypeEntity;
import wyspr.BTAEssentials.BTAEssentials;
import wyspr.BTAEssentials.commands.arguments.ArgumentTypeHome;
import wyspr.BTAEssentials.utils.PlayerData;

@SuppressWarnings("ALL") public class DelhomeCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		CommandNode<Object> command
			= commandDispatcher.register((ArgumentBuilderLiteral) (ArgumentBuilderLiteral.literal(
			"delhome"))
			.requires(source -> ((CommandSource) source).hasAdmin() || BTAEssentials.HomeCommand)
			.then(ArgumentBuilderRequired
				.argument("home", ArgumentTypeHome.home())
				.executes(context -> {
					CommandSource source     = (CommandSource) context.getSource();
					boolean       isAdmin    = source.hasAdmin();
					Player        player     = source.getSender();
					PlayerData    playerData = PlayerData.get(player);
					int           homes      = playerData.getHomesAmount();
					String        homeName   = context.getArgument("home", String.class);

					if (homes == 0) {
						player.sendMessage("§1You do not have any homes!");
						player.sendMessage("§1Set a home with: §3/sethome [name]");
						return 1;
					}

					if (homeName.equals("bed")) {
						player.sendMessage("§4This home is reserved for your bed.");
						return 1;
					}

					if (playerData.delHome(homeName)) {
						player.sendMessage("§4Removed home: §1" + homeName);
						playerData.save();
					} else {
						player.sendMessage("§4You don't have a home named: §1" + homeName);
						player.sendMessage("§4Use: §3/sethome " + homeName + "§4 to create");
					}

					return 1;
				}))
			.then(ArgumentBuilderRequired
				.argument("player", ArgumentTypeEntity.username())
				.requires(source -> ((CommandSource) source).hasAdmin())
				.then(ArgumentBuilderRequired.argument("home", ArgumentTypeString.string()))
				.executes(context -> {
					CommandSource source   = (CommandSource) context.getSource();
					Player        player   = source.getSender();
					Player        targetPlayer = context.getArgument("player", Player.class);
					String        homeName = context.getArgument("home", String.class);

					// TODO: Finish later

					return 1;
				}))

		);

		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("rmhome")
			.redirect(command));
	}
}
