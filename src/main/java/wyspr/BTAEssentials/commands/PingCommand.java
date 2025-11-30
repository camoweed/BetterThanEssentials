package wyspr.BTAEssentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;

@SuppressWarnings("ALL") public class PingCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register(
			(ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal("ping")
				.executes(context -> {
					CommandSource source = (CommandSource) context.getSource();
					source.sendMessage("Pong!");
					return 1;
				}));
	}
}
