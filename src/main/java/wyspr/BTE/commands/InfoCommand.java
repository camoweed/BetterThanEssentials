package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import wyspr.BTE.Essentials;
import wyspr.BTE.commands.arguments.ArgumentTypeInfoRules;
import wyspr.BTE.utils.Utils;

@SuppressWarnings("ALL")
public class InfoCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("info")
			.executes(this::noArg)
			.then(ArgumentBuilderRequired
				.argument("page", ArgumentTypeInfoRules.info())
				.executes(this::pageArg)));
	}

	private @NotNull int noArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source = (CommandSource) context.getSource();
		Player        player = Utils.requirePlayer(source);

		for (String line : Essentials.info.get("")) player.sendMessage(line);

		return 1;
	}

	private @NotNull int pageArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source = (CommandSource) context.getSource();
		Player        player = Utils.requirePlayer(source);
		String        page   = context.getArgument("page", String.class);

		for (String line : Essentials.info.get(page)) player.sendMessage(line);

		return 1;
	}
}
