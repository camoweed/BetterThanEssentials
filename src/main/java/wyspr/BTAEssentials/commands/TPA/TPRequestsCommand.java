package wyspr.BTAEssentials.commands.TPA;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import wyspr.BTAEssentials.BTAEssentials;
import wyspr.BTAEssentials.utils.PlayerData;

@SuppressWarnings("ALL") public class TPRequestsCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {

		CommandNode<Object> command
			= commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("tprequests")
			.requires(source -> ((CommandSource) source).hasAdmin() || BTAEssentials.TPACommand)
			.executes(context -> {
				CommandSource source     = (CommandSource) context.getSource();
				boolean       isAdmin    = source.hasAdmin();
				Player        player     = source.getSender();
				PlayerData    playerData = PlayerData.get(player);

				String requests = String.join(", ", playerData.getAllRequests());

				player.sendMessage("§1Current TP requests: " + requests);

				return 1;
			}));

		String[] literals = {"tpreq", "tpr"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.redirect(command));
		}
	}
}
//
//	public boolean execute(CommandHandler handler, CommandSender sender, String[] args) {
//		EntityPlayer p = sender.getPlayer();
//		PlayerData.TPInfo tpInfo  = PlayerData.get(p).tpInfo();
//		if (tpInfo.hasNoRequests()) {
//			sender.sendMessage("§4You don't have any requests.");
//			return true;
//		}
//		String requests = String.join(", ", tpInfo.getAllRequests());
//
//		sender.sendMessage("§1Current TP requests: " + requests);
//
//		return true;
//	}
//}
