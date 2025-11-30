package wyspr.BTAEssentials.utils;

import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.Container;
import org.jetbrains.annotations.Nullable;

public class ContainerInvsee implements Container {
	private final Player player;

	public ContainerInvsee(Player player) {
		this.player = player;
	}

	@Override
	public int getContainerSize() {
		return 36;
	}

	@Override
	public @Nullable ItemStack getItem(int index) {
		int i = shiftIndex(index);
		return this.player.inventory.mainInventory[i];
	}

	@Override
	public @Nullable ItemStack removeItem(int index, int takeAmount) {
		ItemStack item = this.getItem(index);
		if (item != null) {
			if (item.stackSize <= takeAmount) {
				this.setItem(index, null);
				this.setChanged();
				return item;
			} else {
				ItemStack itemStack1 = item.splitStack(takeAmount);
				if (item.stackSize <= 0) {
					this.setItem(index, null);
				}

				this.setChanged();
				return itemStack1;
			}
		} else {
			return null;
		}
	}

	private int shiftIndex(int index) {
		if (index >= 27 && index < 36) {
			return index - 27;
		} else {
			return index + 9;
		}
	}

	@Override
	public void setItem(int index, @Nullable ItemStack itemStack) {
		int i = shiftIndex(index);
		if (itemStack != null && itemStack.stackSize > this.getMaxStackSize()) {
			itemStack.stackSize = this.getMaxStackSize();
		}
		this.player.inventory.mainInventory[i] = itemStack;

		this.setChanged();
	}

	@Override
	public String getNameTranslationKey() {
		return this.player.username;
	}

	@Override
	public int getMaxStackSize() {
		return 64;
	}

	@Override
	public void setChanged() {}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void sortContainer() {}
}
