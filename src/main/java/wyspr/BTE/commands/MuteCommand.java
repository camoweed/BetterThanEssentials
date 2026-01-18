package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.server.entity.player.PlayerServer;
import wyspr.BTE.commands.arguments.ArgumentTypeOnlineUser;
import wyspr.BTE.utils.PlayerData;
import wyspr.BTE.utils.Utils;

@SuppressWarnings("ALL")
public class MuteCommand implements CommandManager.CommandRegistry {

	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("mute")
			.requires(source -> ((CommandSource) source).hasAdmin())
			.then(ArgumentBuilderRequired
				.argument("player", ArgumentTypeOnlineUser.online())
				.executes(context -> {
					CommandSource source     = (CommandSource) context.getSource();
					Player        player     = Utils.requirePlayer(source);
					PlayerServer  target     = context.getArgument("player", PlayerServer.class);
					PlayerData    targetData = PlayerData.get(target);

					boolean isAlreadyMuted = targetData.muted;
					targetData.mute();

					if (isAlreadyMuted) {
						player.sendMessage(TextFormatting.LIGHT_BLUE + target.username + TextFormatting.YELLOW + " is already muted.");
					} else {
						player.sendMessage(TextFormatting.YELLOW + "You have muted " + TextFormatting.LIGHT_BLUE + target.username);
						target.sendMessage(TextFormatting.RED + "You have been muted.");
					}
					return 1;
				})));
	}
}
