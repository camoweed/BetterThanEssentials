package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeString;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.net.PlayerList;

@SuppressWarnings("ALL") public class OPChatCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		String[] literals = {"chatop", "opc", "opchat"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.<CommandSource>literal(literal)
				.requires(CommandSource::hasAdmin)
				.then(ArgumentBuilderRequired
					.<CommandSource, String>argument("message", ArgumentTypeString.greedyString())
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
		}
	}

	public String opChat(Player player, String message) {
		return "[" + TextFormatting.RED + TextFormatting.BOLD + "OP CHAT" + TextFormatting.RESET + "] <" + player.username + TextFormatting.RESET + "> " + message.replace("$$", "§");
	}
}
