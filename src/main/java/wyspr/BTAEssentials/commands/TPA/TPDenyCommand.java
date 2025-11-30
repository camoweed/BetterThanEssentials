package wyspr.BTAEssentials.commands.TPA;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.arguments.ArgumentTypeEntity;
import net.minecraft.server.entity.player.PlayerServer;
import org.apache.commons.lang3.tuple.Pair;
import wyspr.BTAEssentials.BTAEssentials;
import wyspr.BTAEssentials.utils.PlayerData;
import wyspr.BTAEssentials.utils.TPARequestType;

@SuppressWarnings("ALL") public class TPDenyCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		CommandNode<Object> command
			= commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("tpdeny")
			.requires(source -> ((CommandSource) source).hasAdmin() || BTAEssentials.TPACommand)
			.executes(context -> {
				CommandSource source     = (CommandSource) context.getSource();
				boolean       isAdmin    = source.hasAdmin();
				Player        player     = source.getSender();
				PlayerData    playerData = PlayerData.get(player);

				if (playerData.hasNoRequests()) {
					player.sendMessage("§4You don't have any requests.");
					return 1;
				}

				Pair<String, TPARequestType> requestPair    = playerData.getNewestRequest();
				String                       targetUsername = requestPair.getKey();

				playerData.removeRequest(targetUsername);

				player.sendMessage("§1Denied TP request from §4" + targetUsername);

				return 1;
			})
			.then(ArgumentBuilderRequired.argument("target", ArgumentTypeEntity.username()))
			.executes(context -> {
				CommandSource source         = (CommandSource) context.getSource();
				boolean       isAdmin        = source.hasAdmin();
				Player        target         = context.getArgument("target", Player.class);
				Player        player         = source.getSender();
				PlayerData    targetData     = PlayerData.get(target);
				PlayerData    playerData     = PlayerData.get(player);
				boolean       targetNotAdmin = !((PlayerServer) target).isOperator();

				if (playerData.hasNoRequests()) {
					player.sendMessage("§4You don't have any requests.");
					return 1;
				}

				if (!playerData.hasRequestFrom(target.username)) {
					player.sendMessage("§1You don't have a request from §4" + target);
					return 1;
				}

				playerData.removeRequest(target.username);

				player.sendMessage("§1Denied TP request from §4" + target.username);

				return 1;
			}));

		String[] literals = {"tpno", "tn"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.redirect(command));
		}

	}
}
