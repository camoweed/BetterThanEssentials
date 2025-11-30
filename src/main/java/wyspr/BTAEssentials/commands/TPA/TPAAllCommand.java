package wyspr.BTAEssentials.commands.TPA;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.PlayerServer;
import wyspr.BTAEssentials.utils.PlayerData;
import wyspr.BTAEssentials.utils.TPARequestType;

import java.util.List;

@SuppressWarnings("ALL") public class TPAAllCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		CommandNode<Object> command
			= commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("tpall")
			.requires(source -> ((CommandSource) source).hasAdmin())
			.executes(context -> {
				CommandSource source     = (CommandSource) context.getSource();
				Player        player     = source.getSender();
				PlayerData    playerData = PlayerData.get(player);

				List<PlayerServer> players = MinecraftServer.getInstance().playerList.playerEntities;

				for (PlayerServer targetPlayer : players) {
					PlayerData targetData = PlayerData.get((Player) targetPlayer);
					targetData.sendTPARequest(player.username, TPARequestType.TPAHERE);
					targetPlayer.sendMessage("§4" + player.username + "§1 has sent you a request to teleport to them.");
					targetPlayer.sendMessage("§5/tpyes §1to accept, §e/tpno §1to deny.");
				}

				player.sendMessage("§4Sent a request to all players.");

				return 1;
			}));
		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("tpaall")
			.redirect(command));
	}
}
//import wyspr.utils.PlayerData;
//import wyspr.utils.PlayerData.TPInfo.RequestType;
//import net.minecraft.core.net.command.Command;
//import net.minecraft.core.net.command.CommandHandler;
//import net.minecraft.core.net.command.CommandSender;
//import net.minecraft.server.entity.player.EntityPlayerMP;
//
//import java.util.List;
//import java.util.Objects;
//
//
//public class TPAAllCommand extends Command {
//	public TPAAllCommand() {
//		super("tpaall", "tpall");
//	}
//
//	public boolean opRequired(String[] args) {
//		return true;
//	}
//
//	public void sendCommandSyntax(CommandHandler handler, CommandSender sender) {
//		sender.sendMessage("§3/tpall");
//		sender.sendMessage("§5Request all players to teleport to you");
//	}
//
//	public boolean execute(CommandHandler handler, CommandSender sender, String[] args) {
//		List<EntityPlayerMP> players = handler.asServer().minecraftServer.playerList.playerEntities;
//		String destUser = sender.getPlayer().username;
//
//		for (EntityPlayerMP p : players) {
//			if (Objects.equals(p.username, destUser)) continue;
//
//			PlayerData playerData = PlayerData.get(p);
//			boolean isOnlyRequest = playerData.sendTPARequest(destUser, RequestType.TPAHERE);
//			if (isOnlyRequest) {
//				p.addChatMessage("§4" + destUser + "§1 has sent you a request to teleport to them.");
//				p.addChatMessage("§5/tpyes §1to accept, §e/tpno §1to deny.");
//			}
//		}
//		sender.sendMessage("§4Sent a request to all players.");
//
//		return true;
//	}
//}
