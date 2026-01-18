package wyspr.BTE.commands.TPA;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.PlayerServer;
import wyspr.BTE.utils.PlayerData;
import wyspr.BTE.utils.TPARequestType;
import wyspr.BTE.utils.Utils;

import java.util.List;

@SuppressWarnings("ALL")
public class TPAAllCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		String[] literals = {"tpall", "tpaall"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin())
				.executes(context -> {
					CommandSource source     = (CommandSource) context.getSource();
					Player        player     = Utils.requirePlayer(source);
					PlayerData    playerData = PlayerData.get(player);

					List<PlayerServer> players
						= MinecraftServer.getInstance().playerList.playerEntities;

					for (PlayerServer targetPlayer : players) {
						if (targetPlayer.uuid == player.uuid) continue; // Dont /tpall yourself
						PlayerData targetData = PlayerData.get(targetPlayer);
						targetData.tpManager.sendTPARequest(player.username, TPARequestType.TPAHERE);
						targetPlayer.world.playSoundAtEntity(null, targetPlayer, "note.celesta", 1, 2);
						targetPlayer.sendMessage(TextFormatting.YELLOW + player.username + TextFormatting.ORANGE + " has sent you a request to teleport to them.");
						targetPlayer.sendMessage(TextFormatting.LIME + "/tpyes " + TextFormatting.ORANGE + "to accept, " + TextFormatting.RED + "/tpno " + TextFormatting.ORANGE + "to deny.");
					}

					player.sendMessage(TextFormatting.YELLOW + "Sent a request to all players.");

					return 1;
				}));
		}
	}
}
