package wyspr.BTE.mixins;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.commands.CommandSpawn;
import net.minecraft.core.net.command.exceptions.CommandExceptions;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.ChunkCoordinates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wyspr.BTE.Essentials;
import wyspr.BTE.utils.PlayerData;

@SuppressWarnings("ALL")
@Mixin(value = CommandSpawn.class, remap = false) public class SpawnCommandMixin {

	/**
	 * @author ipiepiepie
	 * @reason override default spawn command, which accessible only for admins.
	 * <br>
	 * <a href="https://github.com/MelonModding/MelonUtilities/blob/b196734af0a1f7cf350546f610a83e52d93fe4b8/src/main/java/MelonUtilities/mixins/SpawnCommandMixin.java">source<a>
	 */
	@Inject(
		method = "register", at = @At("HEAD"), cancellable = true
	)
	private void setSpawn(CommandDispatcher<CommandSource> dispatcher, CallbackInfo ci) {
		dispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral.literal("spawn")
			.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.SpawnCommand)
			.executes(context -> {
				CommandSource    source           = (CommandSource) context.getSource();
				Player           sender           = source.getSender();
				World            world            = source.getWorld(0);
				ChunkCoordinates spawnCoordinates = world.getSpawnPoint();
				if (sender == null) {
					throw CommandExceptions
						.notInWorld().create();
				} else {
					if (sender.dimension != 0) {
						source.movePlayerToDimension(sender, 0);
					}

					PlayerData
						.get(sender).updateBackPos();

					source.teleportPlayerToPos(
						sender,
						(double) spawnCoordinates.x + (double) 0.5F,
						(double) ((float) world.findTopSolidBlock(
							spawnCoordinates.x,
							spawnCoordinates.z
						) + sender.heightOffset),
						(double) spawnCoordinates.z + (double) 0.5F
					);
					source.sendTranslatableMessage("command.commands.spawn.success", new Object[0]);
					return 1;
				}
			})
		);

		ci.cancel();
	}

}
