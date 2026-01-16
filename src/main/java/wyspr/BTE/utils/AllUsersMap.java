package wyspr.BTE.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.entity.player.Player;
import wyspr.BTE.Essentials;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Set;

public class AllUsersMap {
	private static final File userFile = Essentials.DATA_DIR
		.resolve("users.json")
		.toFile();

	/// <uuid, username>
	private static BiMap<String, String> USERS;

	static {
		if (!userFile.exists()) {
			USERS = HashBiMap.create();
			// Populate file first time                                                                       o
			for (String uuid : Objects.requireNonNull(FabricLoader
				.getInstance()
				.getGameDir()
				.resolve("world/players")
				.toFile()
				.list((dir, name) -> name.endsWith(".dat")))) {
				uuid = uuid.replace(".dat", ""); // remove ".dat"
				if (uuid.length() != 36) continue;
				Essentials.LOGGER.info("Looking up username for UUID: " + uuid);
				String username = Utils.getNameFromUUID(uuid); // lookup username
				if (username != null) {
					USERS.put(uuid, username); // add to map
				}
			}
			try {
				userFile.createNewFile();
				save();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void load() {
		Gson gson = new Gson();
		try {
			String json = new String(Files.readAllBytes(userFile.toPath()), StandardCharsets.UTF_8);
			USERS = HashBiMap.create(gson.<LinkedTreeMap<String, String>>fromJson(
				json,
				new TypeToken<LinkedTreeMap<String, String>>() {}.getType()
			));
			if (USERS == null) {
				USERS = HashBiMap.create();
			}
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}
	}

	public static void save() {
		Gson gson = new GsonBuilder()
			.setPrettyPrinting()
			.create();
		String json = gson.toJson(USERS);
		try {
			Files.write(userFile.toPath(), json.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			System.err.println("Error writing file: " + e.getMessage());
		}
	}

	public static void addUser(Player player) {
		USERS.putIfAbsent(player.uuid.toString(), player.username);
	}

	public static Set<String> getUsernames() {
		return USERS.values();
	}

	public static String getUUID(String username) {
		return USERS.inverse().get(username);
	}

	public static String getUsername(String uuid) {
		return USERS.get(uuid);
	}
}
