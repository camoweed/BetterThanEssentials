package wyspr.BTE.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicTNT;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wyspr.BTE.Essentials;

@Environment(EnvType.SERVER) @Mixin(value = BlockLogicTNT.class, remap = false) public class BlockTNTMixin extends BlockLogic {
	public BlockTNTMixin(Block<?> block, Material material) {
		super(block, material);
	}

	@Inject(
		method = "ignite(Lnet/minecraft/core/world/World;IIILnet/minecraft/core/entity/player/Player;Z)V", at = @At("HEAD"), cancellable = true
	)
	public void disableAbove(World world, int x, int y, int z, Player player, boolean sound, CallbackInfo ci)
	{
		String msg = TextFormatting.YELLOW + "TNT is disabled above y: " + TextFormatting.LIME;
		if (world.dimension.id == 0 && Essentials.DisableTNTOverworld <= y) {
			player.sendMessage(msg + Essentials.DisableTNTOverworld);
			ci.cancel();
		}
		if (world.dimension.id == 1 && Essentials.DisableTNTNether <= y) {
			player.sendMessage(msg + Essentials.DisableTNTNether);
			ci.cancel();
		}
		if (world.dimension.id == 2 && Essentials.DisableTNTSky <= y) {
			player.sendMessage(msg + Essentials.DisableTNTSky);
			ci.cancel();
		}
	}
}
