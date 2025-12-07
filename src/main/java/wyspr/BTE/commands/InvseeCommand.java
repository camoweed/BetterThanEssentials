package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.server.entity.player.PlayerServer;
import wyspr.BTE.commands.arguments.ArgumentTypeUser;
import wyspr.BTE.utils.ContainerInvsee;

@SuppressWarnings("ALL") public class InvseeCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		String[] literals = {"invsee", "openinv"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin())
				.then(ArgumentBuilderRequired
					.argument("target", ArgumentTypeUser.user())
					.executes(context -> {
						CommandSource source = (CommandSource) context.getSource();
						PlayerServer  target = context.getArgument("target", PlayerServer.class);
						Player        player = source.getSender();
						player.displayContainerScreen(new ContainerInvsee(target));

						return 1;
					})));
		}
	}
}
