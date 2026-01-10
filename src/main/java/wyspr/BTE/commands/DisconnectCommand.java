package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.server.entity.player.PlayerServer;

@SuppressWarnings("ALL")
public class DisconnectCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		String[] literals = {"disconnect", "kickself"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.executes(context -> {
					CommandSource source = (CommandSource) context.getSource();
					PlayerServer  player = (PlayerServer) source.getSender();
					player.playerNetServerHandler.kickPlayer("You have disconnected.");
					return 1;
				}));
		}
	}
}
