package wyspr.BTAEssentials.utils;

import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.packet.PacketAddEntity;
import net.minecraft.core.net.packet.PacketRespawn;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.PlayerList;
import net.minecraft.server.world.WorldServer;

public class Teleport {
	public static boolean teleport(Player player, double x, double y, double z, int dimID) {
		return teleport(player, new WorldPosition(x, y, z, dimID));
	}

	public static boolean teleport(Player player, WorldPosition destination) {
		if (player.isPassenger()) {
			player.sendMessage("§4You can't teleport while you're a passenger.");
			return false;
		}

		PlayerList playerList = MinecraftServer.getInstance().playerList;
		if (player.dimension != destination.dimID) {
			playerList.sendPlayerToOtherDimension(
				(PlayerServer) player,
				destination.dimID,
				DyeColor.MAGENTA,
				false
			);
			((PlayerServer) player).playerNetServerHandler.sendPacket(new PacketRespawn(
				(byte) destination.dimID,
				(byte) 0
			));
		}

		MinecraftServer server     = MinecraftServer.getInstance();
		WorldServer     world      = server.getDimensionWorld(destination.dimID);
		int             chunkCordX = (int) destination.x >> 4;
		int             chunkCordZ = (int) destination.z >> 4;

		world.getChunkProvider().prepareChunk(chunkCordX, chunkCordZ);

		((PlayerServer) player).playerNetServerHandler.teleportAndRotate(
			destination.x,
			destination.y,
			destination.z,
			player.yRot,
			player.xRot
		);

		player.moveTo(destination.x, destination.y, destination.z, player.yRot, player.xRot);
		return true;
	}

	public static boolean teleport(Player movingPlayer, Player stationaryPlayer) {
		if (movingPlayer.isPassenger() || stationaryPlayer.isPassenger()) {
			movingPlayer.sendMessage("§4You cannot teleport as, or to, a passenger!");
			stationaryPlayer.sendMessage("§4You cannot teleport as, or to, a passenger!");
			return false;
		}
		double     x          = stationaryPlayer.x;
		double     y          = stationaryPlayer.y;
		double     z          = stationaryPlayer.z;
		float      xr         = stationaryPlayer.xRot;
		float      yr         = stationaryPlayer.yRot;
		PlayerList playerList = MinecraftServer.getInstance().playerList;
		if (movingPlayer.dimension != stationaryPlayer.dimension) {
			playerList.sendPlayerToOtherDimension(
				(PlayerServer) movingPlayer,
				stationaryPlayer.dimension,
				DyeColor.MAGENTA,
				false
			);
			((PlayerServer) movingPlayer).playerNetServerHandler.sendPacket(new PacketRespawn(
				(byte) stationaryPlayer.dimension,
				(byte) 0
			));
		}
		((PlayerServer) movingPlayer).playerNetServerHandler.teleportAndRotate(x, y, z, yr, xr);
		movingPlayer.moveTo(x, y, z, yr, xr);
		// Show the teleported player to the accepting player instantly
		// instead of waiting on the server to send it
		((PlayerServer) stationaryPlayer).playerNetServerHandler.sendPacket(new PacketAddEntity(movingPlayer));
		return true;
	}
}
