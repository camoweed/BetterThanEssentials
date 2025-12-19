package wyspr.BTE;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.util.GameStartEntrypoint;
import turniplabs.halplibe.util.RecipeEntrypoint;
import turniplabs.halplibe.util.TomlConfigHandler;
import turniplabs.halplibe.util.toml.Toml;
import wyspr.BTE.commands.*;
import wyspr.BTE.commands.TPA.*;
import wyspr.BTE.commands.gamemode.CreativeCommand;
import wyspr.BTE.commands.gamemode.SpectatorCommand;
import wyspr.BTE.commands.gamemode.SurvivalCommand;
import wyspr.BTE.commands.home.DelhomeCommand;
import wyspr.BTE.commands.home.HomeCommand;
import wyspr.BTE.commands.home.HomesCommand;
import wyspr.BTE.commands.home.SethomeCommand;
import wyspr.BTE.commands.warp.DelWarpCommand;
import wyspr.BTE.commands.warp.SetWarpCommand;
import wyspr.BTE.commands.warp.WarpCommand;
import wyspr.BTE.commands.warp.WarpsCommand;
import wyspr.BTE.utils.ConfigBuilder;
import wyspr.BTE.utils.Warps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;


public class Essentials implements ModInitializer, RecipeEntrypoint, GameStartEntrypoint {
	public static final String            MOD_ID     = "BTEssentials";
	public static final Logger            LOGGER     = LoggerFactory.getLogger(MOD_ID);
	public static final TomlConfigHandler CFG;
	//  server/config/
	public static final Path              CFG_DIR    = Paths.get("config");
	//  server/config/BTEssentials
	public static final Path              DATA_DIR   = CFG_DIR.resolve(MOD_ID);
	//  server/config/BTEssentials/players
	public static final Path              PLAYER_DIR = DATA_DIR.resolve("players");
	public static       ConfigBuilder     info;
	public static       ConfigBuilder     rules;
	// Options
	public static       String            MOTD;
	public static       int               NickLength;
	public static       int               TPTimeout;
	public static       double            DeathCost;
	public static       int               DisableTNTOverworld;
	public static       int               DisableTNTNether;
	public static       int               DisableTNTSky;
	public static       int               AddedTicksCatchable;
	public static       boolean           EnableAntiTrampleFence;
	public static       boolean           DisableTrample;
	public static       boolean           DisableBedExplosion;
	public static       boolean           HeadSit;
	public static       boolean           HeadSitSaddle;
	public static       boolean           ColorChat;
	public static       boolean           GreenText;
	// Home
	public static       boolean           HomeCommand;
	public static       int               MaxHomes;
	public static       int               HomeCost;
	// Back
	public static       boolean           BackCommand;
	public static       int               BackCost;
	public static       boolean           BackOnDeath;
	// TPA
	public static       boolean           TPACommand;
	public static       int               TPACost;
	// Warp
	public static       boolean           WarpCommand;
	public static       int               WarpCost;
	// RTP
	public static       boolean           RTPCommand;
	public static       int               RTPCost;
	public static       int               RTPMin;
	public static       int               RTPMax;
	// Command toggles
	public static       boolean           SpawnCommand;
	public static       boolean           ClearCommand;
	public static       boolean           PayCommand;
	public static       boolean           CraftCommand;
	public static       boolean           GiveCommand;
	public static       boolean           GamemodeCommand;
	public static       boolean           FixCommand;

	public static       String            TeleportSound;

