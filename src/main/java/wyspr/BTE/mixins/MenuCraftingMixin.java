package wyspr.BTE.mixins;

import net.minecraft.core.entity.player.Player;
import net.minecraft.core.player.inventory.menu.MenuCrafting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wyspr.BTE.utils.PlayerData;

@Mixin(value = MenuCrafting.class, remap = false)
public abstract class MenuCraftingMixin {
	@Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
	public void stillValid(Player player, CallbackInfoReturnable<Boolean> cir) {
		if (PlayerData.get(player).craftCommandOpen) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "onCraftGuiClosed", at = @At("HEAD"))
	public void commandClose(Player player, CallbackInfo ci) {
		PlayerData.get(player).craftCommandOpen = false;
	}
}
