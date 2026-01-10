package wyspr.BTE.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jetbrains.annotations.NotNull;
import wyspr.BTE.utils.AllUsersMap;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ArgumentTypeAllUsers implements ArgumentType<String> {
	private ArgumentTypeAllUsers() {}

	public static ArgumentType<String> all() {
		return new ArgumentTypeAllUsers();
	}

	public @NotNull String parse(StringReader reader) throws CommandSyntaxException {
		final String string  = reader.readString();
		Set<String>  players = AllUsersMap.getUsernames();

		for (String username : players) {
			if (username.equalsIgnoreCase(string)) {
				return username;
			}
		}
		throw new CommandSyntaxException(
			CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(),
			() -> "Failed to find User: " + string
		);
	}

	public <S> CompletableFuture<Suggestions> listSuggestions(
		CommandContext<S> context,
		SuggestionsBuilder builder
	)
	{
		Set<String> players = AllUsersMap.getUsernames();

		for (String username : players) {
			if (username.startsWith(builder.getRemaining())) {
				builder.suggest(username);
			}
		}

		return builder.buildFuture();
	}
}