	static {
		Toml cfg = new Toml();
		cfg.addCategory("Options");
		cfg.addEntry("Options.MOTD", "Message of the day, rendered in server list.", "§5§lWelcome!");
		cfg.addEntry("Options.NickLength", "Nickname length limit, default = 16", 16);
		cfg.addEntry(
			"Options.TPTimeout",
			"Number of seconds between uses of /tpa, /rtp, /back, and /home",
			15
		);
		cfg.addEntry(
			"Options.DeathCost",
			"Point ratio after death. Points will be multiplied with this number upon death. 1.0 keeps all points, 0.0 is default behavior.",
			0.0
		);
		cfg.addEntry(
			"Options.DisableTNTOverworld",
			"Disable TNT in the overworld above this y level. 0 = no TNT",
			256
		);
		cfg.addEntry(
			"Options.DisableTNTNether",
			"Disable TNT in the nether above this y level. 0 = no TNT",
			256
		);
		cfg.addEntry(
			"Options.DisableTNTSky",
			"Disable TNT in the sky dimension above this y level. 0 = no TNT",
			256
		);
		cfg.addEntry(
			"Options.AddTicksCatchableFishing",
			"Adds time in ticks to the window of time that you can catch fish, very useful for players with higher ping. 10-15 is recommended. 0 to disable, -40 or lower to make fishing impossible.",
			0
		);
		cfg.addEntry(
			"Options.EnableAntiTrampleFences",
			"Re-enable farmland trample prevention by putting fences under them",
			false
		);
		cfg.addEntry("Options.DisableTrample", "Completely disables trampling crops", false);
		cfg.addEntry(
			"Options.DisableBedExplosions",
			"Remove explosions from beds in non-respawn dimensions.",
			false
		);
		cfg.addEntry(
			"Options.headSit",
			"Allows players to sit on each other's heads when holding nothing in their hand.",
			false
		);
		cfg.addEntry(
			"Options.headSitSaddle",
			"Requires players to wear a saddle on their head to be ridden.",
			true
		);
		cfg.addEntry(
			"Options.colourChat",
			"Allows players to use $$ as colour code for colourful chatting, obfuscation is disabled.",
			false
		);
		cfg.addEntry(
			"Options.greenText",
			"Allows players to turn their text green by putting '>' at the start of their messages.",
			true
		);

		cfg.addCategory("Home");
		cfg.addEntry("Home.Command", "Let non-opped players use /home commands.", true);
		cfg.addEntry("Home.Max", "Max amount of homes a player may have. 0 = infinite", 15);
		cfg.addEntry("Home.Cost", "Amount of points that /home will take on use.", 0);

		cfg.addCategory("Back");
		cfg.addEntry("Back.Command", "Let non-opped players use /back", true);
		cfg.addEntry("Back.OnDeath", "Allow /back to return a player to their death point.", true);
		cfg.addEntry("Back.Cost", "Amount of points that /back will take on use.", 0);

		cfg.addCategory("TPA");
		cfg.addEntry("TPA.Command", "Let non-opped players use /tpa", true);
		cfg.addEntry("TPA.Cost", "Amount of points that /tpa will take on use.", 0);

		cfg.addCategory("Warp");
		cfg.addEntry("Warp.Command", "Let non-opped players use /warp", true);
		cfg.addEntry("Warp.Cost", "Amount of points that /warp will take on use.", 0);

		cfg.addCategory("RTP");
		cfg.addEntry("RTP.Command", "Let non-opped players use /rtp", true);
		cfg.addEntry("RTP.Cost", "Amount of points that /rtp will take on use.", 0);
		cfg.addEntry("RTP.Min", "Minimum distance that /rtp will send a player", 100_000);
		cfg.addEntry("RTP.Max", "Maximum distance that /rtp will send a player", 200_000);

		cfg.addCategory("Commands");
		cfg.addEntry("Commands.Spawn", "Let non-opped players use /spawn.", true);
		cfg.addEntry("Commands.Clear", "Let non-opped players use /clear.", false);
		cfg.addEntry("Commands.Pay", "Let non-opped players use /pay.", true);
		cfg.addEntry("Commands.Craft", "Let non-opped players use /craft", false);
		cfg.addEntry("Commands.Give", "Let non-opped players use /give.", false);
		cfg.addEntry("Commands.Gamemode", "Let non-opped players use /gamemode.", false);
		cfg.addEntry("Commands.FixCommand", "Let non-opped players use /fix.", false);

		cfg.addEntry("Options.TeleportSound", "Sounds can be found in bta.jar/assets/minecraft/sounds/sounds.json e.g. note.snare, mob.sheep, mob.skeletondeath.", "random.explode");
		CFG = new TomlConfigHandler(MOD_ID, cfg);

		// Options
		MOTD                   = CFG.getString("Options.MOTD");
		NickLength             = CFG.getInt("Options.NickLength");
		TPTimeout              = CFG.getInt("Options.TPTimeout");
		DeathCost              = CFG.getDouble("Options.DeathCost");
		DisableTNTOverworld    = CFG.getInt("Options.DisableTNTOverworld");
		DisableTNTNether       = CFG.getInt("Options.DisableTNTNether");
		DisableTNTSky          = CFG.getInt("Options.DisableTNTSky");
		AddedTicksCatchable    = CFG.getInt("Options.AddTicksCatchableFishing");
		EnableAntiTrampleFence = CFG.getBoolean("Options.EnableAntiTrampleFences");
		DisableTrample         = CFG.getBoolean("Options.DisableTrample");
		DisableBedExplosion    = CFG.getBoolean("Options.DisableBedExplosions");
		HeadSit                = CFG.getBoolean("Options.headSit");
		HeadSitSaddle          = CFG.getBoolean("Options.headSitSaddle");
		ColorChat              = CFG.getBoolean("Options.colourChat");
		GreenText              = CFG.getBoolean("Options.greenText");
		// Home
		HomeCommand = CFG.getBoolean("Home.Command");
		HomeCost    = CFG.getInt("Home.Cost");
		MaxHomes    = CFG.getInt("Home.Max");
		// Back
		BackCommand = CFG.getBoolean("Back.Command");
		BackCost    = CFG.getInt("Back.Cost");
		BackOnDeath = CFG.getBoolean("Back.OnDeath");
		// TPA
		TPACommand = CFG.getBoolean("TPA.Command");
		TPACost    = CFG.getInt("TPA.Cost");
		// Warp
		WarpCommand = CFG.getBoolean("Warp.Command");
		WarpCost    = CFG.getInt("Warp.Cost");
		// RTP
		RTPCommand = CFG.getBoolean("RTP.Command");
		RTPCost    = CFG.getInt("RTP.Cost");
		RTPMin     = CFG.getInt("RTP.Min");
		RTPMax     = CFG.getInt("RTP.Max");
		// Commands
		SpawnCommand    = CFG.getBoolean("Commands.Spawn");
		ClearCommand    = CFG.getBoolean("Commands.Clear");
		PayCommand      = CFG.getBoolean("Commands.Pay");
		CraftCommand    = CFG.getBoolean("Commands.Craft");
		GiveCommand     = CFG.getBoolean("Commands.Give");
		GamemodeCommand = CFG.getBoolean("Commands.Gamemode");
		FixCommand      = CFG.getBoolean("Commands.FixCommand");

		TeleportSound                = CFG.getString("Options.TeleportSound");
	}

