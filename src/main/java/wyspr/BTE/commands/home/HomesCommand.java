package wyspr.BTE.commands.home;

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
import wyspr.BTE.Essentials;
import wyspr.BTE.commands.arguments.ArgumentTypeOnlineUser;
import wyspr.BTE.utils.PlayerData;
import wyspr.BTE.utils.Utils;

import java.util.List;

@SuppressWarnings("ALL")
public class HomesCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("homes")
			.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.HomeCommand)
			.executes(this::noArg)
			.then(ArgumentBuilderRequired
				.argument("player", ArgumentTypeOnlineUser.online())
				.requires(source -> ((CommandSource) source).hasAdmin())
				.executes(this::playerArg)));
	}

	private int noArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source     = (CommandSource) context.getSource();
		boolean       isAdmin    = source.hasAdmin();
		Player        player     = Utils.requirePlayer(source);
		PlayerData    playerData = PlayerData.get(player);
		List<String>  homes      = playerData.homes.getHomesList();

		if (homes.isEmpty()) {
			player.sendMessage(TextFormatting.ORANGE + "You do not have any homes!");
			player.sendMessage(TextFormatting.ORANGE + "Set a home with: §3/sethome [name]");
			return 1;
		}

		String homesString = String.join(", ", homes);
		player.sendMessage(TextFormatting.ORANGE + "Homes: " + TextFormatting.YELLOW + homesString);

		return 1;
	}

	private int playerArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source     = (CommandSource) context.getSource();
		Player        player     = Utils.requirePlayer(source);
		Player        target     = context.getArgument("player", PlayerServer.class);
		PlayerData    playerData = PlayerData.get(target);
		List<String>  homes      = playerData.homes.getHomesList();

		if (homes.isEmpty()) {
			player.sendMessage(TextFormatting.ORANGE + "" + target.nickname + " does not have any homes!");
			return 1;
		}

		String homesString = String.join(", ", homes);
		player.sendMessage(TextFormatting.ORANGE + "Homes: " + TextFormatting.YELLOW + homesString);

		return 1;
	}
}
