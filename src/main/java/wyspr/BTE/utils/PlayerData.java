package wyspr.BTE.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.player.gamemode.Gamemode;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.tuple.Pair;
import wyspr.BTE.Essentials;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class PlayerData {
	private final File      saveFile;
	private final Player    player;
	public        TPManager tpManager;
	public        Homes     homes;
	public        boolean   craftCommandOpen = false;
	public        boolean   godMode          = false;
	public        boolean   muted;
	public        boolean   vanished;
	public        Gamemode  vanishedGamemode;
	public 		  float 	speed;
	public 		  float 	flySpeed;

	public PlayerData(Player player) {
		this.player    = player;
		this.saveFile  = new File(Essentials.PLAYER_DIR.toFile(), player.uuid + ".json");
		this.tpManager = new TPManager();
		this.homes     = new Homes();

		if (!saveFile.exists()) {
			this.save();
		}
		this.load();
	}

	private void save() {
		Gson gson = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(Instant.class, new InstantTypeAdapter())
			.create();
		PlayerDataFile dataFile = new PlayerDataFile();
		dataFile.backPos          = this.tpManager.backPos;
		dataFile.lastTPTime       = this.tpManager.lastTPTime;
		dataFile.homes            = this.homes;
		dataFile.vanished         = this.vanished;
		dataFile.vanishedGamemode = this.vanishedGamemode;
		dataFile.muted            = this.muted;
		String json = gson.toJson(dataFile);
		try {
			Files.write(saveFile.toPath(), json.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			Essentials.LOGGER.error("Error writing file: {}", e.getMessage());
		}
	}

	private void load() {
		Gson gson = new GsonBuilder()
			.registerTypeAdapter(Instant.class, new InstantTypeAdapter())
			.create();
		try {
			String json = new String(
				Files.readAllBytes(saveFile.toPath()),
				StandardCharsets.UTF_8
			);
			PlayerDataFile loadedInfo = gson.fromJson(json, PlayerDataFile.class);

			tpManager.lastTPTime = loadedInfo.lastTPTime;
			tpManager.backPos    = loadedInfo.backPos;
			homes                = new Homes().from(loadedInfo.homes);
			muted                = loadedInfo.muted;
			vanished             = loadedInfo.vanished;
		} catch (IOException e) {
			Essentials.LOGGER.error("Error reading file: {}", e.getMessage());
		}
	}

	public static PlayerData get(Player player) {
		return ((Interface) player).betterThanEssentials$getPlayerData(player);
	}

	public static void set(Player player) {
		((Interface) player).betterThanEssentials$setPlayerData(player);
	}

	public boolean toggleVanished() {
		if (vanished) {
			vanished = false;
			player.setGamemode(vanishedGamemode);
			MinecraftServer.getInstance().playerList.sendEncryptedChatToAllPlayers(player.getDisplayName() + TextFormatting.YELLOW + " joined the game.");
		} else {
			vanishedGamemode = player.gamemode;
			vanished         = true;
			MinecraftServer.getInstance().playerList.sendEncryptedChatToAllPlayers(player.getDisplayName() + TextFormatting.YELLOW + " left the game.");
			player.setGamemode(Gamemode.spectator);
		}
		this.save();
		return vanished;
	}

	public void removeVanish() {
		vanished = false;
		MinecraftServer.getInstance().playerList.sendEncryptedChatToAllPlayers(player.getDisplayName() + TextFormatting.YELLOW + " joined the game.");
		save();
	}

	public void mute() {
		muted = true;
		save();
	}

	public void unmute() {
		muted = false;
		save();
	}

	public boolean toggleGodMode() {
		godMode = !godMode;
		return godMode;
	}

	public interface Interface {
		PlayerData betterThanEssentials$getPlayerData(Player player);

		void betterThanEssentials$setPlayerData(Player player);
	}

	private static class PlayerDataFile implements Serializable {
		private static final long                           serialVersionUID = 1L;
		// Ensures version compatibility during deserialization
		public               Instant                        lastTPTime;
		public               WorldPosition                  backPos;
		public               HashMap<String, WorldPosition> homes;
		public               boolean                        muted;
		public               boolean                        vanished;
		public               Gamemode                       vanishedGamemode;
	}

	public class TPManager {
		private final ArrayList<String>               TPARequestsOrder = new ArrayList<>();
		private final HashMap<String, TPARequestType> TPARequests      = new HashMap<>();
		private       Instant                         lastTPTime;
		private       WorldPosition                   backPos;

		public TPManager() {
			this.lastTPTime = Instant
				.now()
				.minus(Duration.ofSeconds(Essentials.TPTimeout));
			this.backPos    = new WorldPosition(player.x, player.y, player.z, player.dimension);
		}

		public boolean canTP() {
			return TPCooldown() == 0;
		}

		/// Returns 0 if tp is available
		public int TPCooldown() {
			Instant now         = Instant.now();
			Instant TPAvailable = lastTPTime.plus(Duration.ofSeconds(Essentials.TPTimeout));

			if (now.isAfter(TPAvailable)) {
				return 0;
			}

			return Math.toIntExact(Duration
				.between(now, TPAvailable)
				.getSeconds());
		}

		public WorldPosition getLastPos() {
			return backPos;
		}

		public void updateBackPos() {
			lastTPTime = Instant.now();
			backPos    = new WorldPosition(player.x, player.y, player.z, player.dimension);
			save();
		}

		public boolean atNewPos() {
			WorldPosition newPos = new WorldPosition(player.x, player.y, player.z, player.dimension);
			return !(newPos.equals(backPos));
		}

		/**
		 * Send a teleport request from a user.
		 * <br>
		 * If a user with a pending request sends a new request of a different type (TPA vs TPAHERE)
		 * the old request is removed and the new one is moved to the front of the list.
		 *
		 * @param username String
		 * @param type     TPARequestType
		 * @return <code>true</code> if request was sent, <code>false</code> if that user has a pending request or is offline</code>
		 */
		public boolean sendTPARequest(String username, TPARequestType type) {
			if (TPARequests.get(username) == type) {
				return false;
			}
			TPARequestsOrder.remove(username);
			TPARequestsOrder.add(username);
			TPARequests.put(username, type);
			return true;
		}

		public Pair<String, TPARequestType> getNewestRequest() {
			// Get the index of the newest request
			int lastIndex = TPARequests.size() - 1;

			// Check if there are any requests
			if (lastIndex >= 0) {
				String         lastRequestName = TPARequestsOrder.get(lastIndex);
				TPARequestType req             = TPARequests.get(lastRequestName);

				return Pair.of(lastRequestName, req);
			} else {
				return null;
			}
		}

		public List<String> getAllRequests() {
			if (hasNoRequests()) {
				return Collections.singletonList(TextFormatting.YELLOW + "You don't have any requests.");
			}
			ArrayList<String> out = new ArrayList<>();

			for (String username : TPARequestsOrder) {
				TPARequestType type = TPARequests.get(username);

				out.add(TextFormatting.ORANGE + "[ " + TextFormatting.LIGHT_BLUE + username + " " + TextFormatting.ORANGE + "| " + TextFormatting.YELLOW + type.toString() + TextFormatting.ORANGE + " ]" + TextFormatting.RESET);
			}

			return out;
		}

		public boolean hasNoRequests() {
			return TPARequests.isEmpty();
		}

		public boolean hasRequestFrom(String username) {
			return TPARequests.containsKey(username);
		}

		public TPARequestType getRequest(String username) {
			return TPARequests.get(username);
		}

		public void removeRequest(String username) {
			TPARequestsOrder.remove(username);
			TPARequests.remove(username);
		}
	}

	public class Homes extends HashMap<String, WorldPosition> {
		public Homes from(HashMap<String, WorldPosition> map) {
			for (String key : map.keySet()) {
				this.put(key, map.get(key));
			}
			return this;
		}

		public int getHomesAmount() {
			return this.size();
		}

		/**
		 * @return <code>true</code> if the home was added, <code>false</code> if the home exists.
		 */
		public boolean setHome(Player p, String homeName) {
			if (this.containsKey(homeName)) {
				return false;
			}

			this.put(homeName, new WorldPosition(p.x, p.y, p.z, p.dimension));

			save();
			return true;
		}

		/**
		 * @return <code>true</code> if the home was removed, <code>false</code> if the home does not exist.
		 */
		public boolean delHome(String homeName) {
			if (!this.containsKey(homeName)) {
				return false;
			}

			this.remove(homeName);
			save();
			return true;
		}

		/**
		 * @return An Optional containing the <code>HomePosition</code> for the player, or an empty Optional if it does not exist.
		 */
		public Optional<WorldPosition> getHomePos(String homeName) {
			return Optional.ofNullable(this.get(homeName));
		}

		/**
		 * @return An <code>ArrayList</code> of home names for the player,
		 * or an empty <code>ArrayList</code> if the player has no homes.
		 */
		public List<String> getHomesList() {
			return new ArrayList<>(this.keySet());
		}

	}
}