	@Override
	public void onInitialize() {
		System.out.println("┌───────────────────────────────────┐");
		System.out.println("│ Better than Essentials loading... │");
		System.out.println("└───────────────────────────────────┘");

		for (Path dir : new Path[]{DATA_DIR, PLAYER_DIR}) {
			if (!Files.exists(dir)) {
				try {
					Files.createDirectory(dir);
					LOGGER.info("Created {}", dir);
				} catch (IOException e) {
					LOGGER.error("Could not create: {}", dir, new RuntimeException(e));
				}
			}
		}

		initInfo();
		initRules();
		initCommands();
		Warps.load();

		System.out.println("┌─────────────────────────────────────┐");
		System.out.println("│ Better than Essentials initialized! │");
		System.out.println("└─────────────────────────────────────┘");
	}

	static void initInfo() {
		info = new ConfigBuilder(
			"Info", Arrays.asList(
			"<aqua>Thanks for installing Better Than Essentials!<r>",
			"<yellow>this is an automatically generated message<r>",
			"<lime>and you may customize it in the config folder!<r>",
			"///  ----------------==================== INFO ===================-----------------",
			"///",
			"/// - You are able to add more pages to info and rules by following this format",
			"///     Example: /info 2 = Info2.txt",
			"/// - These files update live so be mindful of any changes you save to the disk",
			"///",
			"///  ----------------=================== SYNTAX ==================-----------------",
			"///",
			"/// - Lines staring with '///' are a comment and are not displayed to the player. ",
			"///",
			"/// - Use html like tags for formatting",
			"///    Example: <red><b>BOLD RED<r> normal text",
			"/// - You can escape the '<' and '>' symbols with a '\\'",
			"///    Example: \\<blue>",
			"///",
			"///  Formatting tags: ",
			"///             +-----------------------------------------------+",
			"///             | white   | gray    | grey   | silver | black   |",
			"///             |---------+---------+--------+--------+---------|",
			"///             | red     | orange  | yellow | green  | blue    |",
			"///             |---------+---------+--------+--------+---------|",
			"///             | magenta | brown   | cyan   | lime   | aqua    |",
			"///             |---------+---------+--------+--------+---------+",
			"///             | i = italics       | purple | pink   |",
			"///             |-------------------+--------+--------|",
			"///             | u = underline     | b = bold        |",
			"///             |-------------------+-----------------|",
			"///             | r / reset = reset | s = strike      |",
			"///             |-------------------+-----------------+",
			"///             | o = obfuscate     |",
			"///             +-------------------+"
		), true
		);
	}

