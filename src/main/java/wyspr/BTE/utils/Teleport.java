package wyspr.BTE.utils;

import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.net.packet.PacketAddEntity;
import net.minecraft.core.net.packet.PacketRespawn;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.PlayerList;
import net.minecraft.server.world.WorldServer;
import wyspr.BTE.Essentials;

public class Teleport {
	public static boolean teleport(Player player, double x, double y, double z, int dimID) {
		return teleport(player, new WorldPosition(x, y, z, dimID));
	}

	public static boolean teleport(Player player, WorldPosition destination) {
		if (player.isPassenger()) {
			player.sendMessage(TextFormatting.ORANGE + "You can't teleport while you're a passenger.");
			return false;
		}

		MinecraftServer mc         = MinecraftServer.getInstance();
		PlayerList      playerList = mc.playerList;
		if (player.dimension != destination.dimID) {
			playerList.sendPlayerToOtherDimension(
				(PlayerServer) player,
				destination.dimID,
				null,
				false
			);
			playerList.sendPacketToPlayer(
				player.username, new PacketRespawn(
					(byte) destination.dimID,
					(byte) 0
				)
			);
		}

		WorldServer world      = mc.getDimensionWorld(destination.dimID);
		int         chunkCordX = (int) destination.x >> 4;
		int         chunkCordZ = (int) destination.z >> 4;

		world
			.getChunkProvider()
			.prepareChunk(chunkCordX, chunkCordZ);

		((PlayerServer) player).teleport(
			destination.x,
			destination.y,
			destination.z,
			player.yRot,
			player.xRot
		);
		player.moveTo(destination.x, destination.y, destination.z, player.yRot, player.xRot);

		String[] sounds     = Essentials.TeleportSound.split(":");
		String   soundName  = sounds[0];
		float    soundPitch = sounds[1] != null ? Float.parseFloat(sounds[1]) : 1;
		float    soundVol   = sounds[2] != null ? Float.parseFloat(sounds[2]) : 1;
		player.world.playSoundAtEntity(null, player, soundName, soundVol, soundPitch);

		player.world.spawnParticle(
			"smoke",
			destination.x + 0.5,
			destination.y,
			destination.z + 0.5,
			0,
			0,
			0,
			0
		);
		// Show the teleported player  instantly
		// instead of waiting on the server to send the packet
		playerList.sendPacketToPlayersAroundPoint(
			destination.x,
			destination.y,
			destination.z,
			64,
			destination.dimID,
			new PacketAddEntity(player)
		);

		return true;
	}

	public static boolean teleport(Player movingPlayer, Player stationaryPlayer) {
		if (stationaryPlayer.isPassenger()) {
			stationaryPlayer.sendMessage(TextFormatting.ORANGE + "You cannot teleport as, or to, a passenger!");
			return false;
		}
		double x     = stationaryPlayer.x;
		double y     = stationaryPlayer.y;
		double z     = stationaryPlayer.z;
		int    dimID = stationaryPlayer.dimension;

		WorldPosition pos = new WorldPosition(x, y, z, dimID);

		return teleport(movingPlayer, pos);
	}
}
