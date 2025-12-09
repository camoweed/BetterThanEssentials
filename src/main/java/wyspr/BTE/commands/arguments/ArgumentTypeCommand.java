package wyspr.BTE.commands.arguments;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.command.ServerCommandSource;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ArgumentTypeCommand implements ArgumentType<String> {
	public ArgumentTypeCommand() {
	}

	public static ArgumentType<String> commands() {
		return new ArgumentTypeCommand();
	}

	@Override
	public String parse(StringReader reader) {
		String text = reader.getRemaining();
		reader.setCursor(reader.getTotalLength());
		return text;
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(
		CommandContext<S> context,
		SuggestionsBuilder builder
	)
	{
		PlayerServer target = context.getArgument("player", PlayerServer.class);
		ServerCommandSource playerCommandSource = new ServerCommandSource(
			MinecraftServer.getInstance(),
			target
		);
		CommandDispatcher<CommandSource> dispatcher = MinecraftServer
			.getInstance()
			.getDimensionWorld(target.dimension)
			.getCommandManager()
			.getDispatcher();

		String[] commands = dispatcher.getAllUsage(dispatcher.getRoot(), playerCommandSource, true);
		for (String command : commands) {
			String cmd = command.split(" ")[0];
			if (Objects.equals(cmd, "<target>")) continue;
			if (cmd.startsWith(builder.getRemainingLowerCase())) {
				builder.suggest(cmd);
			}
		}

		return builder.buildFuture();
	}
}
