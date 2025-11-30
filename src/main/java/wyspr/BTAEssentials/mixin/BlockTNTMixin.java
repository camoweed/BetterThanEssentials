package wyspr.BTAEssentials.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicTNT;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wyspr.BTAEssentials.BTAEssentials;

@Environment(EnvType.SERVER) @Mixin(value = BlockLogicTNT.class, remap = false) public class BlockTNTMixin extends BlockLogic {
	public BlockTNTMixin(Block<?> block, Material material) {
		super(block, material);
	}

	@Inject(
		method = "ignite(Lnet/minecraft/core/world/World;IIILnet/minecraft/core/entity/player/Player;Z)V", at = @At("HEAD"), cancellable = true
	)
	public void disableAbove(World world, int x, int y, int z, Player player, boolean sound, CallbackInfo ci)
	{
		String msg = "§4TNT is disabled above y: §5";
		if (world.dimension.id == 0 && BTAEssentials.DisableTNTOverworld <= y) {
			player.sendMessage(msg + BTAEssentials.DisableTNTOverworld);
			ci.cancel();
		}
		if (world.dimension.id == 1 && BTAEssentials.DisableTNTNether <= y) {
			player.sendMessage(msg + BTAEssentials.DisableTNTNether);
			ci.cancel();
		}
		if (world.dimension.id == 2 && BTAEssentials.DisableTNTSky <= y) {
			player.sendMessage(msg + BTAEssentials.DisableTNTSky);
			ci.cancel();
		}
	}
}