	static void initRules() {
		rules = new ConfigBuilder(
			"Rules", Arrays.asList(
			"<aqua>Basic rules:<r>",
			"  <yellow>No cheating<r>",
			"  <yellow>No harassing<r>",
			"  <yellow>No minecraft youtuber shenanigans<r>",
			"///  -----------------=================== INFO ===================-----------------",
			"///                                                                                ",
			"///",
			"/// - You are able to add more pages to info and rules by following this format",
			"///     Example: /rules 2 = Rules2.txt",
			"/// - These files update live so be mindful of any changes you save to the disk",
			"///",
			"///  -----------------================== SYNTAX ==================-----------------",
			"///",
			"/// - Lines staring with '///' are a comment and are not displayed to the player. ",
			"///",
			"/// - Use html like tags for formatting",
			"///    Example: <red><b>BOLD RED<r> normal text",
			"/// - You can escape the '<' and '>' symbols with a '\\'",
			"///    Example: \\<blue>",
			"///",
			"///  Formatting tags: ",
			"///             +-----------------------------------------------+",
			"///             | white   | gray    | grey   | silver | black   |",
			"///             |---------+---------+--------+--------+---------|",
			"///             | red     | orange  | yellow | green  | blue    |",
			"///             |---------+---------+--------+--------+---------|",
			"///             | magenta | brown   | cyan   | lime   | aqua    |",
			"///             |---------+---------+--------+--------+---------+",
			"///             | i = italics       | purple | pink   |",
			"///             |-------------------+--------+--------|",
			"///             | u = underline     | b = bold        |",
			"///             |-------------------+-----------------|",
			"///             | r / reset = reset | s = strike      |",
			"///             |-------------------+-----------------+",
			"///             | o = obfuscate     |",
			"///             +-------------------+"
		), true
		);
	}

	void initCommands() {
		CommandManager.registerServerCommand(new BackCommand());
		CommandManager.registerServerCommand(new ColorsCommand());
		CommandManager.registerServerCommand(new CraftingCommand());
		CommandManager.registerServerCommand(new DisconnectCommand());
		CommandManager.registerServerCommand(new FixCommand());
		CommandManager.registerServerCommand(new FireballCommand());
		CommandManager.registerServerCommand(new ImportMelonUtilsCommand());
		CommandManager.registerServerCommand(new GodCommand());
		CommandManager.registerServerCommand(new InvseeCommand());
		CommandManager.registerServerCommand(new LeaveBedCommand());
		CommandManager.registerServerCommand(new MOTDCommand());
		CommandManager.registerServerCommand(new OPChatCommand());
		CommandManager.registerServerCommand(new PayCommand());
		CommandManager.registerServerCommand(new PingCommand());
		CommandManager.registerServerCommand(new RTPCommand());
		CommandManager.registerServerCommand(new SudoCommand());
		CommandManager.registerServerCommand(new TntCommand());
		CommandManager.registerServerCommand(new TrollCommand());
		CommandManager.registerServerCommand(new SmiteCommand());
		CommandManager.registerServerCommand(new VanishCommand());

		CommandManager.registerServerCommand(new InfoCommand());
		CommandManager.registerServerCommand(new RulesCommand());

		CommandManager.registerServerCommand(new MuteCommand());
		CommandManager.registerServerCommand(new UnmuteCommand());

		CommandManager.registerServerCommand(new CreativeCommand());
		CommandManager.registerServerCommand(new SpectatorCommand());
		CommandManager.registerServerCommand(new SurvivalCommand());

		CommandManager.registerServerCommand(new DelhomeCommand());
		CommandManager.registerServerCommand(new HomeCommand());
		CommandManager.registerServerCommand(new HomesCommand());
		CommandManager.registerServerCommand(new SethomeCommand());

		CommandManager.registerServerCommand(new TPAAllCommand());
		CommandManager.registerServerCommand(new TPACommand());
		CommandManager.registerServerCommand(new TPAHereCommand());
		CommandManager.registerServerCommand(new TPConfirmCommand());
		CommandManager.registerServerCommand(new TPDenyCommand());
		CommandManager.registerServerCommand(new TPRequestsCommand());

		CommandManager.registerServerCommand(new DelWarpCommand());
		CommandManager.registerServerCommand(new SetWarpCommand());
		CommandManager.registerServerCommand(new WarpCommand());
		CommandManager.registerServerCommand(new WarpsCommand());
	}

	@Override
	public void onRecipesReady() {}

	@Override
	public void initNamespaces() {}

	@Override
	public void beforeGameStart() {}

	@Override
	public void afterGameStart() {
		MinecraftServer mcs = MinecraftServer.getInstance();
		mcs.motd = MOTD.replace("$$", "§");
	}
}
