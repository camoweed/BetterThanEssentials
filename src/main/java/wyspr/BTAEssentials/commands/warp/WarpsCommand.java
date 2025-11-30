package wyspr.BTAEssentials.commands.warp;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import wyspr.BTAEssentials.BTAEssentials;
import wyspr.BTAEssentials.utils.Warps;

import java.util.List;

@SuppressWarnings("ALL") public class WarpsCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("warps")
			.requires(source -> ((CommandSource) source).hasAdmin() || BTAEssentials.WarpCommand)
			.executes(context -> {
				CommandSource source     = (CommandSource) context.getSource();
				Player        player     = source.getSender();

				List<String> warps = Warps.getWarps();

				if (warps.isEmpty()) {
					player.sendMessage("§1There are no warps!");
					return 1;
				}

				String warpsString = String.join(", ", warps);
				player.sendMessage("§1Warps: §4" + warpsString);

				return 1;
			}));
	}
}
