package wyspr.BTAEssentials.commands.warp;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import wyspr.BTAEssentials.BTAEssentials;
import wyspr.BTAEssentials.commands.arguments.ArgumentTypeWarp;
import wyspr.BTAEssentials.utils.PlayerData;
import wyspr.BTAEssentials.utils.Teleport;
import wyspr.BTAEssentials.utils.Warps;
import wyspr.BTAEssentials.utils.WorldPosition;

import java.util.Optional;

@SuppressWarnings("ALL") public class WarpCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("warp")
			.requires(source -> ((CommandSource) source).hasAdmin() || BTAEssentials.WarpCommand)
			.then(ArgumentBuilderRequired.argument("target", ArgumentTypeWarp.warp()))
			.executes(context -> {
				CommandSource source     = (CommandSource) context.getSource();
				Player        player     = source.getSender();
				PlayerData    playerData = PlayerData.get(player);
				boolean       isAdmin    = source.hasAdmin();
				String        target     = context.getArgument("target", String.class);

				Optional<WorldPosition> warp = Warps.getWarp(target);

				if (!warp.isPresent()) {
					player.sendMessage("§1There is no warp named: §4" + target);
					return 1;
				}

				int cost = BTAEssentials.WarpCost;
				if (player.score < cost && !isAdmin) {
					player.sendMessage("§4You do not have enough points to use this command!");
					player.sendMessage("§4You need §1" + (cost - player.score) + "§4 more points!");
					return 1;
				}

				if (playerData.canTP() || isAdmin) {
					playerData.updateBackPos();
					WorldPosition warpPos = warp.get();
					if (Teleport.teleport(player, warpPos)) {
						player.sendMessage("§4Teleported to §1" + target);
					}
				} else {
					int waitTime = playerData.TPCooldown();
					player.sendMessage("§4Teleport available in §1" + waitTime + "§4 seconds.");
				}

				return 1;
			}));
	}
}
