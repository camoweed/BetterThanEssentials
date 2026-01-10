package wyspr.BTE.mixins;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicFarmland;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wyspr.BTE.Essentials;

@Mixin(value = BlockLogicFarmland.class, remap = false)
public class BlockFarmlandMixin {
	@Inject(
		method = "onEntityWalking", at = @At("HEAD"), cancellable = true
	)
	public void trampleControl(World world, int x, int y, int z, Entity entity, CallbackInfo ci) {
		if (Essentials.DisableTrample) {
			ci.cancel();
		}

		if (Essentials.EnableAntiTrampleFence) {
			Block<?> blockBelow = world.getBlock(x, y - 1, z);
			if (blockBelow == Blocks.FENCE_PLANKS_OAK || blockBelow == Blocks.FENCE_PLANKS_OAK_PAINTED) {
				ci.cancel();
			}
		}
	}
}
