package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeString;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.server.MinecraftServer;

@SuppressWarnings("ALL")
public class MOTDCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("motd")
			.executes(context -> {
				CommandSource source = (CommandSource) context.getSource();
				source.sendMessage(MinecraftServer.getInstance().motd);
				return 1;
			})
			.then(ArgumentBuilderLiteral
				.literal("set")
				.requires(source -> ((CommandSource) source).hasAdmin())
				.then(ArgumentBuilderRequired
					.argument("motd", ArgumentTypeString.greedyString())
					.executes(context -> {
						String newMOTD = context.getArgument("motd", String.class);
						MinecraftServer.getInstance().motd = newMOTD.replace("$$", "§");
						return 1;
					}))));
	}
}
