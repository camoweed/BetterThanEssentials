package wyspr.BTE.commands.TPA;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import wyspr.BTE.Essentials;
import wyspr.BTE.utils.PlayerData;

import java.util.List;

@SuppressWarnings("ALL") public class TPRequestsCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {

		String[] literals = {"tpreq", "tpr", "tprequests"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.TPACommand)
				.executes(context -> {
					CommandSource source     = (CommandSource) context.getSource();
					boolean       isAdmin    = source.hasAdmin();
					Player        player     = source.getSender();
					PlayerData    playerData = PlayerData.get(player);
					List<String> allRequests = playerData.tpManager.getAllRequests();

					if (allRequests.isEmpty()) {
						player.sendMessage(TextFormatting.ORANGE + "No current TP requests");
					}

					String requests = String.join(", ", allRequests);

					player.sendMessage(TextFormatting.ORANGE + "Current TP requests: " + requests);

					return 1;
				}));

		}
	}
}
