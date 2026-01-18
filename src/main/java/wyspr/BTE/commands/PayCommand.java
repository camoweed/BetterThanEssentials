package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeInteger;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.server.entity.player.PlayerServer;
import wyspr.BTE.Essentials;
import wyspr.BTE.commands.arguments.ArgumentTypeOnlineUser;
import wyspr.BTE.utils.Utils;

@SuppressWarnings("ALL")
public class PayCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("pay")
			.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.PayCommand)
			.then(ArgumentBuilderRequired
				.argument("player", ArgumentTypeOnlineUser.online())
				.then(ArgumentBuilderRequired
					.argument("amount", ArgumentTypeInteger.integer(1))
					.executes(context -> {
						CommandSource source        = (CommandSource) context.getSource();
						Player        sender        = Utils.requirePlayer(source);
						boolean       senderIsAdmin = source.hasAdmin();
						int           amount        = context.getArgument("amount", Integer.class);
						PlayerServer  reciever      = context.getArgument("player", PlayerServer.class);

						if (sender.score < amount && !senderIsAdmin) {
							sender.sendMessage(TextFormatting.RED + (TextFormatting.BOLD + "Insufficient funds!"));
							return 1;
						}

						sender.score -= amount;
						reciever.score += amount;
						sender.sendMessage(
							TextFormatting.ORANGE + "Paid " +
								TextFormatting.YELLOW + reciever.username + " " +
								TextFormatting.LIGHT_BLUE + amount +
								TextFormatting.ORANGE + " points."
						);
						reciever.sendMessage(
							TextFormatting.YELLOW + sender.username +
								TextFormatting.ORANGE + " has paid you " +
								TextFormatting.LIGHT_BLUE + amount +
								TextFormatting.ORANGE + " points."
						);

						return 1;
					}))));
	}
}
