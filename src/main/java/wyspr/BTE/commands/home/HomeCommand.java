package wyspr.BTE.commands.home;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.world.chunk.ChunkCoordinates;
import net.minecraft.server.entity.player.PlayerServer;
import wyspr.BTE.Essentials;
import wyspr.BTE.commands.arguments.ArgumentTypeHome;
import wyspr.BTE.commands.arguments.ArgumentTypeOnlineUser;
import wyspr.BTE.utils.PlayerData;
import wyspr.BTE.utils.PlayerData.TPManager;
import wyspr.BTE.utils.Teleport;
import wyspr.BTE.utils.Utils;
import wyspr.BTE.utils.WorldPosition;

import java.util.Optional;

@SuppressWarnings("ALL")
public class HomeCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("home")
			.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.HomeCommand)
			.executes(this::noArg)
			.then(ArgumentBuilderRequired
				.argument("home", ArgumentTypeHome.ownHomes())
				.executes(this::homeArg))
			.then(ArgumentBuilderRequired
				.argument("player", ArgumentTypeOnlineUser.online())
				.requires(source -> ((CommandSource) source).hasAdmin())
				.then(ArgumentBuilderRequired
					.argument("home", ArgumentTypeHome.othersHomes())
					.executes(this::playerHomeArg))));
	}

	private int noArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource           source     = (CommandSource) context.getSource();
		boolean                 isAdmin    = source.hasAdmin();
		Player                  player     = Utils.requirePlayer(source);
		PlayerData              playerData = PlayerData.get(player);
		String                  homeName   = "home";
		Optional<WorldPosition> homePos    = playerData.homes.getHomePos(homeName);

		return goHome(homePos, player, homeName, playerData, isAdmin);
	}

	private int homeArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source     = (CommandSource) context.getSource();
		boolean       isAdmin    = source.hasAdmin();
		Player        player     = Utils.requirePlayer(source);
		PlayerData    playerData = PlayerData.get(player);
		String        homeName   = context.getArgument("home", String.class);

		Optional<WorldPosition> homePos;

		if (homeName.equals("bed")) {
			ChunkCoordinates bed = player.getPlayerSpawnCoordinate();
			if (bed == null) {
				player.sendMessage(TextFormatting.ORANGE + "You do not have a bed! You should work on that!");
				return 1;
			}
			homePos = Optional.of(new WorldPosition(bed.x, bed.y + 1.0, bed.z, 0));
		} else {
			homePos = playerData.homes.getHomePos(homeName);
		}

		return goHome(homePos, player, homeName, playerData, isAdmin);
	}

	private int playerHomeArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source       = (CommandSource) context.getSource();
		boolean       isAdmin      = source.hasAdmin();
		Player        player       = Utils.requirePlayer(source);
		TPManager     playerTP     = PlayerData.get(player).tpManager;
		String        homeName     = context.getArgument("home", String.class);
		PlayerServer  targetPlayer = context.getArgument("player", PlayerServer.class);
		PlayerData    targetData   = PlayerData.get(targetPlayer);

		int homes = targetData.homes.getHomesAmount();
		if (homes == 0) {
			player.sendMessage(targetPlayer.getDisplayName() + TextFormatting.ORANGE + " does not have any homes!");
			return 1;
		}

		Optional<WorldPosition> homePos = targetData.homes.getHomePos(homeName);

		boolean homeNotFound = !homePos.isPresent();
		if (homeNotFound) {
			player.sendMessage(targetPlayer.getDisplayName() + TextFormatting.ORANGE + " does not have a home named: " + TextFormatting.YELLOW + homeName);
			player.sendMessage(TextFormatting.ORANGE + "View homes with: " + TextFormatting.LIGHT_BLUE + "/homes " + targetPlayer.username);
			return 1;
		}

		playerTP.updateBackPos();
		WorldPosition home = homePos.get();
		if (Teleport.teleport(player, home)) {
			player.sendMessage(TextFormatting.YELLOW + "Teleported to " + TextFormatting.RESET + targetPlayer.getDisplayName() + TextFormatting.YELLOW + "'s home: " + TextFormatting.ORANGE + homeName);
		}

		return 1;
	}


	private static Integer goHome(
		Optional<WorldPosition> homePos,
		Player player,
		String homeName,
		PlayerData playerData,
		boolean isAdmin
	)
	{
		int cost = Essentials.HomeCost;
		if (player.score < cost && !isAdmin) {
			player.sendMessage(TextFormatting.YELLOW + "You do not have enough points to use this command!");
			player.sendMessage(TextFormatting.YELLOW + "You need " + TextFormatting.ORANGE + (cost - player.score) + TextFormatting.YELLOW + " more points!");
			return 1;
		}

		int homes = playerData.homes.getHomesAmount();
		if (homes == 0) {
			player.sendMessage(TextFormatting.ORANGE + "You do not have any homes!");
			player.sendMessage(TextFormatting.ORANGE + "Set a home with: §3/sethome [name]");
			return 1;
		}

		boolean homeNotFound = !homePos.isPresent();
		if (homeNotFound) {
			player.sendMessage(TextFormatting.ORANGE + "You do not have a home named: " + TextFormatting.YELLOW + homeName);
			player.sendMessage(TextFormatting.ORANGE + "View your homes with: §3/homes");
			return 1;
		}

		if (playerData.tpManager.canTP() || isAdmin) {
			playerData.tpManager.updateBackPos();
			WorldPosition home = homePos.get();
			if (Teleport.teleport(player, home)) {
				player.sendMessage(TextFormatting.YELLOW + "Teleported to " + TextFormatting.ORANGE + homeName);
			}
		} else {
			int waitTime = playerData.tpManager.TPCooldown();
			player.sendMessage(TextFormatting.YELLOW + "Teleport available in " + TextFormatting.ORANGE + waitTime + TextFormatting.YELLOW + " seconds.");
		}
		return 1;
	}
}
