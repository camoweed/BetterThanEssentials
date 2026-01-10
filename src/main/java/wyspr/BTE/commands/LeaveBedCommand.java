package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;

@SuppressWarnings("ALL")
public class LeaveBedCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		String[] literals = {"wakeup", "leavebed"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source)
					.getSender()
					.isPlayerSleeping())
				.executes(context -> {
					CommandSource source = (CommandSource) context.getSource();
					Player        player = source.getSender();
					player.wakeUpPlayer(true, true);
					return 1;
				}));
		}
	}
}
