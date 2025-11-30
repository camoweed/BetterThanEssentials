package wyspr.BTAEssentials.commands;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.world.chunk.ChunkCoordinates;
import wyspr.BTAEssentials.BTAEssentials;
import wyspr.BTAEssentials.utils.InstantTypeAdapter;
import wyspr.BTAEssentials.utils.Warps;
import wyspr.BTAEssentials.utils.WorldPosition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("ALL") public class ImportMelonUtilsCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("importmelonutils")
			.requires(source -> ((CommandSource) source).hasAdmin())
			.executes(context -> {
				CommandSource source = (CommandSource) context.getSource();
				Player        player = source.getSender();

				Path melonutilsDir = BTAEssentials.CFG_DIR.resolve("melonutilities");

				if (!melonutilsDir.toFile().exists()) {
					player.sendMessage("melonutilities direcory not found");
					return 1;
				}

				Gson gson = new GsonBuilder().setPrettyPrinting().create();

				File melonutilsConfig = new File(melonutilsDir.toFile(), "config.json");
				// Load full JSON document
				JsonObject root = null;
				try {
					root = JsonParser
						.parseReader(new FileReader(melonutilsConfig))
						.getAsJsonObject();
				} catch (FileNotFoundException e) {
					BTAEssentials.LOGGER.error(
						"Failed to load file: {}",
						melonutilsConfig,
						new RuntimeException(e)
					);
					player.sendMessage("Failed to load config.json");
					return 1;
				}

				JsonObject mainConfig = root.getAsJsonObject("Main Config");

				if (mainConfig == null) {
					player.sendMessage("Main Config section not found in config.json");
					return 1;
				}

				// Apply the changes
				mainConfig.addProperty("enableTPA", false);
				mainConfig.addProperty("enableHomes", false);
				mainConfig.addProperty("enableWarps", false);
				mainConfig.addProperty("enableRules", false);

				// Save the config
				try {
					Files.write(
						melonutilsConfig.toPath(),
						gson.toJson(root).getBytes(StandardCharsets.UTF_8)
					);
					player.sendMessage("Config updated successfully.");
				} catch (Exception e) {
					BTAEssentials.LOGGER.error("Failed to save modified config.json", e);
					player.sendMessage("Failed to save config.json");
				}

				// Locate the "Warp Data" → "warps" array
				JsonArray warpsArray = root.getAsJsonObject("Warp Data").getAsJsonArray("warps");

				// Convert JSON array to List<Warp>
				List<MelonPosition> warpList = gson.fromJson(
					warpsArray, new TypeToken<List<MelonPosition>>() {
					}.getType()
				);

				// Fill the hashmap
				HashMap<String, WorldPosition> warpMap = new HashMap<>();

				for (MelonPosition w : warpList) {
					warpMap.put(w.name, new WorldPosition(w.x, w.y, w.z, w.dimID));
				}

				Warps.importWarps(warpMap);

				File   melonutilsPlayerDir = melonutilsDir.resolve("users").toFile();
				File[] playersDirList      = melonutilsPlayerDir.listFiles();

				if (playersDirList != null) {
					ChunkCoordinates spawnCC = source.getWorld().getSpawnPoint();
					WorldPosition spawn = new WorldPosition(
						spawnCC.x,
						spawnCC.y,
						spawnCC.z,
						0
					);

					for (File melonUserfile : playersDirList) {
						try {
							loadPlayerFile(melonUserfile, spawn);
						} catch (Exception e) {
							BTAEssentials.LOGGER.error(
								"Failed to load userfile: {}",
								melonUserfile,
								new RuntimeException(e)
							);
						}
					}
				} else {
					player.sendMessage("melonutilities/users direcory not found");
				}

				return 1;
			}));
	}

	private void loadPlayerFile(File melonUserFile, WorldPosition spawn) throws Exception {
		JsonObject melonPlayerJson = JsonParser
			.parseReader(new FileReader(melonUserFile))
			.getAsJsonObject();
		Gson gson = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(Instant.class, new InstantTypeAdapter())
			.create();
		String uuid = melonPlayerJson
			.getAsJsonObject("User Data")
			.getAsJsonPrimitive("userUUID")
			.getAsString();

		JsonArray homesArray = melonPlayerJson.getAsJsonObject("Home Data").getAsJsonArray("homes");

		List<MelonPosition> homesList = gson.fromJson(
			homesArray, new TypeToken<List<MelonPosition>>() {
			}.getType()
		);

		HashMap<String, WorldPosition> homesMap = new HashMap<>();

		if (homesList != null) {
			for (MelonPosition home : homesList) {
				homesMap.put(home.name, new WorldPosition(home.x, home.y, home.z, home.dimID));
			}
		}

		MockPlayerData playerData = new MockPlayerData();
		playerData.lastTPTime = Instant.now().minus(Duration.ofSeconds(BTAEssentials.TPTimeout));
		playerData.backPos = spawn;
		playerData.homes = homesMap;

		String json     = gson.toJson(playerData);
		File   saveFile = new File(BTAEssentials.PLAYER_DIR.toFile(), uuid + ".json");

		Files.write(saveFile.toPath(), json.getBytes(StandardCharsets.UTF_8));

	}

	private static class MockPlayerData implements Serializable {
		public Instant                        lastTPTime;
		public WorldPosition                  backPos;
		public HashMap<String, WorldPosition> homes;
	}

	private static class MelonPosition {
		public String name;
		public double x;
		public double y;
		public double z;
		public int    dimID;
	}
}
