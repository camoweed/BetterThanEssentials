package wyspr.BTE.utils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import wyspr.BTE.Essentials;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class WarpsManager {
	private static final File warpFile = Essentials.DATA_DIR.resolve("warps.json").toFile();

	private static HashMap<String, WorldPosition> WARPS;

	static {
		if (!warpFile.exists()) {
			WARPS = new HashMap<>();
			try {
				warpFile.createNewFile();
				save();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void load() {
		Gson gson = new Gson();
		try {
			String json = new String(Files.readAllBytes(warpFile.toPath()), StandardCharsets.UTF_8);
			WARPS = gson.fromJson(json, new TypeToken<HashMap<String, WorldPosition>>() {}.getType());
			if (WARPS == null) {
				WARPS = new HashMap<>();
			}
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}
	}

	public static void importWarps(HashMap<String, WorldPosition> newWarps) {
		WARPS = newWarps;
		save();
	}

	public static void save() {
		Gson   gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(WARPS);
		try {
			Files.write(warpFile.toPath(), json.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			System.err.println("Error writing file: " + e.getMessage());
		}
	}

	public static boolean addWarp(String name, WorldPosition position) {
		if (WARPS.containsKey(name)) {
			return false;
		}
		WARPS.put(name, position);
		return true;
	}

	public static boolean removeWarp(String name) {
		if (!WARPS.containsKey(name)) {
			return false;
		}
		WARPS.remove(name);
		return true;
	}

	public static Optional<WorldPosition> getWarp(String name) {
		return Optional.ofNullable(WARPS.get(name));
	}

	public static List<String> getWarps() {
		if (WARPS.isEmpty()) {
			return new ArrayList<>();
		}

		return WARPS
			.keySet().stream().sorted().collect(Collectors.toList());
	}
}
