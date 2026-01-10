package wyspr.BTE.commands.TPA;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.server.entity.player.PlayerServer;
import wyspr.BTE.Essentials;
import wyspr.BTE.commands.arguments.ArgumentTypeOnlineUser;
import wyspr.BTE.utils.PlayerData;
import wyspr.BTE.utils.TPARequestType;

import static wyspr.BTE.utils.Utils.playNotificationAtPlayer;

@SuppressWarnings("ALL")
public class TPACommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {

		String[] literals = {"tpa", "tpask"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.TPACommand)
				.then(ArgumentBuilderRequired
					.argument("target", ArgumentTypeOnlineUser.online())
					.executes(this::exec)));
		}
	}

	private int exec(CommandContext<Object> context) {
		CommandSource source     = (CommandSource) context.getSource();
		boolean       isAdmin    = source.hasAdmin();
		PlayerServer  target     = context.getArgument("target", PlayerServer.class);
		Player        player     = source.getSender();
		PlayerData    targetData = PlayerData.get(target);
		PlayerData    playerData = PlayerData.get(player);

		int cost = Essentials.TPACost;
		if (player.score < cost && !isAdmin) {
			player.sendMessage(TextFormatting.YELLOW + "You do not have enough points to use this command!");
			player.sendMessage(TextFormatting.YELLOW + "You need " + TextFormatting.ORANGE + (cost - player.score) + TextFormatting.YELLOW + " more points!");
			return 1;
		}

		boolean isOnlyRequest = targetData.tpManager.sendTPARequest(
			player.username,
			TPARequestType.TPA
		);

		if (isOnlyRequest) {
			player.sendMessage(TextFormatting.YELLOW + "Sent a request to " + target.username);

			playNotificationAtPlayer(target, Essentials.TPANotificationSound);
			target.sendMessage(TextFormatting.YELLOW + "" + player.username + TextFormatting.ORANGE + " has sent you a TP request.");
			target.sendMessage(TextFormatting.LIME + "/tpyes " + TextFormatting.ORANGE + "to accept, " + TextFormatting.RED + "/tpno " + TextFormatting.ORANGE + "to deny.");
		} else {
			player.sendMessage(TextFormatting.YELLOW + "You already have a pending request for " + target.username);
		}

		return 1;
	}
}
