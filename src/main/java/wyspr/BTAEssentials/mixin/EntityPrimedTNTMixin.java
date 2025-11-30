package wyspr.BTAEssentials.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityPrimedTNT;
import net.minecraft.core.world.Explosion;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import wyspr.BTAEssentials.BTAEssentials;

@Environment(EnvType.SERVER)
@Mixin(value = EntityPrimedTNT.class, remap = false) public class EntityPrimedTNTMixin {
	@Redirect(
		method = "tick", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/core/world/World;createExplosion(Lnet/minecraft/core/entity/Entity;DDDF)Lnet/minecraft/core/world/Explosion;"
	)
	)
	public Explosion stopTNTBoom(
		World instance,
		Entity entity,
		double x,
		double y,
		double z,
		float explosionSize
	)
	{

		if (instance.dimension.id == 0 && BTAEssentials.DisableTNTOverworld <= (int) y) {
			entity.remove();
			return null;
		}
		if (instance.dimension.id == 1 && BTAEssentials.DisableTNTNether <= (int) y) {
			entity.remove();
			return null;
		}
		if (instance.dimension.id == 2 && BTAEssentials.DisableTNTSky <= (int) y) {
			entity.remove();
			return null;
		}
		instance.createExplosion(null, x, y + 0.5, z, 4.0F);
		return null;
	}
}
