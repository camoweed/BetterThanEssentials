package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.server.entity.player.PlayerServer;
import wyspr.BTE.Essentials;
import wyspr.BTE.commands.arguments.ArgumentTypeOnlineUser;
import wyspr.BTE.utils.Utils;

@SuppressWarnings("ALL")
public class FixCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		String[] literals = {"fix", "repair"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.FixCommand)
				.executes(context -> {
					CommandSource source = (CommandSource) context.getSource();
					Player        player = Utils.requirePlayer(source);
					ItemStack     held   = player.getHeldItem();
					if (held.isItemStackDamageable()) {
						held.setMetadata(0);
						player.sendMessage(TextFormatting.LIGHT_BLUE + held.getDisplayName() + TextFormatting.YELLOW + " has been repaired.");
					} else {
						player.sendMessage(TextFormatting.LIGHT_BLUE + held.getDisplayName() + TextFormatting.YELLOW + " is not repairable.");
					}

					return 1;
				})
				.then(ArgumentBuilderRequired
					.argument("player", ArgumentTypeOnlineUser.online())
					.requires(source -> ((CommandSource) source).hasAdmin())
					.executes(context -> {
						CommandSource source = (CommandSource) context.getSource();
						Player        player = Utils.requirePlayer(source);
						PlayerServer  target = context.getArgument("player", PlayerServer.class);
						ItemStack     held   = target.getHeldItem();
						if (held.isItemStackDamageable()) {
							held.setMetadata(0);
							player.sendMessage(TextFormatting.LIGHT_BLUE + held.getDisplayName() + TextFormatting.YELLOW + " has been repaired for " + TextFormatting.RESET + target.getDisplayName() + TextFormatting.YELLOW + ".");
							target.sendMessage(TextFormatting.LIGHT_BLUE + held.getDisplayName() + TextFormatting.YELLOW + " has been repaired.");
						} else {
							player.sendMessage(TextFormatting.LIGHT_BLUE + held.getDisplayName() + TextFormatting.YELLOW + " is not repairable.");
						}

						return 1;
					})));

		}
	}
}
