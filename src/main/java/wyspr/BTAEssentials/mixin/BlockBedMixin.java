package wyspr.BTAEssentials.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.block.BlockLogicBed;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wyspr.BTAEssentials.BTAEssentials;

@Environment(EnvType.SERVER) @Mixin(value = BlockLogicBed.class, remap = false) public class BlockBedMixin {
	@Inject(
		method = "onBlockRightClicked", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/core/world/World;setBlockWithNotify(IIII)Z", shift = At.Shift.BEFORE, by = 1
	), cancellable = true
	)
	public void bedBoomStop(
		World world,
		int x,
		int y,
		int z,
		Player player,
		Side side,
		double xPlaced,
		double yPlaced,
		CallbackInfoReturnable<Boolean> cir
	)
	{
		if (BTAEssentials.DisableBedExplosion) {
			player.sendMessage("§1You may not sleep here.");
			cir.setReturnValue(true);
		}
	}
}
