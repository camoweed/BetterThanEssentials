package wyspr.BTAEssentials.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.commands.CommandSpawn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wyspr.BTAEssentials.BTAEssentials;
import wyspr.BTAEssentials.commands.SpawnCommand;

@Mixin(value = CommandSpawn.class, remap = false)
public class SpawnCommandMixin {

	/**
	 * @author ipiepiepie
	 * @reason override default spawn command, which accessible only for admins.
	 * <br>
	 * <a href="https://github.com/MelonModding/MelonUtilities/blob/b196734af0a1f7cf350546f610a83e52d93fe4b8/src/main/java/MelonUtilities/mixins/SpawnCommandMixin.java">source<a>
	 */
	@Inject(
		method = "register",
		at = @At("HEAD"),
		cancellable = true
	)
	private void setSpawn(CommandDispatcher<CommandSource> dispatcher, CallbackInfo ci) {
		if(!BTAEssentials.SpawnCommand) return;

		new SpawnCommand().register(dispatcher);

		ci.cancel();
	}

}
