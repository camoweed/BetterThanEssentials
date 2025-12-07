package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeInteger;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import wyspr.BTE.Essentials;

@SuppressWarnings("ALL") public class InfoCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) (ArgumentBuilderLiteral
			.literal("info")
			.executes(this::noArg)).then(ArgumentBuilderRequired
			.argument("page", ArgumentTypeInteger.integer(1))
			.executes(this::pageArg)));
	}

	private @NotNull int noArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source = (CommandSource) context.getSource();
		Player        player = source.getSender();

		for (String line : Essentials.info.get(1)) player.sendMessage(line);

		return 1;
	}

	private @NotNull int pageArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source = (CommandSource) context.getSource();
		Player        player = source.getSender();
		int           page   = context.getArgument("page", Integer.class);

		for (String line : Essentials.info.get(page)) player.sendMessage(line);

		return 1;
	}
}
