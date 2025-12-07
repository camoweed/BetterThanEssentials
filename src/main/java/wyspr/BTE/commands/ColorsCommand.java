package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;

@SuppressWarnings("ALL") public class ColorsCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {

		String[] literals = {"colorcodes", "colourcodes"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.executes(context -> {
					CommandSource source = (CommandSource) context.getSource();
					source.sendMessage("§§00 = §0White");
					source.sendMessage("§§01 = §1Orange");
					source.sendMessage("§§02 = §2Magenta");
					source.sendMessage("§§03 = §3Light blue / Aqua");
					source.sendMessage("§§04 = §4Yellow");
					source.sendMessage("§§05 = §5Lime");
					source.sendMessage("§§06 = §6Pink");
					source.sendMessage("§§07 = §7Grey");
					source.sendMessage("§§08 = §8Light Grey / Silver");
					source.sendMessage("§§09 = §9Cyan / Turquoise");
					source.sendMessage("§§0a = §aPurple");
					source.sendMessage("§§0b = §bBlue");
					source.sendMessage("§§0c = §cBrown");
					source.sendMessage("§§0d = §dGreen");
					source.sendMessage("§§0e = §eRed");
					source.sendMessage("§§0f = §fBlack");
					source.sendMessage("§§0k = (Obfuscated, §e§lOperator§r only.) §kObfuscated §r");
					source.sendMessage("§§0l = §lBold");
					source.sendMessage("§§0m = §mStrikethrough");
					source.sendMessage("§§0n = §nUnderline");
					source.sendMessage("§§0o = §oItalic");
					source.sendMessage("§§0r = §rnormal!");
					return 1;
				}));

		}
	}
}
