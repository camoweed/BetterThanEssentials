package wyspr.BTE.commands.TPA;

import com.mojang.brigadier.CommandDispatcher;
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
import org.apache.commons.lang3.tuple.Pair;
import wyspr.BTE.Essentials;
import wyspr.BTE.commands.arguments.ArgumentTypeUser;
import wyspr.BTE.utils.PlayerData;
import wyspr.BTE.utils.TPARequestType;

@SuppressWarnings("ALL") public class TPDenyCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		String[] literals = {"tpno", "tn", "tpdeny"};
		for (String literal : literals) {
			CommandNode<Object> command
				= commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.TPACommand)
				.executes(this::noArg)
				.then(ArgumentBuilderRequired
					.argument("target", ArgumentTypeUser.user())
					.executes(this::playerArg)));
		}
	}

	private int noArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source     = (CommandSource) context.getSource();
		boolean       isAdmin    = source.hasAdmin();
		Player        player     = source.getSender();
		PlayerData    playerData = PlayerData.get(player);

		if (playerData.hasNoRequests()) {
			player.sendMessage(TextFormatting.YELLOW + "You don't have any requests.");
			return 1;
		}

		Pair<String, TPARequestType> requestPair    = playerData.getNewestRequest();
		String                       targetUsername = requestPair.getKey();

		playerData.removeRequest(targetUsername);

		player.sendMessage(TextFormatting.ORANGE + "Denied TP request from " + TextFormatting.YELLOW + targetUsername);

		return 1;
	}

	private int playerArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source         = (CommandSource) context.getSource();
		boolean       isAdmin        = source.hasAdmin();
		Player        target         = context.getArgument("target", Player.class);
		Player        player         = source.getSender();
		PlayerData    targetData     = PlayerData.get(target);
		PlayerData    playerData     = PlayerData.get(player);
		boolean       targetNotAdmin = !((PlayerServer) target).isOperator();

		if (playerData.hasNoRequests()) {
			player.sendMessage(TextFormatting.YELLOW + "You don't have any requests.");
			return 1;
		}

		if (!playerData.hasRequestFrom(target.username)) {
			player.sendMessage(TextFormatting.ORANGE + "You don't have a request from " + TextFormatting.YELLOW + target);
			return 1;
		}

		playerData.removeRequest(target.username);

		player.sendMessage(TextFormatting.ORANGE + "Denied TP request from " + TextFormatting.YELLOW + target.username);

		return 1;
	}
}
