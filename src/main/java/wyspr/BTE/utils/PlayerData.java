package wyspr.BTE.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.player.gamemode.Gamemode;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.tuple.Pair;
import wyspr.BTE.Essentials;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static wyspr.BTE.Essentials.PLAYER_DIR;

public class PlayerData {
	public final  TPManager   tpManager;
	public final  MailManager mail;
	private final File        saveFile;
	private final Player      player;
	public        Homes       homes;
	public        boolean     craftCommandOpen = false;
	public        boolean     godMode          = false;
	public        boolean     muted;
	public        boolean     vanished;
	public        Gamemode    vanishedGamemode;

	public PlayerData(Player player) {
		this.saveFile  = new File(PLAYER_DIR.toFile(), player.uuid + ".json");
		this.player    = player;
		this.homes     = new Homes();
		this.tpManager = new TPManager();
		this.mail      = new MailManager(player.uuid.toString());

		if (!this.saveFile.exists()) {
			this.save();
		} else {
			this.load();
		}
	}

	private void save() {
		Gson gson = new GsonBuilder()
			.registerTypeAdapter(Instant.class, new InstantTypeAdapter())
			.setPrettyPrinting()
			.create();
		PlayerDataFile dataFile = new PlayerDataFile();

		dataFile.backPos    = this.tpManager.backPos;
		dataFile.lastTPTime = this.tpManager.lastTPTime;
		dataFile.homes      = this.homes;
		dataFile.vanished   = this.vanished;
		dataFile.muted      = this.muted;

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
			PlayerDataFile dataFile = gson.fromJson(json, PlayerDataFile.class);

			tpManager.backPos    = dataFile.backPos;
			tpManager.lastTPTime = dataFile.lastTPTime;
			homes                = new Homes().from(dataFile.homes);
			vanished             = dataFile.vanished;
			muted                = dataFile.muted;
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
		private static final long serialVersionUID = 1L;
		// Ensures version compatibility during deserialization

		public Instant                        lastTPTime;
		public WorldPosition                  backPos;
		public boolean                        muted    = false;
		public boolean                        vanished = false;
		public HashMap<String, WorldPosition> homes;
	}

	public static class MailManager {
		private final transient File       saveFile;
		public transient        int        selectedDraft = -1;
		public                  List<Mail> readMail;
		public                  List<Mail> inbox;
		public                  List<Mail> drafts;

		public MailManager(String uuid) {
			this.saveFile = new File(PLAYER_DIR.toFile(), uuid + ".mail.json");
			this.readMail = new ArrayList<>();
			this.inbox    = new ArrayList<>();
			this.drafts   = new ArrayList<>();

			if (!this.saveFile.exists()) {
				this.save();
			} else {
				this.load();
			}
		}

		private void save() {
			Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();
			String json = gson.toJson(this);
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
				MailManager loadedInfo = gson.fromJson(json, MailManager.class);

				this.readMail = loadedInfo.readMail;
				this.inbox    = loadedInfo.inbox;
				this.drafts   = loadedInfo.drafts;
			} catch (IOException e) {
				Essentials.LOGGER.error("Error reading file: {}", e.getMessage());
			}
		}

		public void reload() {
			load();
		}

		public boolean sendDraft(MailManager otherPlayerMail) {
			if (this.selectedDraft >= this.drafts.size() || this.selectedDraft == -1) return false;

			Mail draft = this.drafts.remove(this.selectedDraft);
			draft.sentDate = new Date();
			otherPlayerMail.inbox.add(draft);

			otherPlayerMail.save();
			save();
			return true;
		}

		public @Nullable Mail markMailRead(int mailIndex) {
			try {
				Mail removed = this.inbox.remove(mailIndex);
				this.readMail.add(removed);
				return removed;
			} catch (Exception e) {
				return null;
			}
		}

		public @Nullable Mail deleteReadMail(int mailIndex) {
			try {
				Mail removed = this.readMail.remove(mailIndex);
				save();
				return removed;
			} catch (Exception e) {
				return null;
			}
		}

		public @Nullable Mail deleteDraft(int mailIndex) {
			try {
				Mail removed = this.drafts.remove(mailIndex);
				if (this.selectedDraft == mailIndex) {
					// If the deleted draft was selected reset the selection
					this.selectedDraft = -1;
				}
				save();
				return removed;
			} catch (Exception e) {
				return null;
			}
		}

		/**
		 * Adds a new draft with a subject and sender information.
		 *
		 * @param subject String
		 * @param player  Player
		 */
		public boolean newDraftOutline(String subject, Player player) {
			Mail    mail  = new Mail(subject, player); // message empty for outline
			boolean isNew = this.drafts.add(mail);
			save();
			return isNew;
		}

		/**
		 * Adds a message to the currently selected draft
		 *
		 * @param message String
		 * @return the subject of the draft (<code>String</code>) or null if no draft is selected
		 */
		public @Nullable String attachMessageToDraft(String message) {
			Mail draft = this.drafts.get(this.selectedDraft);

			draft.message = message;
			save();
			return draft.subject;
		}

		public static class Mail {
			public final String subject;
			public final String senderUsername;
			public final String senderDisplayname;
			public       String message;
			public       Date   sentDate;

			public Mail(String subject, Player player) {
				this.subject           = subject;
				this.senderUsername    = player.username;
				this.senderDisplayname = player.getDisplayName();
			}

			public String getSentDate() {
				ZonedDateTime now  = ZonedDateTime.now();
				ZonedDateTime then = ZonedDateTime.ofInstant(sentDate.toInstant(), ZoneId.systemDefault());

				Duration diff    = Duration.between(then, now);
				long     minutes = diff.toMinutes();

				// Within the last hour
				if (minutes >= 0 && minutes < 60) {
					if (minutes == 0) return "just now";
					if (minutes == 1) return "1 minute ago";
					return minutes + " minutes ago";
				}

				// Same day
				boolean sameDay
					= now.getYear() == then.getYear() && now.getDayOfYear() == then.getDayOfYear();

				if (sameDay) {
					SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
					return timeFormat
						.format(sentDate)
						.toLowerCase(); // e.g., "9:17 am"
				}

				// Date (e.g., "Dec 27")
				SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d");
				return dateFormat.format(sentDate);
			}
		}
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
