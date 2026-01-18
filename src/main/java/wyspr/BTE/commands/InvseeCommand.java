package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.server.entity.player.PlayerServer;
import wyspr.BTE.commands.arguments.ArgumentTypeOnlineUser;
import wyspr.BTE.utils.UI.InvseeContainer;
import wyspr.BTE.utils.Utils;

@SuppressWarnings("ALL")
public class InvseeCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		String[] literals = {"invsee", "openinv"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin())
				.then(ArgumentBuilderRequired
					.argument("target", ArgumentTypeOnlineUser.online())
					.executes(context -> {
						CommandSource source = (CommandSource) context.getSource();
						PlayerServer  target = context.getArgument("target", PlayerServer.class);
						Player        player = Utils.requirePlayer(source);
						player.displayContainerScreen(new InvseeContainer(target));

						return 1;
					})));
		}
	}
}
