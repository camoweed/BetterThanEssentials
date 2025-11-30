package wyspr.BTAEssentials.commands.TPA;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.arguments.ArgumentTypeEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.PlayerServer;
import org.apache.commons.lang3.tuple.Pair;
import wyspr.BTAEssentials.BTAEssentials;
import wyspr.BTAEssentials.utils.PlayerData;
import wyspr.BTAEssentials.utils.TPARequestType;
import wyspr.BTAEssentials.utils.Teleport;

@SuppressWarnings("ALL") public class TPConfirmCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		CommandNode<Object> command
			= commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("tpconfirm")
			.requires(source -> ((CommandSource) source).hasAdmin() || BTAEssentials.TPACommand)
			.executes(context -> {
				CommandSource source = (CommandSource) context.getSource();
				boolean isAdmin = source.hasAdmin();
				Player player = source.getSender();
				PlayerData playerData = PlayerData.get(player);

				if (playerData.hasNoRequests()) {
					player.sendMessage("§4You don't have any requests.");
					return 1;
				}

				Pair<String, TPARequestType> requestPair = playerData.getNewestRequest();
				String targetUsername = requestPair.getKey();
				TPARequestType request = requestPair.getValue();

				Player target = (Player) MinecraftServer.getInstance().playerList.getPlayerEntity(
					targetUsername);
				PlayerData targetData = PlayerData.get(target);
				boolean targetNotAdmin = !((PlayerServer) target).isOperator();

				int cost = BTAEssentials.TPACost;
				if (target.score < cost && targetNotAdmin) {
					target.sendMessage("§4You do not have enough points to use TPA!");
					target.sendMessage("§4You need §1" + (cost - target.score) + "§4 more points!");
					player.sendMessage("§4" + targetUsername + " does not have enough points to use TPA");
					return 1;
				}

				boolean didTeleport = false;

				if (request == TPARequestType.TPA) {
					targetData.updateBackPos();
					didTeleport = Teleport.teleport(target, player);
					if (didTeleport) {
						target.sendMessage("§1Teleported to " + player.getDisplayName());
					}
				} else if (request == TPARequestType.TPAHERE) {
					playerData.updateBackPos();
					didTeleport = Teleport.teleport(player, target);
					if (didTeleport) {
						target.sendMessage("§1Teleported " + player.getDisplayName() + " to you");
						player.sendMessage("§1Teleported to " + target.getDisplayName());
					}
				}

				if (didTeleport) {
					playerData.removeRequest(target.username);
					if (targetNotAdmin) {
						target.score -= BTAEssentials.TPACost;
					}
				}

				return 1;
			})
			.then(ArgumentBuilderRequired.argument("target", ArgumentTypeEntity.username()))
			.executes(context -> {
				CommandSource source = (CommandSource) context.getSource();
				boolean isAdmin = source.hasAdmin();
				Player target = context.getArgument("target", Player.class);
				Player player = source.getSender();
				PlayerData targetData = PlayerData.get(target);
				PlayerData playerData = PlayerData.get(player);
				boolean targetNotAdmin = !((PlayerServer) target).isOperator();

				if (playerData.hasNoRequests()) {
					player.sendMessage("§4You don't have any requests.");
					return 1;
				}

				if (!playerData.hasRequestFrom(target.username)) {
					player.sendMessage("§1You don't have a request from §4" + target);
					return 1;
				}

				TPARequestType request = playerData.getRequest(target.username);

				int cost = BTAEssentials.TPACost;
				if (target.score < cost) {
					target.sendMessage("§4You do not have enough points to use TPA!");
					target.sendMessage("§4You need §1" + (cost - target.score) + "§4 more points!");
					player.sendMessage("§4" + target.username + " does not have enough points to use TPA");
					return 1;
				}

				boolean didTeleport = false;

				if (request == TPARequestType.TPA) {
					targetData.updateBackPos();
					didTeleport = Teleport.teleport(target, player);
					if (didTeleport) {
						target.sendMessage("§1Teleported to " + player.getDisplayName());
					}
				} else if (request == TPARequestType.TPAHERE) {
					playerData.updateBackPos();
					didTeleport = Teleport.teleport(player, target);
					if (didTeleport) {
						target.sendMessage("§1Teleported " + player.getDisplayName() + " to you");
						player.sendMessage("§1Teleported to " + target.getDisplayName());
					}
				}

				if (didTeleport) {
					playerData.removeRequest(target.username);
					if (targetNotAdmin) {
						target.score -= BTAEssentials.TPACost;
					}
				}

				return 1;
			}));

		String[] literals = {"tpyes", "ty"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.redirect(command));
		}

	}
}
