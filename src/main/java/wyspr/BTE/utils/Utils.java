package wyspr.BTE.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.server.entity.player.PlayerServer;
import wyspr.BTE.Essentials;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class Utils {
	private static final SimpleCommandExceptionType PLAYER_RUN
		= new SimpleCommandExceptionType(() -> "This command must be run by a player");

	/**
	 * Gets the UUID of a Minecraft user from their username using the Mojang API.
	 *
	 * @param username the Minecraft username
	 * @return the UUID as a String, or null if not found or error
	 */
	public static String getUUIDFromName(String username) {
		try {
			URL               url  = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			// There is no response for this error. If you encounter this error, the username you have
			// provided has either never been on a profile, is currently dropping to the public (on cooldown),
			// or has been on a profile that is deleted (either hard-deleted or pseudo-hard-deleted).
			if (conn.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
				return null;
			}
			// Most likely, the reason you are getting this error is
			// that you've supplied an invalid username as the username URL parameter.
			if (conn.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
				return null;
			}

			BufferedReader reader   = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder  response = new StringBuilder();
			String         line;

			while ((line = reader.readLine()) != null) {
				response.append(line);
			}

			reader.close();

			JsonObject json = JsonParser
				.parseString(response.toString())
				.getAsJsonObject();
			return json
				.get("id")
				.getAsString()
				.replaceFirst(
					"(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})",
					"$1-$2-$3-$4-$5"
				);
		} catch (Exception e) {
			Essentials.LOGGER.error("Failed to contact Mojang API", e);
			return null;
		}
	}

	public static @Nullable String getNameFromUUID(UUID uuid) {
		return getNameFromUUID(uuid.toString());
	}

	public static @Nullable String getNameFromUUID(String uuid) {
		try {
			URL               url  = new URL("https://api.mojang.com/user/profile/" + uuid);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			// There is no response for this error. If you encounter this error,
			// the UUID you have provided has either never been on a profile or
			// has been on a profile that is hard-deleted.
			if (conn.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
				return null;
			}
			// Most likely, the reason you are getting this error is
			// that you've supplied an invalid UUID as the uuid URL parameter.
			if (conn.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
				return null;
			}

			// If you get this error, you have sent too many requests and
			// must wait at least 30 seconds before sending another.
			if (conn.getResponseCode() == 429) {
				return null;
			}

			BufferedReader reader   = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder  response = new StringBuilder();
			String         line;

			while ((line = reader.readLine()) != null) {
				response.append(line);
			}

			reader.close();

			JsonObject json = JsonParser
				.parseString(response.toString())
				.getAsJsonObject();
			return json
				.get("name")
				.getAsString();

		} catch (Exception e) {
			Essentials.LOGGER.error("Failed to contact Mojang API", e);
			return null;
		}
	}

	public static HitResult rayCastFromPlayer(PlayerServer player, int distance) {
		return player.rayTrace(distance, 1, true, true);
	}

	public static void playNotificationAtPlayer(Player player, String essentialsSound) {
		String[] sounds     = essentialsSound.split(":");
		String   soundName  = sounds[0];
		float    soundPitch = sounds[1] != null ? Float.parseFloat(sounds[1]) : 1;
		float    soundVol   = sounds[2] != null ? Float.parseFloat(sounds[2]) : 1;
		if (player.world != null) {
			player.world.playSoundAtEntity(null, player, soundName, soundVol, soundPitch);
		}
	}

	public static Player requirePlayer(CommandSource source) throws CommandSyntaxException {
		Player player = source.getSender();
		if (player == null) throw PLAYER_RUN.create();
		return player;
	}
}
