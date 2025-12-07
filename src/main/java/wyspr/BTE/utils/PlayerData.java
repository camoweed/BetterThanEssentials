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

public class PlayerData implements Serializable {
	private static final    long                            serialVersionUID = 1L;
	// Ensures version compatibility during deserialization
	private final transient File                            saveFile;
	private final transient Player                          player;
	private final transient ArrayList<String>               TPARequestsOrder = new ArrayList<>();
	private final transient HashMap<String, TPARequestType> TPARequests      = new HashMap<>();
	public transient        boolean                         craftCommandOpen = false;
	public                  Instant                         lastTPTime;
	public                  WorldPosition                   backPos;
	public                  HashMap<String, WorldPosition>  homes            = new HashMap<>();
	public                  boolean                         muted;
	public                  boolean                         vanished;
	public                  Gamemode                        vanishedGamemode;
//	private transient       int                             saveTick         = 0;

	public PlayerData(Player player) {
		this.player   = player;
		this.saveFile = new File(Essentials.PLAYER_DIR.toFile(), player.uuid + ".json");

		this.lastTPTime = Instant
			.now()
			.minus(Duration.ofSeconds(Essentials.TPTimeout));
		this.backPos    = new WorldPosition(player.x, player.y, player.z, player.dimension);

		if (!saveFile.exists()) {
			this.save();
		}

		this.load();
	}

	public void save() {
		Gson gson = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(Instant.class, new InstantTypeAdapter())
			.create();
		String json = gson.toJson(this);
		try {
			Files.write(saveFile.toPath(), json.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			Essentials.LOGGER.error("Error writing file: {}", e.getMessage());
		}
	}

	public void load() {
		Gson gson = new GsonBuilder()
			.registerTypeAdapter(Instant.class, new InstantTypeAdapter())
			.create();
		try {
			String     json       = new String(Files.readAllBytes(saveFile.toPath()), StandardCharsets.UTF_8);
			PlayerData loadedInfo = gson.fromJson(json, PlayerData.class);
			lastTPTime = loadedInfo.lastTPTime;
			backPos    = loadedInfo.backPos;
			homes      = loadedInfo.homes;
			muted      = loadedInfo.muted;
			vanished   = loadedInfo.vanished;
		} catch (IOException e) {
			Essentials.LOGGER.error("Error reading file: {}", e.getMessage());
		}
	}

	public static PlayerData get(Player player) {
		return ((IPlayerData) player).betterThanEssentials$getPlayerData(player);
	}

	public static void set(Player player) {
		((IPlayerData) player).betterThanEssentials$setPlayerData(player);
	}

//	public void tick() {
//		saveTick++;
//		if (saveTick == 6000) { // 60 * 20 * 5 = 6000 (5 mins)
//			saveTick = 0;
//			if (this.player != null) {
//				save();
//			}
//		}
//	}

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
	 * If a user with a pending request sends a new request of a diffent type (TPA vs TPAHERE)
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

	public int getHomesAmount() {
		return homes.size();
	}

	/**
	 * @return <code>true</code> if the home was added, <code>false</code> if the home exists.
	 */
	public boolean setHome(Player p, String homeName) {
		if (homes.containsKey(homeName)) {
			return false;
		}

		homes.put(homeName, new WorldPosition(p.x, p.y, p.z, p.dimension));
		this.save();
		return true;
	}

	/**
	 * @return <code>true</code> if the home was removed, <code>false</code> if the home does not exist.
	 */
	public boolean delHome(String homeName) {
		if (!homes.containsKey(homeName)) {
			return false;
		}

		homes.remove(homeName);
		this.save();
		return true;
	}

	/**
	 * @return An Optional containing the <code>HomePosition</code> for the player, or an empty Optional if it does not exist.
	 */
	public Optional<WorldPosition> getHomePos(String homeName) {
		return Optional.ofNullable(homes.get(homeName));
	}

	/**
	 * @return An <code>ArrayList</code> of home names for the player,
	 * or an empty <code>ArrayList</code> if the player has no homes.
	 */
	public List<String> getHomesList() {
		return new ArrayList<>(homes.keySet());
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
}
