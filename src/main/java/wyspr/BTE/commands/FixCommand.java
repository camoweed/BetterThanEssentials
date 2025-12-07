package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.server.entity.player.PlayerServer;
import wyspr.BTE.Essentials;
import wyspr.BTE.commands.arguments.ArgumentTypeUser;

@SuppressWarnings("ALL") public class FixCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		String[] literals = {"fix", "repair"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.HealCommand)
				.executes(context -> {
					CommandSource source = (CommandSource) context.getSource();
					Player        player = source.getSender();

					ItemStack held = player.getHeldItem();
					if (held.isItemStackDamageable()) {
						held.setMetadata(held.getMaxDamage());
					}
					player.inventory.setHeldItemStack(held);

					player.sendMessage(TextFormatting.YELLOW + "Repaired held item.");
					return 1;
				})
				.then(ArgumentBuilderRequired
					.argument("player", ArgumentTypeUser.user())
					.requires(source -> ((CommandSource) source).hasAdmin())
					.executes(context -> {
						CommandSource source = (CommandSource) context.getSource();
						Player       player   = source.getSender();
						PlayerServer target = context.getArgument("player", PlayerServer.class);
						ItemStack held = target.getHeldItem();
						if (held.isItemStackDamageable()) {
							held.setMetadata(held.getMaxDamage());
						}
						target.inventory.setHeldItemStack(held);

						player.sendMessage(TextFormatting.YELLOW + "Repaired " + TextFormatting.RESET + target.getDisplayName() + TextFormatting.YELLOW + "'s held item.");
						target.sendMessage(TextFormatting.YELLOW + "Held item repaired.");
						return 1;
					}))
			);

		}
	}
}
