package wyspr.BTE.utils.UI;

import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntityTrommel;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.net.packet.PacketContainerClose;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.server.entity.player.PlayerServer;
import org.jetbrains.annotations.Nullable;
import wyspr.BTE.Essentials;
import wyspr.BTE.utils.PlayerData;
import wyspr.BTE.utils.PlayerData.MailManager.Mail;
import wyspr.BTE.utils.UI.Mailbox.MailboxType;

import java.util.List;

import static wyspr.BTE.utils.UI.Mailbox.getDraftItem;

public class MailSelector extends TileEntityTrommel {
	public static final  ItemStack READ_ITEM;
	public static final  ItemStack CONFIRM_READ_ITEM;
	public static final  ItemStack BACK_ITEM;
	public static final  ItemStack DELETE_ITEM;
	public static final  ItemStack CONFIRM_DELETE_ITEM;
	public static final  ItemStack CANCEL_ITEM;
	public static final  ItemStack DESELECT_MAIL_ITEM;
	public static final  ItemStack SELECT_MAIL_ITEM;
	private static final ItemStack REPLY_ITEM;
	private static final ItemStack INBOX_CANCEL_ITEM;

	static {
		READ_ITEM = new ItemStack(
			Blocks.WOOL,
			0,
			DyeColor.LIGHT_BLUE.blockMeta
		);
		READ_ITEM.setCustomName(TextFormatting.RESET + "" + TextFormatting.LIGHT_BLUE + "Mark mail as read");

		CONFIRM_READ_ITEM = new ItemStack(
			Blocks.LAMP_ACTIVE,
			0,
			DyeColor.LIGHT_BLUE.blockMeta
		);
		CONFIRM_READ_ITEM.setCustomName(TextFormatting.RESET + "" + TextFormatting.LIGHT_BLUE + "Confirm mark mail as read");

		DELETE_ITEM = new ItemStack(
			Blocks.PLANKS_OAK_PAINTED,
			0,
			DyeColor.RED.blockMeta
		);
		DELETE_ITEM.setCustomName(TextFormatting.RESET + "" + TextFormatting.RED + "Delete mail");

		CONFIRM_DELETE_ITEM = new ItemStack(
			Blocks.LAMP_ACTIVE,
			0,
			DyeColor.RED.blockMeta
		);
		CONFIRM_DELETE_ITEM.setCustomName(TextFormatting.RESET + "" + TextFormatting.RED + "Confirm delete");

		BACK_ITEM = new ItemStack(Blocks.DEADBUSH, 0);
		BACK_ITEM.setCustomName(TextFormatting.RESET + "Go back");

		REPLY_ITEM = new ItemStack(Items.PAINTBRUSH, 0);
		REPLY_ITEM.setCustomName(TextFormatting.RESET + "Draft a reply");

		INBOX_CANCEL_ITEM = new ItemStack(Blocks.FLUID_WATER_FLOWING.asItem(), 0);
		INBOX_CANCEL_ITEM.setCustomName(TextFormatting.RESET + "Cancel");

		CANCEL_ITEM = new ItemStack(Blocks.FIRE, 0);
		CANCEL_ITEM.setCustomName(TextFormatting.RESET + "Cancel");

		SELECT_MAIL_ITEM = new ItemStack(Blocks.LIMESTONE, 0);
		SELECT_MAIL_ITEM.setCustomName(TextFormatting.RESET + "" + TextFormatting.YELLOW + "Select mail");

		DESELECT_MAIL_ITEM = new ItemStack(Blocks.MARBLE, 0);
		DESELECT_MAIL_ITEM.setCustomName(TextFormatting.RESET + "Deselect mail");
	}

	private final Player      player;
	private final int         mailIndex;
	private final ItemStack   mailItem;
	private final PlayerData  playerData;
	private final boolean     confirmAction;
	private final MailboxType type;

