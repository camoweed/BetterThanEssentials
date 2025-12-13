package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import wyspr.BTE.Essentials;
import wyspr.BTE.utils.PlayerData;
import wyspr.BTE.utils.PlayerData.TPManager;
import wyspr.BTE.utils.Teleport;
import wyspr.BTE.utils.WorldPosition;

@SuppressWarnings("ALL") public class BackCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("back")
			.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.BackCommand)
			.executes(this::command));
	}

	private int command(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source     = (CommandSource) context.getSource();
		boolean       isAdmin    = source.hasAdmin();
		Player    player     = source.getSender();
		TPManager playerData = PlayerData.get(player).tpManager;

		int cost = Essentials.BackCost;
		if (player.score < cost && !isAdmin) {
			player.sendMessage(TextFormatting.YELLOW + "You do not have enough points to use this command!");
			player.sendMessage(TextFormatting.YELLOW + "You need " + TextFormatting.ORANGE + (cost - player.score) + TextFormatting.YELLOW + " more points!");
			return 1;
		}

		if (playerData.canTP() || isAdmin) {
			if (playerData.atNewPos()) {
				WorldPosition lastPos = playerData.getLastPos();

				playerData.updateBackPos();

				if (Teleport.teleport(player, lastPos)) {
					player.sendMessage(TextFormatting.YELLOW + "Went back.");
				}
			} else {
				player.sendMessage(TextFormatting.YELLOW + "You have not moved!");
			}

		} else {
			int cooldown = playerData.TPCooldown();
			player.sendMessage(TextFormatting.YELLOW + "Teleport available in " + TextFormatting.ORANGE + cooldown + TextFormatting.YELLOW + " seconds.");
		}

		return 1;
	}
}
