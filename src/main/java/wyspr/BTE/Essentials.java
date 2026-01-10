package wyspr.BTE;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.util.GameStartEntrypoint;
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
import wyspr.BTE.utils.AllUsersMap;
import wyspr.BTE.utils.ConfigBuilder;
import wyspr.BTE.utils.WarpsManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Essentials implements DedicatedServerModInitializer, GameStartEntrypoint {
	public static final String            MOD_ID     = "BTEssentials";
	public static final Logger            LOGGER     = LoggerFactory.getLogger(MOD_ID);
	public static final TomlConfigHandler CFG;
	///  server/config/
	public static final Path              CFG_DIR    = FabricLoader
		.getInstance()
		.getConfigDir();
	///  server/config/BTEssentials
	public static final Path              DATA_DIR   = CFG_DIR.resolve(MOD_ID);
	///  server/config/BTEssentials/players
	public static final Path              PLAYER_DIR = DATA_DIR.resolve("players");
	public static final int               MAX_MAILS  = 36;
	public static       ConfigBuilder     info;
	public static       ConfigBuilder     rules;
	// Options
	public static final String            MOTD;
	public static final int               NickLength;
	public static final int               TPTimeout;
	public static final double            DeathCost;
	public static final int               DisableTNTOverworld;
	public static final int               DisableTNTNether;
	public static final int               DisableTNTSky;
	public static final int               AddedTicksCatchable;
	public static final boolean           EnableAntiTrampleFence;
	public static final boolean           DisableTrample;
	public static final boolean           DisableBedExplosion;
	public static final boolean           HeadSit;
	public static final boolean           HeadSitSaddle;
	public static final boolean           ColorChat;
	public static final boolean           GreenText;
	// Home
	public static final boolean           HomeCommand;
	public static final int               MaxHomes;
	public static final int               HomeCost;
	// Back
	public static final  boolean           BackCommand;
	public static final  int               BackCost;
	public static final  boolean           BackOnDeath;
	// TPA
	public static final boolean           TPACommand;
	public static final int               TPACost;
	// Warp
	public static final boolean           WarpCommand;
	public static final int               WarpCost;
	// RTP
	public static final boolean           RTPCommand;
	public static final int               RTPCost;
	public static final int               RTPMin;
	public static final int               RTPMax;
	// Command toggles
	public static final boolean           SpawnCommand;
	public static final boolean           ClearCommand;
	public static final boolean           PayCommand;
	public static final boolean           CraftCommand;
	public static final boolean           GiveCommand;
	public static final boolean           GamemodeCommand;
	public static final boolean           FixCommand;
	// Sounds
	public static final String            TeleportSound;
	public static final String            TPANotificationSound;
	public static final String            MailNotificationSound;
	public static final String            MutedSound;

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

		//noinspection SpellCheckingInspection
		cfg.addCategory(
			"Sounds can be found in bta.jar/assets/minecraft/sounds/sounds.json e.g. note.snare, mob.sheep, mob.skeletondeath. Pitch and volume can optionally be specified after the sound, separated by colons e.g note.celesta:1:2",
			"Sounds"
		);
		cfg.addEntry(
			"Sounds.TeleportSound",
			"Plays when the player teleports",
			"random.explode:2:2"
		);
		cfg.addEntry(
			"Sounds.TPANotificationSound",
			"Plays when the user receives a TPA request",
			"note.harp:1:2"
		);
		cfg.addEntry(
			"Sounds.MailNotificationSound",
			"Plays when the user receives mail",
			"random.page:2:0.5"
		);
		cfg.addEntry("Sounds.MutedSound", "Plays when the user chats while muted", "note.chant:2:0");

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
		// Sounds
		TeleportSound = CFG.getString("Sounds.TeleportSound");
		TPANotificationSound = CFG.getString("Sounds.TPANotificationSound");
		MailNotificationSound = CFG.getString("Sounds.MailNotificationSound");
		MutedSound = CFG.getString("Sounds.MutedSound");

	}

	@Override
	public void onInitializeServer() {
		System.out.println("+-----------------------------------+");
		System.out.println("| Better than Essentials loading... |");
		System.out.println("+-----------------------------------+");

		for (Path dir : new Path[]{DATA_DIR, PLAYER_DIR}) {
			if (!Files.exists(dir)) {
				try {
					Files.createDirectories(dir);
					LOGGER.info("Created {}", dir);
				} catch (IOException e) {
					LOGGER.error("Could not create: {}", dir, e);
				}
			}
		}

		initInfo();
		initRules();
		initCommands();
		WarpsManager.load();
		AllUsersMap.load();

		System.out.println("+-------------------------------------+");
		System.out.println("| Better than Essentials initialized! |");
		System.out.println("+-------------------------------------+");
	}

	static void initInfo() {
		info = new ConfigBuilder(
			"Info", Arrays.asList(
			"<aqua>Thanks for installing Better Than Essentials!<r>",
			"<yellow>This is an automatically generated message<r>",
			"<lime>you may customize it in the config folder!<r>",
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
			"  <yellow>- No cheating<r>",
			"  <yellow>- No harassing<r>",
			"  <yellow>- No Minecraft youtuber shenanigans<r>",
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
		CommandManager.registerServerCommand(new DraftCommand());
		CommandManager.registerServerCommand(new DisconnectCommand());
		CommandManager.registerServerCommand(new FixCommand());
		CommandManager.registerServerCommand(new FireballCommand());
		CommandManager.registerServerCommand(new ImportMelonUtilsCommand());
		CommandManager.registerServerCommand(new GodCommand());
		CommandManager.registerServerCommand(new InvseeCommand());
		CommandManager.registerServerCommand(new LeaveBedCommand());
		CommandManager.registerServerCommand(new MailCommand());
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
	public void beforeGameStart() {
	}

	@Override
	public void afterGameStart() {
		MinecraftServer mcs = MinecraftServer.getInstance();
		mcs.motd = MOTD.replace("$$", "§");
	}
}
