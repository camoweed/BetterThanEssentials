package wyspr.BTE.mixins;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeInteger;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.arguments.ArgumentTypeEntity;
import net.minecraft.core.net.command.arguments.ArgumentTypeItemStack;
import net.minecraft.core.net.command.commands.CommandGive;
import net.minecraft.core.net.command.helpers.EntitySelector;
import net.minecraft.core.net.command.util.CommandHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import wyspr.BTE.Essentials;

import java.util.List;


@SuppressWarnings("ALL")
@Mixin(value = CommandGive.class, remap = false)
public class CommandGiveMixin implements CommandManager.CommandRegistry {
	@Overwrite
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		String[] literals = {"i", "give"};
		for (String literal : literals) {
			dispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.GiveCommand)
				.then(ArgumentBuilderRequired
					.argument("item", ArgumentTypeItemStack.itemStack())
					.executes(this::fullStackToSelf))
				.then(ArgumentBuilderRequired
					.argument("target", ArgumentTypeEntity.usernames())
					.then(ArgumentBuilderRequired
						.argument("item", ArgumentTypeItemStack.itemStack())
						.executes(this::fullStack)
						.then(ArgumentBuilderRequired
							.argument("amount", ArgumentTypeInteger.integer(1, 6400))
							.executes(this::amountArg)))));
		}
	}

	private int fullStackToSelf(CommandContext<Object> c) throws CommandSyntaxException {
		CommandSource source    = (CommandSource) c.getSource();
		Player        player    = source.getSender();
		ItemStack     itemStack = (ItemStack) c.getArgument("item", ItemStack.class);
		int           amount    = itemStack.getMaxStackSize();

		player.inventory.insertItem(itemStack, true);
		if (itemStack.stackSize > 0) {
			player.dropPlayerItem(itemStack);
		}

		source.sendTranslatableMessage(
			"command.commands.give.success_single_entity", new Object[]{
				CommandHelper.getEntityName(player), amount, itemStack.getDisplayName()
			}
		);

		return 1;
	}

	private int fullStack(CommandContext<Object> c) throws CommandSyntaxException {
		CommandSource          source         = (CommandSource) c.getSource();
		ItemStack              itemStack      = (ItemStack) c.getArgument("item", ItemStack.class);
		int                    amount         = itemStack.stackSize;
		EntitySelector         entitySelector = (EntitySelector) c.getArgument(
			"target",
			EntitySelector.class
		);
		List<? extends Entity> entities       = entitySelector.get(source);

		for (Entity player : entities) {
			((Player) player).inventory.insertItem(itemStack, true);
			if (itemStack.stackSize > 0) {
				((Player) player).dropPlayerItem(itemStack);
			}
		}

		if (entities.size() == 1) {
			source.sendTranslatableMessage(
				"command.commands.give.success_single_entity", new Object[]{
					CommandHelper.getEntityName((Entity) entities.get(0)), amount, itemStack.getDisplayName()
				}
			);
		} else {
			source.sendTranslatableMessage(
				"command.commands.give.success_single_entity",
				new Object[]{entities.size(), amount, itemStack.getDisplayName()}
			);
		}

		return 1;
	}

	private int amountArg(CommandContext<Object> c) throws CommandSyntaxException {
		CommandSource          source         = (CommandSource) c.getSource();
		ItemStack              itemStack      = (ItemStack) c.getArgument("item", ItemStack.class);
		EntitySelector         entitySelector = (EntitySelector) c.getArgument(
			"target",
			EntitySelector.class
		);
		List<? extends Entity> entities       = entitySelector.get(source);
		int                    amount         = (Integer) c.getArgument("amount", Integer.class);

		for (Entity player : entities) {
			int incompleteStack = amount % 64;

			for (int i = 0; i < (amount - incompleteStack) / 64; ++i) {
				ItemStack itemStack1 = ItemStack.copyItemStack(itemStack);
				itemStack1.stackSize = 64;
				((Player) player).inventory.insertItem(itemStack1, true);
				if (itemStack1.stackSize > 0) {
					((Player) player).dropPlayerItem(itemStack1);
				}
			}

			if (incompleteStack > 0) {
				ItemStack itemStack1 = ItemStack.copyItemStack(itemStack);
				itemStack1.stackSize = incompleteStack;
				((Player) player).inventory.insertItem(itemStack1, true);
				if (itemStack1.stackSize > 0) {
					((Player) player).dropPlayerItem(itemStack1);
				}
			}
		}

		if (entities.size() == 1) {
			source.sendTranslatableMessage(
				"command.commands.give.success_single_entity", new Object[]{
					CommandHelper.getEntityName((Entity) entities.get(0)), amount, itemStack.getDisplayName()
				}
			);
		} else {
			source.sendTranslatableMessage(
				"command.commands.give.success_single_entity",
				new Object[]{entities.size(), amount, itemStack.getDisplayName()}
			);
		}

		return 1;
	}
}
