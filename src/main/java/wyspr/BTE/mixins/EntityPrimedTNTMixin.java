package wyspr.BTE.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityPrimedTNT;
import net.minecraft.core.world.Explosion;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wyspr.BTE.Essentials;

@Mixin(value = EntityPrimedTNT.class, remap = false)
public class EntityPrimedTNTMixin {
	@SuppressWarnings("SpellCheckingInspection")
	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/world/World;createExplosion(Lnet/minecraft/core/entity/Entity;DDDF)Lnet/minecraft/core/world/Explosion;"))
	public Explosion stopTNTBoom(
		World instance,
		Entity entity,
		double x,
		double y,
		double z,
		float explosionSize,
		Operation<Explosion> original
	) {
		if ((instance.dimension.id == 0 && Essentials.DisableTNTOverworld <= (int) y)
		||  (instance.dimension.id == 1 && Essentials.DisableTNTNether    <= (int) y)
		||  (instance.dimension.id == 2 && Essentials.DisableTNTSky       <= (int) y)) {
			if (entity != null) entity.remove();
			return null;
		}

		return original.call(instance, entity, x, y, z, explosionSize);
	}
}
