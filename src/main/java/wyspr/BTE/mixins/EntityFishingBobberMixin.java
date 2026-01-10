package wyspr.BTE.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.entity.EntityFishingBobber;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wyspr.BTE.Essentials;

@Mixin(value = EntityFishingBobber.class, remap = false)
public class EntityFishingBobberMixin {
	@WrapOperation(
		method = "tick",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/core/entity/EntityFishingBobber;ticksCatchable:I",
			opcode = Opcodes.PUTFIELD
		)
	)
	public void bobberAddTicks(EntityFishingBobber bobber, int value, Operation<Void> original) {
		if (Essentials.AddedTicksCatchable <= -40) {
			bobber.remove();
		} else {
			original.call(bobber, value + Essentials.AddedTicksCatchable);
		}
	}
}
