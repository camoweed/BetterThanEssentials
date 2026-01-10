package wyspr.BTE.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ArgumentTypeOnlineUser implements ArgumentType<PlayerServer> {
	public ArgumentTypeOnlineUser() {}

	public static ArgumentType<PlayerServer> online() {
		return new ArgumentTypeOnlineUser();
	}

	public @NotNull PlayerServer parse(StringReader reader) throws CommandSyntaxException {
		final String       string  = reader.readString();
		List<PlayerServer> players = MinecraftServer.getInstance().playerList.playerEntities;

		for (PlayerServer player : players) {
			if (player.username.equalsIgnoreCase(string)) {
				return player;
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
		MinecraftServer    server  = ((ServerCommandSource) context.getSource()).getServer();
		List<PlayerServer> players = server.playerList.playerEntities;

		for (PlayerServer player : players) {
			String username = player.username;
			if (username.startsWith(builder.getRemaining())) {
				builder.suggest(username);
			}
		}

		return builder.buildFuture();
	}
}
