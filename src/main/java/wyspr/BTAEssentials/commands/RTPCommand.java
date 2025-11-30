package wyspr.BTAEssentials.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import wyspr.BTAEssentials.BTAEssentials;
import wyspr.BTAEssentials.utils.PlayerData;
import wyspr.BTAEssentials.utils.Teleport;

import java.util.Random;

@SuppressWarnings("ALL") public class RTPCommand implements CommandManager.CommandRegistry {
    Random r = new Random();

    @Override
    public void register(CommandDispatcher<CommandSource> commandDispatcher) {
        commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral.literal("rtp")
            .requires(source -> ((CommandSource) source).hasAdmin() || BTAEssentials.RTPCommand)
            .executes(context -> {
                CommandSource source  = (CommandSource) context.getSource();
                boolean       isAdmin = source.hasAdmin();
                Player        player  = source.getSender();
                PlayerData playerData = PlayerData.get(player);

				int cost = BTAEssentials.RTPCost;
				if (player.score < cost && !isAdmin) {
					player.sendMessage("§4You do not have enough points to use this command!");
					player.sendMessage("§4You need §1" + (cost - player.score) + "§4 more points!");
					return 1;
				}
                if (player.dimension != 0) {
                    player.sendMessage("§4You may only use this in the overworld!");
                    return 1;
                }
                if (!playerData.canTP() && !isAdmin) {
                    int waitTime = playerData.TPCooldown();
                    player.sendMessage("§4Teleport available in §1" + waitTime + "§4 seconds.");
                    return 1;
                }

                int min = BTAEssentials.RTPMin;
                int max = BTAEssentials.RTPMax;
		        int randX = (int) (r.nextDouble() * (max - min) + min);
		        int randZ = (int) (r.nextDouble() * (max - min) + min);

                playerData.updateBackPos();

                if (Teleport.teleport(player, randX, 256, randZ, player.dimension)) {
                    player.sendMessage("§4Teleported! §lShould you get stuck, rejoin§4.");

                    player.score -= BTAEssentials.RTPCost;
                    player.fireImmuneTicks = 200;
                    player.onGround = false;
                    player.maxHurtTime = 200;
                    player.hurtTime = 200;
                    player.airSupply = 1000;
                    player.fallDistance = -1000;
                }

                return 1;
            }));
    }
}
