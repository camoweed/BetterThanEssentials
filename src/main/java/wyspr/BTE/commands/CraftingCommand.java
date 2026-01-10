package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import wyspr.BTE.Essentials;
import wyspr.BTE.utils.PlayerData;

@SuppressWarnings("ALL")
public class CraftingCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		String[] literals = {"craft", "crafting", "cb", "craftingtable"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.CraftCommand)
				.executes(context -> {
					CommandSource source = (CommandSource) context.getSource();
					Player        player = source.getSender();
					PlayerData.get(player).craftCommandOpen = true;
					player.displayWorkbenchScreen((int) player.x, (int) player.y, (int) player.z);
					return 1;
				}));

		}
	}
}
