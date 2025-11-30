package wyspr.BTAEssentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import wyspr.BTAEssentials.BTAEssentials;
import wyspr.BTAEssentials.utils.PlayerData;
import wyspr.BTAEssentials.utils.Teleport;
import wyspr.BTAEssentials.utils.WorldPosition;

@SuppressWarnings("ALL") public class BackCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("back")
			.requires(source -> ((CommandSource) source).hasAdmin() || BTAEssentials.BackCommand)
			.executes(context -> {
				CommandSource source     = (CommandSource) context.getSource();
				boolean       isAdmin    = source.hasAdmin();
				Player        player     = source.getSender();
				PlayerData    playerData = PlayerData.get(player);

				int cost = BTAEssentials.BackCost;
				if (player.score < cost && !isAdmin) {
					player.sendMessage("§4You do not have enough points to use this command!");
					player.sendMessage("§4You need §1" + (cost - player.score) + "§4 more points!");
					return 1;
				}

				if (playerData.canTP() || isAdmin) {
					if (playerData.atNewPos()) {
						WorldPosition lastPos = playerData.getLastPos();

						playerData.updateBackPos();

						if (Teleport.teleport(player, lastPos)) {
							player.sendMessage("§4Went back.");
						}
					} else {
						player.sendMessage("§4You have not moved!");
					}

				} else {
					int cooldown = playerData.TPCooldown();
					player.sendMessage("§4Teleport available in §1" + cooldown + "§4 seconds.");
				}

				return 1;
			}));
	}
}
