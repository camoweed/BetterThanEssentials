package wyspr.BTE.commands.warp;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import wyspr.BTE.Essentials;
import wyspr.BTE.commands.arguments.ArgumentTypeWarp;
import wyspr.BTE.utils.PlayerData;
import wyspr.BTE.utils.Teleport;
import wyspr.BTE.utils.Warps;
import wyspr.BTE.utils.WorldPosition;

import java.util.Optional;

@SuppressWarnings("ALL") public class WarpCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("warp")
			.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.WarpCommand)
			.then(ArgumentBuilderRequired
				.argument("target", ArgumentTypeWarp.warp())
				.executes(this::exec)));
	}

	private int exec(CommandContext<Object> context) throws CommandSyntaxException {
			CommandSource source     = (CommandSource) context.getSource();
			Player        player     = source.getSender();
			PlayerData    playerData = PlayerData.get(player);
			boolean       isAdmin    = source.hasAdmin();
			String        target     = context.getArgument("target", String.class);

			Optional<WorldPosition> warp = Warps.getWarp(target);

			if (!warp.isPresent()) {
				player.sendMessage(TextFormatting.ORANGE + "There is no warp named: " + TextFormatting.YELLOW + target);
				return 1;
			}

			int cost = Essentials.WarpCost;
			if (player.score < cost && !isAdmin) {
				player.sendMessage(TextFormatting.YELLOW + "You do not have enough points to use this command!");
				player.sendMessage(TextFormatting.YELLOW + "You need " + TextFormatting.ORANGE + (cost - player.score) + TextFormatting.YELLOW + " more points!");
				return 1;
			}

			if (playerData.canTP() || isAdmin) {
				playerData.updateBackPos();
				WorldPosition warpPos = warp.get();
				if (Teleport.teleport(player, warpPos)) {
					player.sendMessage(TextFormatting.YELLOW + "Teleported to " + TextFormatting.ORANGE + target);
				}
			} else {
				int waitTime = playerData.TPCooldown();
				player.sendMessage(TextFormatting.YELLOW + "Teleport available in " + TextFormatting.ORANGE + waitTime + TextFormatting.YELLOW + " seconds.");
			}

			return 1;
	}
}
