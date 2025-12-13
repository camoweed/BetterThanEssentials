package wyspr.BTE.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import wyspr.BTE.utils.Warps;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// Adapted from
// https://github.com/MelonModding/MelonUtilities/blob/main/src/main/java/MelonUtilities/command/arguments/ArgumentTypeWarp.java

public class ArgumentTypeWarp implements ArgumentType<String> {
	private static final List<String> EXAMPLES = Arrays.asList("market", "arena", "parkour");

	public ArgumentTypeWarp() {}

	public static ArgumentType<String> warp() {
		return new ArgumentTypeWarp();
	}

	public String parse(StringReader reader) throws CommandSyntaxException {
		final String string = reader.readString();

		List<String> warps  = Warps.getWarps();
		for (String warp : warps) {
			if (warp.equalsIgnoreCase(string)) {
				return warp;
			}
		}
		throw new CommandSyntaxException(
			CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(),
			() -> "Failed to find Warp: " + string + " (Warp Doesn't Exist)"
		);
	}

	public <S> CompletableFuture<Suggestions> listSuggestions(
		CommandContext<S> context,
		SuggestionsBuilder builder
	)
	{
		List<String> warps = Warps.getWarps();
		for (String warp : warps) {
			if (warp.startsWith(builder.getRemaining())) {
				builder.suggest(warp);
			}
		}

		return builder.buildFuture();
	}

	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
