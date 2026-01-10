package wyspr.BTE.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.entity.player.Player;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.command.ServerCommandSource;
import wyspr.BTE.utils.PlayerData;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ArgumentTypeHome implements ArgumentType<String> {
	private static final List<String> EXAMPLES = Arrays.asList("home", "base", "mobspawner");
	private final        HomesType    type;

	private ArgumentTypeHome(HomesType type) {
		this.type = type;
	}

	public static ArgumentType<String> ownHomes() {
		return new ArgumentTypeHome(HomesType.OWN);
	}

	public static ArgumentType<String> othersHomes() {
		return new ArgumentTypeHome(HomesType.OTHERS);
	}

	public String parse(StringReader reader) throws CommandSyntaxException {
		return reader.readString();
	}

	public <S> String parse(StringReader reader, S source) throws CommandSyntaxException {
		final String input = reader.readString();
		List<String> homes;
		if (type == HomesType.OWN) {
			Player     sender     = ((ServerCommandSource) source).getSender();
			PlayerData playerData = PlayerData.get(sender);
			homes = playerData.homes.getHomesList();
		} else {
			return input;
		}

		for (String home : homes) {
			if (home.equalsIgnoreCase(input)) {
				return input;
			}
		}
		throw new CommandSyntaxException(
			CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(),
			() -> "Failed to find Home: " + input
		);
	}

	public <S> CompletableFuture<Suggestions> listSuggestions(
		CommandContext<S> context,
		SuggestionsBuilder builder
	)
	{
		Player     sender = ((ServerCommandSource) context.getSource()).getSender();
		PlayerData playerData;

		if (type == HomesType.OTHERS) {
			Player target = context.getArgument("player", PlayerServer.class);
			playerData = PlayerData.get(target);
		} else {
			playerData = PlayerData.get(sender);
		}

		List<String> homes = playerData.homes.getHomesList();

		for (String home : homes) {
			if (home.startsWith(builder.getRemaining()) || builder
				.getRemaining()
				.isEmpty()) {
				builder.suggest(home);
			}
		}

		return builder.buildFuture();
	}

	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	private enum HomesType {
		OWN, OTHERS
	}
}
