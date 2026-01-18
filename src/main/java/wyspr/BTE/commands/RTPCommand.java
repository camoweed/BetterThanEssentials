package wyspr.BTE.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import wyspr.BTE.Essentials;
import wyspr.BTE.utils.PlayerData;
import wyspr.BTE.utils.PlayerData.TPManager;
import wyspr.BTE.utils.Teleport;
import wyspr.BTE.utils.Utils;

import java.util.Random;

@SuppressWarnings("ALL")
public class RTPCommand implements CommandManager.CommandRegistry {
	Random r = new Random();

	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("rtp")
			.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.RTPCommand)
			.executes(context -> {
				CommandSource source    = (CommandSource) context.getSource();
				boolean       isAdmin   = source.hasAdmin();
				Player        player    = Utils.requirePlayer(source);
				TPManager     playerTPM = PlayerData.get(player).tpManager;

				int cost = Essentials.RTPCost;
				if (player.score < cost && !isAdmin) {
					player.sendMessage(TextFormatting.YELLOW + "You do not have enough points to use this command!");
					player.sendMessage(
						TextFormatting.YELLOW + "You need " +
							TextFormatting.ORANGE + (cost - player.score) +
							TextFormatting.YELLOW + " more points!"
					);
					return 1;
				}
				if (player.dimension != 0) {
					player.sendMessage(TextFormatting.YELLOW + "You may only use this in the overworld!");
					return 1;
				}
				if (!playerTPM.canTP() && !isAdmin) {
					int waitTime = playerTPM.TPCooldown();
					player.sendMessage(
						TextFormatting.YELLOW + "Teleport available in " +
							TextFormatting.ORANGE + waitTime +
							TextFormatting.YELLOW + " seconds."
					);
					return 1;
				}

				int min   = Essentials.RTPMin;
				int max   = Essentials.RTPMax;
				int randX = (int) (r.nextDouble() * (max - min) + min);
				int randZ = (int) (r.nextDouble() * (max - min) + min);

				playerTPM.updateBackPos();

				if (Teleport.teleport(player, randX, 256, randZ, player.dimension)) {
					player.sendMessage(
						TextFormatting.YELLOW + "Teleported! " +
							TextFormatting.BOLD + "Should you get stuck, rejoin."
					);

					player.score -= Essentials.RTPCost;

					player.fireImmuneTicks = 200;
					player.onGround        = false;
					player.maxHurtTime     = 200;
					player.hurtTime        = 200;
					player.airSupply       = 1000;
					player.fallDistance    = -1000;
				}

				return 1;
			}));
	}
}
