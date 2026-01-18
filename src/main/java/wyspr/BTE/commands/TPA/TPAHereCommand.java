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
import net.minecraft.server.entity.player.PlayerServer;
import wyspr.BTE.Essentials;
import wyspr.BTE.commands.arguments.ArgumentTypeOnlineUser;
import wyspr.BTE.utils.PlayerData;
import wyspr.BTE.utils.TPARequestType;
import wyspr.BTE.utils.Utils;

@SuppressWarnings("ALL")
public class TPAHereCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		String[] literals = {"tphere", "tph", "tpahere"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.TPACommand)
				.then(ArgumentBuilderRequired
					.argument("player", ArgumentTypeOnlineUser.online())
					.executes(this::exec)));
		}
	}

	private int exec(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source     = (CommandSource) context.getSource();
		boolean       isAdmin    = source.hasAdmin();
		PlayerServer  target     = context.getArgument("player", PlayerServer.class);
		Player        player     = Utils.requirePlayer(source);
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
			TPARequestType.TPAHERE
		);

		if (isOnlyRequest) {
			player.sendMessage(TextFormatting.YELLOW + "Sent a request to " + target.getDisplayName());
			target.world.playSoundAtEntity(null, target, "note.celesta", 1, 2);
			target.sendMessage(TextFormatting.YELLOW + "" + player.username + TextFormatting.ORANGE + " has sent you a request to teleport to them.");
			target.sendMessage(TextFormatting.LIME + "/tpyes " + TextFormatting.ORANGE + "to accept, " + TextFormatting.RED + "/tpno " + TextFormatting.ORANGE + "to deny.");
		} else {
			player.sendMessage(TextFormatting.YELLOW + "You already have a pending request for " + target);
		}

		return 1;
	}
}
