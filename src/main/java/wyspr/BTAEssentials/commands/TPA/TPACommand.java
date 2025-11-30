package wyspr.BTAEssentials.commands.TPA;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.arguments.ArgumentTypeEntity;
import wyspr.BTAEssentials.BTAEssentials;
import wyspr.BTAEssentials.utils.PlayerData;
import wyspr.BTAEssentials.utils.TPARequestType;

@SuppressWarnings("ALL") public class TPACommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {

		CommandNode<Object> command
			= commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("tpask")
			.requires(source -> ((CommandSource) source).hasAdmin() || BTAEssentials.TPACommand)
			.then(ArgumentBuilderRequired.argument("target", ArgumentTypeEntity.username()))
			.executes(context -> {
				CommandSource source     = (CommandSource) context.getSource();
				boolean       isAdmin    = source.hasAdmin();
				Player        target     = context.getArgument("target", Player.class);
				Player        player     = source.getSender();
				PlayerData    targetData = PlayerData.get(target);
				PlayerData    playerData = PlayerData.get(player);

				int cost = BTAEssentials.TPACost;
				if (player.score < cost && !isAdmin) {
					player.sendMessage("§4You do not have enough points to use this command!");
					player.sendMessage("§4You need §1" + (cost - player.score) + "§4 more points!");
					return 1;
				}

				boolean isOnlyRequest = targetData.sendTPARequest(player.username, TPARequestType.TPA);

				if (isOnlyRequest) {
					player.sendMessage("§4Sent a request to " + target.getDisplayName());
					target.sendMessage("§4" + player.username + "§1 has sent you a TP request.");
					target.sendMessage("§5/tpyes §1to accept, §e/tpno §1to deny.");
				} else {
					player.sendMessage("§4You already have a pending request for " + target);
				}

				return 1;
			}));

		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("tpa")
			.redirect(command));
	}
}
