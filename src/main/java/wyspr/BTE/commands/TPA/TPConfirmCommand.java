package wyspr.BTE.commands.TPA;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.PlayerServer;
import org.apache.commons.lang3.tuple.Pair;
import wyspr.BTE.Essentials;
import wyspr.BTE.commands.arguments.ArgumentTypeOnlineUser;
import wyspr.BTE.utils.PlayerData;
import wyspr.BTE.utils.PlayerData.TPManager;
import wyspr.BTE.utils.TPARequestType;
import wyspr.BTE.utils.Teleport;
import wyspr.BTE.utils.Utils;

@SuppressWarnings("ALL")
public class TPConfirmCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {

		String[] literals = {"tpyes", "ty", "tpconfirm"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.TPACommand)
				.executes(this::noArg)
				.then(ArgumentBuilderRequired
					.argument("target", ArgumentTypeOnlineUser.online())
					.executes(this::playerArg)));
		}

	}

	private int noArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source    = (CommandSource) context.getSource();
		boolean       isAdmin   = source.hasAdmin();
		Player        player    = Utils.requirePlayer(source);
		TPManager     playerTPM = PlayerData.get(player).tpManager;

		if (playerTPM.hasNoRequests()) {
			player.sendMessage(TextFormatting.YELLOW + "You don't have any requests.");
			return 1;
		}

		Pair<String, TPARequestType> requestPair    = playerTPM.getNewestRequest();
		String                       targetUsername = requestPair.getKey();
		TPARequestType               request        = requestPair.getValue();

		Player target = (Player) MinecraftServer.getInstance().playerList.getPlayerEntity(targetUsername);
		if (target == null) {
			player.sendMessage(TextFormatting.LIGHT_BLUE + targetUsername + TextFormatting.ORANGE + " is offline.");
			return 1;
		}

		PlayerData targetData     = PlayerData.get(target);
		boolean    targetNotAdmin = !((PlayerServer) target).isOperator();

		int cost = Essentials.TPACost;
		if (target.score < cost && targetNotAdmin) {
			target.sendMessage(TextFormatting.YELLOW + "You do not have enough points to use TPA!");
			target.sendMessage(TextFormatting.YELLOW + "You need " + TextFormatting.ORANGE + (cost - target.score) + TextFormatting.YELLOW + " more points!");
			player.sendMessage(TextFormatting.YELLOW + "" + targetUsername + " does not have enough points to use TPA");
			return 1;
		}

		boolean didTeleport = false;

		if (request == TPARequestType.TPA) {
			targetData.tpManager.updateBackPos();
			didTeleport = Teleport.teleport(target, player);
			if (didTeleport) {
				target.sendMessage(TextFormatting.ORANGE + "Teleported to " + player.getDisplayName());
			}
		} else if (request == TPARequestType.TPAHERE) {
			playerTPM.updateBackPos();
			didTeleport = Teleport.teleport(player, target);
			if (didTeleport) {
				target.sendMessage(TextFormatting.ORANGE + "Teleported " + player.getDisplayName() + " to you");
				player.sendMessage(TextFormatting.ORANGE + "Teleported to " + target.getDisplayName());
			}
		}

		if (didTeleport) {
			playerTPM.removeRequest(target.username);
			if (targetNotAdmin) {
				target.score -= Essentials.TPACost;
			}
		}

		return 1;
	}

	private int playerArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source         = (CommandSource) context.getSource();
		boolean       isAdmin        = source.hasAdmin();
		Player        player         = Utils.requirePlayer(source);
		PlayerServer  target         = context.getArgument("target", PlayerServer.class);
		PlayerData    targetData     = PlayerData.get(target);
		TPManager     playerTPM      = PlayerData.get(player).tpManager;
		boolean       targetNotAdmin = !target.isOperator();

		if (playerTPM.hasNoRequests()) {
			player.sendMessage(TextFormatting.YELLOW + "You don't have any requests.");
			return 1;
		}

		if (!playerTPM.hasRequestFrom(target.username)) {
			player.sendMessage(TextFormatting.ORANGE + "You don't have a request from " + TextFormatting.YELLOW + target);
			return 1;
		}

		TPARequestType request = playerTPM.getRequest(target.username);

		int cost = Essentials.TPACost;
		if (target.score < cost && targetNotAdmin) {
			target.sendMessage(TextFormatting.YELLOW + "You do not have enough points to use TPA!");
			target.sendMessage(TextFormatting.YELLOW + "You need " + TextFormatting.ORANGE + (cost - target.score) + TextFormatting.YELLOW + " more points!");
			player.sendMessage(TextFormatting.YELLOW + "" + target.username + " does not have enough points to use TPA");
			return 1;
		}

		boolean didTeleport = false;

		if (request == TPARequestType.TPA) {
			targetData.tpManager.updateBackPos();
			didTeleport = Teleport.teleport(target, player);
			if (didTeleport) {
				target.sendMessage(TextFormatting.ORANGE + "Teleported to " + player.getDisplayName());
			}
		} else if (request == TPARequestType.TPAHERE) {
			playerTPM.updateBackPos();
			didTeleport = Teleport.teleport(player, target);
			if (didTeleport) {
				target.sendMessage(TextFormatting.ORANGE + "Teleported " + player.getDisplayName() + " to you");
				player.sendMessage(TextFormatting.ORANGE + "Teleported to " + target.getDisplayName());
			}
		}

		if (didTeleport) {
			playerTPM.removeRequest(target.username);
			if (targetNotAdmin) {
				target.score -= Essentials.TPACost;
			}
		}

		return 1;
	}
}
