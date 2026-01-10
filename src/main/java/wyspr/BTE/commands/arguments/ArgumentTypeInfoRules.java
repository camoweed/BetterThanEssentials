package wyspr.BTE.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import wyspr.BTE.Essentials;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ArgumentTypeInfoRules implements ArgumentType<String> {
	private final File      filesDir;
	private final InfoRules type;

	private ArgumentTypeInfoRules(InfoRules type) {
		this.type     = type;
		this.filesDir = Essentials.DATA_DIR
			.resolve(type
				.toString()
				.toLowerCase())
			.toFile();
	}

	public static ArgumentTypeInfoRules info() {
		return new ArgumentTypeInfoRules(InfoRules.Info);
	}

	public static ArgumentTypeInfoRules rules() {
		return new ArgumentTypeInfoRules(InfoRules.Rules);
	}

	@Override
	public String parse(StringReader stringReader) throws CommandSyntaxException {
		final String input = stringReader.readString();

		for (String file : getDataFiles()) {
			if (file.equalsIgnoreCase(input)) {
				return input;
			}
		}
		throw new CommandSyntaxException(
			CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(),
			() -> "Failed to find page: " + input
		);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(
		CommandContext<S> context,
		SuggestionsBuilder builder
	)
	{
		for (String file : getDataFiles()) {
			if (file.startsWith(builder.getRemaining())) {
				builder.suggest(file);
			}
		}

		return builder.buildFuture();
	}

	private List<String> getDataFiles() {
		return Arrays
			.stream(filesDir.list((dir, name) -> {
				if (Objects.equals(name, type + ".txt")) return false;
				return name.startsWith(type.toString()) && name.endsWith(".txt");
			}))
			.map(s -> s.substring(4, s.length() - 4))
			.collect(Collectors.toList());
	}

	private enum InfoRules {
		Info, Rules
	}
}