	private MailSelector(
		Player player,
		int mailIndex,
		ItemStack mailItem,
		boolean confirmAction,
		MailboxType type
	)
	{
		this.player        = player;
		this.mailIndex     = mailIndex;
		this.mailItem      = mailItem;
		this.playerData    = PlayerData.get(player);
		this.confirmAction = confirmAction;
		this.type          = type;
	}

	@Override
	public @Nullable ItemStack getItem(int index) {
		if (this.confirmAction && (
			index == 0
		 || index == 1
		 || index == 3)
		) {
			if (this.type == MailboxType.Inbox) {
				return INBOX_CANCEL_ITEM;
			}
			return CANCEL_ITEM;
		}

		switch (index) {
			case 0: // North slot: Empty
				break;
			case 1: // West slot: Select draft / reply
				if (type == MailboxType.Drafts) {
					if (mailIndex == playerData.mail.selectedDraft) {
						return DESELECT_MAIL_ITEM;
					} else {
						return SELECT_MAIL_ITEM;
					}
				} else {
					return REPLY_ITEM;
				}
			case 2: // East slot: Read / Delete item
				if (type == MailboxType.Inbox) {
					if (confirmAction) {
						return CONFIRM_READ_ITEM;
					} else {
						return READ_ITEM;
					}
				} else {
					if (confirmAction) {
						return CONFIRM_DELETE_ITEM;
					} else {
						return DELETE_ITEM;
					}
				}
			case 3: // South slot: Back
				return BACK_ITEM;
			case 4: // Fuel slot: Mail item (reload gui)
				if (type == MailboxType.Drafts) return getDraftItem(playerData, mailIndex);
				return mailItem;
		}
		return null;
	}

	@Override
	public @Nullable ItemStack removeItem(int index, int takeAmount) {
		if (this.confirmAction && (
			index == 0
		 || index == 1
		 || index == 3)) {
			// Go back if cancel clicked
			player.displayTrommelScreen(new MailSelector(player, mailIndex, mailItem, false, type));
			return null;
		}

		switch (index) {
			case 0: // North slot: Empty
				break;

			case 1: // West slot: Select draft / reply
				switch (this.type) {
					case Inbox:
						createReply(playerData.mail.inbox);
						break;
					case Read:
						createReply(playerData.mail.readMail);
						break;
					case Drafts:
						if (mailIndex == playerData.mail.selectedDraft) {
							playerData.mail.selectedDraft = -1;
							player.sendMessage(TextFormatting.YELLOW + "Message un-selected.");
						} else {
							playerData.mail.selectedDraft = mailIndex;
							player.sendMessage(
								TextFormatting.YELLOW + "Mail selected. Attach a message with " +
									TextFormatting.LIGHT_BLUE + "/draft <message>"
							);
						}
						player.displayTrommelScreen(MailSelector.drafts(player, mailIndex, false, getDraftItem(playerData, mailIndex)));
						break;
				}
				break;

			case 2: // East slot: Read / Delete item
				switch (this.type) {
					case Inbox:
						if (confirmAction) {
							if (playerData.mail.readMail.size() >= Essentials.MAX_MAILS) {
								player.sendMessage(TextFormatting.ORANGE + "You have a full read mailbox and cannot add more.");
								player.sendMessage(TextFormatting.ORANGE + "You must delete read mails to mark new ones as read.");
								player.displayContainerScreen(Mailbox.read(player));
								break;
							}

							Mail markedMail = playerData.mail.markMailRead(mailIndex);
							player.displayContainerScreen(Mailbox.inbox(player));
							player.sendMessage(
								TextFormatting.YELLOW + "Marked \"" +
									TextFormatting.LIGHT_BLUE + markedMail.subject +
									TextFormatting.YELLOW + "\" as read"
							);
						} else {
							player.displayTrommelScreen(MailSelector.inbox(
								player,
								mailIndex,
								true,
								mailItem
							));
						}
						break;

					case Read:
						if (confirmAction) {
							// Delete read mail
							Mail deletedMail = playerData.mail.deleteReadMail(mailIndex);
							player.displayContainerScreen(Mailbox.read(player));
							player.sendMessage(
								TextFormatting.RED + "Deleted mail: " +
									TextFormatting.YELLOW + deletedMail.subject
							);
						} else {
							player.displayTrommelScreen(MailSelector.read(player, mailIndex, true, mailItem));
						}
						break;

					case Drafts:
						if (confirmAction) {
							// Delete draft
							Mail deletedMail = playerData.mail.deleteDraft(mailIndex);
							player.displayContainerScreen(Mailbox.drafts(player));
							player.sendMessage(
								TextFormatting.RED + "Deleted draft: " +
									TextFormatting.LIGHT_BLUE + deletedMail.subject
							);
						} else {
							player.displayTrommelScreen(MailSelector.drafts(
								player,
								mailIndex,
								true,
								mailItem
							));
						}
						break;
				}
				break;


			case 3: // South slot: Back
				switch (type) {
					case Inbox:
						player.displayContainerScreen(Mailbox.inbox(player));
						break;
					case Read:
						player.displayContainerScreen(Mailbox.read(player));
						break;
					case Drafts:
						player.displayContainerScreen(Mailbox.drafts(player));
						break;
				}
				break;


			case 4: // Fuel slot: Mail item (reload gui)
				player.displayTrommelScreen(new MailSelector(
					player,
					mailIndex,
					mailItem,
					confirmAction,
					type
				));
				break;
		}
		return null;
	}

