package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.server.player.PlayerListBox;
import wyspr.BTE.utils.PlayerData;

@SuppressWarnings("ALL")
public class VanishCommand implements CommandManager.CommandRegistry {

	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("vanish")
			.requires(source -> ((CommandSource) source).hasAdmin())
			.executes(context -> {
				CommandSource source = (CommandSource) context.getSource();
				Player        player = source.getSender();
				boolean isVanished = PlayerData
					.get(player)
					.toggleVanished();

				PlayerListBox.updateList();

				if (isVanished) {
					player.sendMessage(TextFormatting.YELLOW + "You have vanished.");
				} else {
					player.sendMessage(TextFormatting.YELLOW + "You have reappeared.");
				}

				return 1;
			}));
	}
}
