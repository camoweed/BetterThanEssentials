package wyspr.BTAEssentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeInteger;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import wyspr.BTAEssentials.BTAEssentials;

@SuppressWarnings("ALL") public class InfoCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) (ArgumentBuilderLiteral
			.literal("info")
			.executes(context -> {
				CommandSource source = (CommandSource) context.getSource();
				Player player = source.getSender();

				for (String line : BTAEssentials.info.get(1)) player.sendMessage(line);

				return 1;
			})).then(ArgumentBuilderRequired
			.argument("page", ArgumentTypeInteger.integer(1))
			.executes(context -> {
				CommandSource source = (CommandSource) context.getSource();
				Player player = source.getSender();
				int page = context.getArgument("page", Integer.class);

				for (String line : BTAEssentials.info.get(page)) player.sendMessage(line);

				return 1;
			})));
	}
}
