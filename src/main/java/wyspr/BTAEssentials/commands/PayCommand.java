package wyspr.BTAEssentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeInteger;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.arguments.ArgumentTypeEntity;
import net.minecraft.core.net.command.helpers.EntitySelector;
import net.minecraft.server.entity.player.PlayerServer;

import java.util.List;

@SuppressWarnings("ALL") public class PayCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("pay")
			.then(ArgumentBuilderRequired
				.argument("target", ArgumentTypeEntity.username())
				.then(ArgumentBuilderRequired
					.argument("amount", ArgumentTypeInteger.integer(1))
					.executes(context -> {
						CommandSource source = (CommandSource) context.getSource();
						Player sender = source.getSender();
						boolean senderIsAdmin = source.hasAdmin();
						EntitySelector entitySelector = (EntitySelector) context.getArgument(
							"target",
							EntitySelector.class
						);
						List<? extends Entity> entities = entitySelector.get(source);
						PlayerServer reciever = (PlayerServer) entities.get(0);
						int amount = context.getArgument("amount", Integer.class);

						if (sender.score < amount && !senderIsAdmin) {
							sender.sendMessage("§e§lInsufficient funds!");
							return 1;
						}

						if (!senderIsAdmin) {
							sender.score -= amount;
						}
						reciever.score += amount;
						sender.sendMessage("§1Paid §4" + reciever.username + " §3" + amount + "§1 points.");
						reciever.sendMessage("§4" + sender.username + "§1 has paid you §3" + amount + "§1 points.");

						return 1;
					}))));
	}
}
