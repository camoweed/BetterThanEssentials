package wyspr.BTE.commands.warp;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import wyspr.BTE.Essentials;
import wyspr.BTE.utils.Warps;

import java.util.List;

@SuppressWarnings("ALL") public class WarpsCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("warps")
			.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.WarpCommand)
			.executes(context -> {
				CommandSource source = (CommandSource) context.getSource();
				Player        player = source.getSender();
				List<String>  warps  = Warps.getWarps();

				if (warps.isEmpty()) {
					player.sendMessage(TextFormatting.ORANGE + "There are no warps!");
					return 1;
				}

				String warpsString = String.join(", ", warps);
				player.sendMessage(TextFormatting.ORANGE + "Warps: " + TextFormatting.YELLOW + warpsString);

				return 1;
			}));
	}
}
