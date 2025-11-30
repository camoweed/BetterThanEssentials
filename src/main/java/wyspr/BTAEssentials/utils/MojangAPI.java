package wyspr.BTAEssentials.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import wyspr.BTAEssentials.BTAEssentials;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MojangAPI {
	/**
	 * Gets the UUID of a Minecraft user from their username using the Mojang API.
	 *
	 * @param username the Minecraft username
	 * @return the UUID as a String, or null if not found or error
	 */
	public static String getUUID(String username) {
		try {
			String            urlStr = "https://api.mojang.com/users/profiles/minecraft/" + username;
			URL               url    = new URL(urlStr);
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
			BTAEssentials.LOGGER.error("Failed to contact Mojang API", e);
			return null;
		}
	}
}
