package wyspr.BTAEssentials.commands.home;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.world.chunk.ChunkCoordinates;
import wyspr.BTAEssentials.BTAEssentials;
import wyspr.BTAEssentials.commands.arguments.ArgumentTypeHome;
import wyspr.BTAEssentials.utils.PlayerData;
import wyspr.BTAEssentials.utils.Teleport;
import wyspr.BTAEssentials.utils.WorldPosition;

import java.util.Optional;

@SuppressWarnings("ALL") public class HomeCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) (ArgumentBuilderLiteral
			.literal("home")
			.requires(source -> ((CommandSource) source).hasAdmin() || BTAEssentials.HomeCommand)
			.executes(context -> {
				CommandSource source = (CommandSource) context.getSource();
				boolean isAdmin = source.hasAdmin();
				Player player = source.getSender();
				PlayerData playerData = PlayerData.get(player);
				String homeName = "home";
				Optional<WorldPosition> homePos = playerData.getHomePos(homeName);

				return goHome(homePos, player, homeName, playerData, isAdmin);
			})).then(ArgumentBuilderRequired
			.argument("home", ArgumentTypeHome.home())
			.executes(context -> {
				CommandSource source = (CommandSource) context.getSource();
				boolean isAdmin = source.hasAdmin();
				Player player = source.getSender();
				PlayerData playerData = PlayerData.get(player);
				String homeName = context.getArgument("home", String.class);

				Optional<WorldPosition> homePos;

				if (homeName.equals("bed")) {
					ChunkCoordinates bed = player.getPlayerSpawnCoordinate();
					if (bed == null) {
						player.sendMessage("§1You do not have a bed! You should work on that!");
						return 1;
					}
					homePos = Optional.of(new WorldPosition(bed.x, bed.y + 1.0, bed.z, 0));
				} else {
					homePos = playerData.getHomePos(homeName);
				}

				return goHome(homePos, player, homeName, playerData, isAdmin);
			})));
	}

	private static Integer goHome(
		Optional<WorldPosition> homePos,
		Player player,
		String homeName,
		PlayerData playerData,
		boolean isAdmin
	)
	{
		int cost = BTAEssentials.HomeCost;
		if (player.score < cost && !isAdmin) {
			player.sendMessage("§4You do not have enough points to use this command!");
			player.sendMessage("§4You need §1" + (cost - player.score) + "§4 more points!");
			return 1;
		}

		int homes = playerData.getHomesAmount();
		if (homes == 0) {
			player.sendMessage("§1You do not have any homes!");
			player.sendMessage("§1Set a home with: §3/sethome [name]");
			return 1;
		}

		boolean homeNotFound = !homePos.isPresent();
		if (homeNotFound) {
			player.sendMessage("§1You do not have a home named: §4" + homeName);
			player.sendMessage("§1View your homes with: §3/homes");
			return 1;
		}

		if (playerData.canTP() || isAdmin) {
			playerData.updateBackPos();
			WorldPosition home = homePos.get();
			if (Teleport.teleport(player, home)) {
				player.sendMessage("§4Teleported to §1" + homeName);
			}
		} else {
			int waitTime = playerData.TPCooldown();
			player.sendMessage("§4Teleport available in §1" + waitTime + "§4 seconds.");
		}
		return 1;
	}
}
