package wyspr.BTAEssentials.commands.home;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.arguments.ArgumentTypeEntity;
import wyspr.BTAEssentials.BTAEssentials;
import wyspr.BTAEssentials.utils.PlayerData;

import java.util.List;

@SuppressWarnings("ALL") public class HomesCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("homes")
			.requires(source -> ((CommandSource) source).hasAdmin() || BTAEssentials.HomeCommand)
			.executes(context -> {
				CommandSource source     = (CommandSource) context.getSource();
				boolean       isAdmin    = source.hasAdmin();
				Player        player     = source.getSender();
				PlayerData    playerData = PlayerData.get(player);
				List<String>  homes      = playerData.getHomesList();

				if (homes.isEmpty()) {
					player.sendMessage("§1You do not have any homes!");
					player.sendMessage("§1Set a home with: §3/sethome [name]");
					return 1;
				}

				String homesString = String.join(", ", homes);
				player.sendMessage("§1Homes: §4" + homesString);

				return 1;
			})
			.then(ArgumentBuilderRequired
				.argument("player", ArgumentTypeEntity.username())
				.requires(source -> ((CommandSource) source).hasAdmin())
				.executes(context -> {
					CommandSource source     = (CommandSource) context.getSource();
					Player        player     = source.getSender();
					Player        target     = context.getArgument("player", Player.class);
					PlayerData    playerData = PlayerData.get(target);
					List<String>  homes      = playerData.getHomesList();

					if (homes.isEmpty()) {
						player.sendMessage("§1" + target.nickname + " does not have any homes!");
						return 1;
					}

					String homesString = String.join(", ", homes);
					player.sendMessage("§1Homes: §4" + homesString);

					return 1;
				})));
	}
}
