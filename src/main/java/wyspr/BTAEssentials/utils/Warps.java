package wyspr.BTAEssentials.utils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import wyspr.BTAEssentials.BTAEssentials;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class Warps {
	private static final File warpFile = BTAEssentials.DATA_DIR.resolve("warps.json").toFile();

	private static HashMap<String, WorldPosition> warps;

	static {
		if (!warpFile.exists()) {
			warps = new HashMap<>();
			try {
				warpFile.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			load();
		}
	}

	public Warps() {}

	public static void load() {
		Gson gson = new Gson();
		try {
			String json = new String(Files.readAllBytes(warpFile.toPath()), StandardCharsets.UTF_8);
			warps = gson.fromJson(json, new TypeToken<HashMap<String, WorldPosition>>() {}.getType());
			if (warps == null) {
				warps = new HashMap<>();
			}
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}
	}

	public static void importWarps(HashMap<String, WorldPosition> newWarps) {
		warps = newWarps;
		save();
	}

	public static void save() {
		Gson   gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(warps);
		try {
			Files.write(warpFile.toPath(), json.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			System.err.println("Error writing file: " + e.getMessage());
		}
	}

	public static boolean addWarp(String name, WorldPosition position) {
		if (warps.containsKey(name)) {
			return false;
		}
		warps.put(name, position);
		return true;
	}

	public static boolean removeWarp(String name) {
		if (!warps.containsKey(name)) {
			return false;
		}
		warps.remove(name);
		return true;
	}

	public static Optional<WorldPosition> getWarp(String name) {
		return Optional.ofNullable(warps.get(name));
	}

	public static List<String> getWarps() {
		if (warps.isEmpty()) {
			return new ArrayList<>();
		}

		return warps.keySet().stream().sorted().collect(Collectors.toList());
	}
}
