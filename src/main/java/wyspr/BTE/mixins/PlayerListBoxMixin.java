package wyspr.BTE.mixins;

import net.minecraft.core.net.packet.PacketPlayerList;
import net.minecraft.core.player.gamemode.Gamemode;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.player.PlayerListBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import wyspr.BTE.utils.PlayerData;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = PlayerListBox.class, remap = false)
public class PlayerListBoxMixin {
	/**
	 * @author wyspr
	 * @reason hide spectators from playerlist
	 */
	@Overwrite
	public static void updateList() {
		MinecraftServer server      = MinecraftServer.getInstance();
		List<String>    playersList = new ArrayList<>();
		List<String>    scoresList  = new ArrayList<>();

		for (PlayerServer player : server.playerList.playerEntities) {
			if (PlayerData.get(player).vanished && player.gamemode == Gamemode.spectator)
				continue;  // skip spectators
			playersList.add(player.getDisplayName());
			scoresList.add(String.valueOf(player.getScore()));
		}

		int playerCount = playersList.size();

		String[] players = new String[playerCount];
		String[] scores  = new String[playerCount];

		for (int i = 0; i < playerCount; i++) players[i] = playersList.get(i);
		for (int i = 0; i < playerCount; i++) scores[i] = scoresList.get(i);

		server.playerList.sendPacketToAllPlayers(new PacketPlayerList(playerCount, players, scores));
	}
}
