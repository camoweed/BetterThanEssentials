package wyspr.BTE.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
	/**
	 * Gets the UUID of a Minecraft user from their username using the Mojang API.
	 *
	 * @param username the Minecraft username
	 * @return the UUID as a String, or null if not found or error
	 */
	public static String getUUIDFromName(String username) {
		try {
			URL               url    = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
			HttpURLConnection conn   = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			// If the username doesn't exist, Mojang returns HTTP 204
			if (conn.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
				return null;
			}

			BufferedReader reader   = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder  response = new StringBuilder();
			String         line;

			while ((line = reader.readLine()) != null) {
				response.append(line);
			}

			reader.close();

			JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
			return json.get("id").getAsString().replaceFirst(
				"(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})",
				"$1-$2-$3-$4-$5"
			);
		} catch (Exception e) {
			Essentials.LOGGER.error("Failed to contact Mojang API", e);
			return null;
		}
	}

	public static @Nullable String getNameFromUUID(UUID uuid) {
		try {
			URL               url    = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
			HttpURLConnection conn   = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			BufferedReader reader   = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder  response = new StringBuilder();
			String         line;

			while ((line = reader.readLine()) != null) {
				response.append(line);
			}

			reader.close();

			JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
			return json.get("name").getAsString();

		} catch (Exception e) {
			Essentials.LOGGER.error("Failed to contact Mojang API", e);
			return null;
		}
	}

	public static HitResult rayCastFromPlayer(PlayerServer player, int distance) {
		return player.rayTrace(distance, 1, true, true);
	}
}