	private void createReply(List<Mail> mailList) {
		Mail selected = mailList.get(mailIndex);
		if (playerData.mail.drafts.size() >= Essentials.MAX_MAILS) {
			player.sendMessage(
				TextFormatting.ORANGE + "You have a full draft mailbox and cannot create drafts."
			);
			return;
		}
		boolean draftAdded = playerData.mail.newDraftOutline("RE: " + selected.subject, player);
		if (draftAdded) {
			player.sendMessage(
				TextFormatting.YELLOW + "Added new draft with subject: " +
				TextFormatting.LIGHT_BLUE + "RE: " + selected.subject
			);
			player.displayContainerScreen(Mailbox.drafts(player));
		} else {
			player.sendMessage(TextFormatting.ORANGE + "A draft with that subject already exists!");
			player.sendMessage(TextFormatting.LIGHT_GRAY + "(View drafts with /draft)");
		}
	}

	public static MailSelector drafts(
		Player player,
		int mailIndex,
		boolean confirmAction,
		ItemStack mailItem
	)
	{
		MailSelector ms = new MailSelector(
			player,
			mailIndex,
			mailItem,
			confirmAction,
			MailboxType.Drafts
		);

		if (confirmAction) {
			ms.burnTime = 50;
		}

		return ms;
	}

	public static MailSelector inbox(
		Player player,
		int mailIndex,
		boolean confirmAction,
		ItemStack mailItem
	)
	{
		return new MailSelector(
			player,
			mailIndex,
			mailItem,
			confirmAction,
			MailboxType.Inbox
		);
	}

	public static MailSelector read(
		Player player,
		int mailIndex,
		boolean confirmAction,
		ItemStack mailItem
	)
	{
		MailSelector ms = new MailSelector(
			player,
			mailIndex,
			mailItem,
			confirmAction,
			MailboxType.Read
		);

		if (confirmAction) {
			ms.burnTime = 50;
		}

		return ms;
	}

	// When the player tries to put an item into the inventory
	@Override
	public void setItem(int index, @Nullable ItemStack itemStack) {
		if (itemStack != null) {
			// Close the GUI
			((PlayerServer) player)
				.playerNetServerHandler
				.sendPacket(new PacketContainerClose());
			// Return the removed item to the player
			player.inventory.insertItem(itemStack, true);
		}
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}
}
