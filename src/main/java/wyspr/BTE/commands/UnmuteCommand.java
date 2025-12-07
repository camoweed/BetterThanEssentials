package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.server.entity.player.PlayerServer;
import wyspr.BTE.commands.arguments.ArgumentTypeUser;
import wyspr.BTE.utils.PlayerData;

@SuppressWarnings("ALL") public class UnmuteCommand implements CommandManager.CommandRegistry {

	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("unmute")
			.requires(source -> ((CommandSource) source).hasAdmin())
			.then(ArgumentBuilderRequired
				.argument("player", ArgumentTypeUser.user())
				.executes(context -> {
					CommandSource source = (CommandSource) context.getSource();
					Player        player = source.getSender();
					PlayerServer target     = context.getArgument("player", PlayerServer.class);
					PlayerData   targetData = PlayerData.get(target);

					boolean isAlreadyUnmuted = !targetData.muted;
					targetData.unmute();

					if (isAlreadyUnmuted) {
						player.sendMessage(TextFormatting.LIGHT_BLUE + target.username + TextFormatting.YELLOW + " is already unmuted.");
					} else {
						player.sendMessage(TextFormatting.YELLOW + "You have unmuted " + TextFormatting.LIGHT_BLUE + target.username);
						target.sendMessage(TextFormatting.ORANGE + "You have been unmuted.");
					}
					return 1;
				})));
	}
}
