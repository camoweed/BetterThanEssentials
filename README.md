# Better Than Essentials

An Essentials-like plugin for Better Than Adventure servers!

Adds various new commands and features for server administration and gameplay enhancement.

## Commands

| Command       | OP Required | Config Toggle | Aliases                           | Description                                                                                                |
|---------------|:-----------:|:-------------:|-----------------------------------|------------------------------------------------------------------------------------------------------------|
| `/back`       |      ❌      |       ✅       |                                   | Go back                                                                                                    |
| `/colorcodes` |      ❌      |       ❌       | `/colourcodes`                    | Print a list of formatting codes                                                                           |
| `/craft`      |      ❌      |       ✅       | `/craftingtable`, `/craft`, `/cb` | Opens a crafting table                                                                                     |
| `/delhome`    |     ❌/✅     |       ✅       | `/rmhome`                         | Remove a home<br><sup>(OPs can remove other players homes)                                                 |
| `/delwarp`    |      ✅      |       ❌       | `/rmwarp`                         | Remove a warp                                                                                              |
| `/fix`        |      ❌      |       ✅       | `/repair`                         | Repairs currently held item                                                                                |
| `/give`       |      ❌      |       ✅       | `/i`                              | Shorthand for giving items                                                                                 |
| `/gm`         |      ❌      |       ✅       | `/gms`, `/gmc`, `/gmsp`           | Shorthand for changing game modes                                                                          |
| `/home`       |     ❌/✅     |       ✅       |                                   | Travel to a home<sup><br>(With no argument, defaults to "home")<br>(OPs can travel to other players homes) |
| `/homes`      |      ❌      |       ✅       |                                   | Lists your homes<br><sup>(OPs can view other players homes)                                                |
| `/info`       |      ❌      |       ❌       |                                   | Prints info pages into chat, see below for creating info pages                                             |
| `/invsee`     |      ✅      |       ❌       | `/openinv`                        | View and modify other players inventories                                                                  |
| `/motd`       |     ❌/✅     |       ❌       |                                   | View and edit the server MOTD<br><sup>(Anyone can view, OPs can edit)                                      |
| `/mute`       |      ✅      |       ❌       |                                   | Stop players from chatting<br><sup>(OPs can still see muted messages)                                      |
| `/opchat`     |      ✅      |       ❌       | `/chatop`, `/opc`                 | A private chat channel for OPs                                                                             |
| `/pay`        |      ❌      |       ✅       |                                   | Send points to another player                                                                              |
| `/ping`       |      ❌      |       ❌       |                                   | Pong!                                                                                                      |
| `/rtp`        |      ❌      |       ✅       |                                   | Random teleport in a configurable area                                                                     |
| `/rules`      |      ❌      |       ❌       |                                   | Prints rules pages into chat, see below for creating rules pages                                           | 
| `/sethome`    |      ❌      |       ✅       | `/addhome`                        | Adds a home<br><sup>(With no argument, defaults to home")                                                  |
| `/setwarp`    |      ✅      |       ❌       | `/addwarp`                        | Adds a warp                                                                                                |
| `/sudo`       |      ✅      |       ❌       | `/doas`                           | Run commands as another player                                                                             |
| `/tpall`      |      ✅      |       ✅       | `/tpaall`                         | Sends a tpa request to all players                                                                         |
| `/tpa`        |      ❌      |       ✅       | `/tpask`                          | Sends a request to teleport yourself to a player                                                           |
| `/tpahere`    |      ❌      |       ✅       | `/tphere` , `/tph`                | Sends a request to teleport a player to you                                                                |
| `/tpconfirm`  |      ❌      |       ✅       | `/ty` , `/tpyes`                  | Accepts a teleport request                                                                                 |
| `/tpdeny`     |      ❌      |       ✅       | `/tn` , `/tpno`                   | Denies a teleport request                                                                                  |
| `/tprequests` |      ❌      |       ✅       | `/tpreq` , `/tpr`                 | Prints teleport requests into chat                                                                         |
| `/vanish`     |      ✅      |       ❌       |                                   | Extend spectator mode to hide your name from the playerlist                                                |
| `/warp`       |      ❌      |       ✅       |                                   | Travel to a warp                                                                                           |
| `/warps`      |      ❌      |       ✅       |                                   | Lists the available warps                                                                                  |

## Features

*(Toggleable in the configuration file)*

- Head Sit: Allow players to ride on each-others heads
- Set nickname max length
- Disable crop trample with fences below
- Disable crop trample overall
- Teleport cooldown
- Per-world TNT controls: Set a Y level that TNT will stop working below. 
- Disable bed explosions in non-sleeping dimensions
- Greentext in chat by starting a message with "> "
- Selection symbol <sup>(§)</sup> in chat with $$
- Lose a portion of points on death (instead of all)
- Custom rules and info pages

## Rules and Info

### Pages

The `info` and `rules` directories will be created automatically in the plugin configuration folder and populated with `Info.txt` and `Rules.txt` respectively.
The commands read these files as pages. 
#### Examples: 
- `/info`   = `config/BTEssentials/info/Info.txt`
- `/info 1` = `config/BTEssentials/info/Info.txt`
- `/info 2` = `config/BTEssentials/info/Info2.txt`

### Syntax

Rules and info have a text parser that allows html-like tags to be used to format the pages. 

- Lines staring with '///' are a comment and are not displayed to the player.
- Format example: `<red><b>BOLD RED<r> normal text`
- Comment example: `/// this is a comment that will not be shown in game!`

### Formatting Tags


|   Color    |        Aliases         |
|:----------:|:----------------------:|
|   white    |                        |
|    gray    |          grey          |
| light_gray |  silver / light_grey   |
|   black    |                        |
|   brown    |                        |
|    pink    |                        |
|    red     |                        |
|   orange   |                        |
|   yellow   |                        |
|   green    |                        |
|    lime    |                        |
| light_blue |          aqua          |
|    cyan    |                        |
|    blue    |                        |
|  magenta   |                        |
|   purple   |                        |
|     o      |    obf / obfuscated    |
|     b      |          bold          |
|     s      | strike / strikethrough |
|     u      |       underline        |
|     i      |         italic         |
|     r      |         reset          |

---

#### Default configuration file

```
[Options]
	# Message of the day, shows up in server list.
	MOTD = "§5§lWelcome!"
	# Nickname length limit, default = 16
	NickLength = 16
	# Number of seconds between uses of /tpa, /rtp, /back, and /home
	TPTimeout = 15
	# Point ratio after death. Points will be multiplied with this number upon death. 1.0 keeps all points, 0.0 is default behavior.
	DeathCost = 0.0
	# Disable TNT in the overworld above this y level. 0 = no TNT
	DisableTNTOverworld = 256
	# Disable TNT in the nether above this y level. 0 = no TNT
	DisableTNTNether = 256
	# Disable TNT in the sky dimension above this y level. 0 = no TNT
	DisableTNTSky = 256
	# Adds time in ticks to the window of time that you can catch fish, very useful for players with higher ping. 10-15 is recommended. 0 to disable, -40 or lower to make fishing impossible.
	AddTicksCatchableFishing = 0
	# Re-enable farmland trample prevention by putting fences under them
	EnableAntiTrampleFences = false
	# Completely disables trampling crops
	DisableTrample = false
	# Remove explosions from beds in non-respawn dimensions.
	DisableBedExplosions = false
	# Allows players to sit on each other's heads when holding nothing in their hand.
	headSit = false
	# Requires players to wear a saddle on their head to be ridden.
	headSitSaddle = true
	# Allows players to use $$ as colour code for colourful chatting, obfuscation is disabled.
	colourChat = false
	# Allows players to turn their text green by putting '>' at the start of their messages.
	greenText = true

[Home]
	# Let non-opped players use /home commands.
	Command = true
	# Max amount of homes a player may have. 0 = infinite
	Max = 15
	# Amount of points that /home will take on use.
	Cost = 0

[Back]
	# Let non-opped players use /back
	Command = true
	# Allow /back to return a player to their death point.
	OnDeath = true
	# Amount of points that /back will take on use.
	Cost = 0

[TPA]
	# Let non-opped players use /tpa
	Command = true
	# Amount of points that /tpa will take on use.
	Cost = 0

[Warp]
	# Let non-opped players use /warp
	Command = true
	# Amount of points that /warp will take on use.
	Cost = 0

[RTP]
	# Let non-opped players use /rtp
	Command = true
	# Amount of points that /rtp will take on use.
	Cost = 0
	# Minimum distance that /rtp will send a player
	Min = 100000
	# Maximum distance that /rtp will send a player
	Max = 200000

[Commands]
	# Let non-opped players use /spawn.
	Spawn = true
	# Let non-opped players use /clear.
	Clear = false
	# Let non-opped players use /pay.
	Pay = true
	# Let non-opped players use /craft
	Craft = false
	# Let non-opped players use /give.
	Give = false
	# Let non-opped players use /gamemode.
	Gamemode = false
	# Let non-opped players use /fix.
	FixCommand = false
```
