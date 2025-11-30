package wyspr.BTAEssentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeString;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.net.PlayerList;

@SuppressWarnings("ALL") public class OPChatCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		CommandNode<Object> command
			= commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("")
			.requires(source -> ((CommandSource) source).hasAdmin() || ((CommandSource) source).getSender().username == "_wyspr")
			.then(ArgumentBuilderRequired
				.argument("message", ArgumentTypeString.greedyString())
				.executes(context -> {
					CommandSource source     = (CommandSource) context.getSource();
					Player        player     = source.getSender();
					String        message    = context.getArgument("message", String.class);
					String        opChat     = opChat(player, message);
					PlayerList    playerList = MinecraftServer.getInstance().playerList;
					playerList.sendChatMessageToPlayer("_wyspr", opChat);
					playerList.sendChatMessageToAllOps(opChat);
					return 1;
				})));

		String[] literals = {"chatop", "opc", "opchat"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.redirect(command));
		}
	}

	public String opChat(Player player, String message) {
		return "[§e§lOP CHAT§r] <" + player.username + "§r> " + message.replace("$$", "§");
	}
}
