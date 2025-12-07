package wyspr.BTE.utils;

import net.minecraft.core.net.command.TextFormatting;
import wyspr.BTE.Essentials;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigBuilder {
	private static final Map<String, String> colorMap = new HashMap<>(33);

	static {
		colorMap.put("white", TextFormatting.WHITE.toString());
		colorMap.put("orange", TextFormatting.ORANGE.toString());
		colorMap.put("magenta", TextFormatting.MAGENTA.toString());
		colorMap.put("aqua", TextFormatting.LIGHT_BLUE.toString());
		colorMap.put("light_blue", TextFormatting.LIGHT_BLUE.toString());
		colorMap.put("yellow", TextFormatting.YELLOW.toString());
		colorMap.put("lime", TextFormatting.LIME.toString());
		colorMap.put("pink", TextFormatting.PINK.toString());
		colorMap.put("grey", TextFormatting.GRAY.toString());
		colorMap.put("gray", TextFormatting.GRAY.toString());
		colorMap.put("silver", TextFormatting.LIGHT_GRAY.toString());
		colorMap.put("light_gray", TextFormatting.LIGHT_GRAY.toString());
		colorMap.put("light_grey", TextFormatting.LIGHT_GRAY.toString());
		colorMap.put("cyan", TextFormatting.CYAN.toString());
		colorMap.put("purple", TextFormatting.PURPLE.toString());
		colorMap.put("blue", TextFormatting.BLUE.toString());
		colorMap.put("brown", TextFormatting.BROWN.toString());
		colorMap.put("green", TextFormatting.GREEN.toString());
		colorMap.put("red", TextFormatting.RED.toString());
		colorMap.put("black", TextFormatting.BLACK.toString());
		colorMap.put("o", TextFormatting.OBFUSCATED.toString());
		colorMap.put("obf", TextFormatting.OBFUSCATED.toString());
		colorMap.put("obfuscated", TextFormatting.OBFUSCATED.toString());
		colorMap.put("b", TextFormatting.BOLD.toString());
		colorMap.put("bold", TextFormatting.BOLD.toString());
		colorMap.put("s", TextFormatting.STRIKETHROUGH.toString());
		colorMap.put("strike", TextFormatting.STRIKETHROUGH.toString());
		colorMap.put("strikethrough", TextFormatting.STRIKETHROUGH.toString());
		colorMap.put("u", TextFormatting.UNDERLINE.toString());
		colorMap.put("underline", TextFormatting.UNDERLINE.toString());
		colorMap.put("i", TextFormatting.ITALIC.toString());
		colorMap.put("italic", TextFormatting.ITALIC.toString());
		colorMap.put("r", TextFormatting.RESET.toString());
		colorMap.put("reset", TextFormatting.RESET.toString());
	}

	private final Path         cfgPath;
	private final String       fileName;
	private final List<String> defaultContent;
	private final boolean      syntaxEnabled;

	public ConfigBuilder(String fileName, List<String> defaultContent, boolean syntaxEnabled) {
		this.fileName       = fileName;
		this.defaultContent = defaultContent;
		this.syntaxEnabled  = syntaxEnabled;
		this.cfgPath        = Essentials.DATA_DIR.resolve(fileName.toLowerCase());
		if (!Files.exists(this.cfgPath)) {
			try {
				Files.createDirectory(this.cfgPath);
				Essentials.LOGGER.info("Created {}", this.cfgPath);
			} catch (IOException e) {
				Essentials.LOGGER.error("Could not create: {}", this.cfgPath, new RuntimeException(e));
				throw new RuntimeException(e); // Exit if the directory cannot be created
			}
		}
		createBase();
	}

	private void createBase() {
		Path baseFile = cfgPath.resolve(fileName + ".txt");
		if (!Files.exists(baseFile)) {
			try {
				Essentials.LOGGER.info("{} does not exist. Creating it for you...", baseFile);
				Files.write(baseFile, defaultContent, StandardCharsets.UTF_8);
				Essentials.LOGGER.info("Done! Check your config folder for {}", baseFile);
			} catch (IOException e) {
				Essentials.LOGGER.error("Error creating file: {}", e.getMessage());
			}
		}
	}

	public List<String> get(int pageNumber) {
		if (pageNumber <= 1) {
			return readFile(cfgPath.resolve(fileName + ".txt"));
		}
		return readFile(cfgPath.resolve(fileName + pageNumber + ".txt"));
	}

	private List<String> readFile(Path path) {
		try {
			List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
			if (syntaxEnabled) {
				return parseSyntax(lines);
			} else {
				return lines;
			}
		} catch (IOException e) {
			List<String> errorMsg = Collections.singletonList(parseTags("<orange>Page not found.<r>"));
			return parseSyntax(errorMsg);
		}
	}

	private List<String> parseSyntax(List<String> content) {
		List<String> parsedLines = new ArrayList<>();

		for (String line : content) {
			if (line.startsWith("///")) continue; // Skip comments
			line = parseTags(line);
			parsedLines.add(line);
		}

		return parsedLines;
	}

	private String parseTags(String line) {
		// Handle escaping
		line = line.replaceAll("\\\\<", "ESCAPED_LT")
			.replaceAll("\\\\>", "ESCAPED_GT");
		// Process color tags
		for (Map.Entry<String, String> entry : colorMap.entrySet()) {
			line = line.replaceAll("<" + entry.getKey() + ">", entry.getValue());
		}
		// Revert escaped characters
		return line.replaceAll("ESCAPED_LT", "<")
			.replaceAll("ESCAPED_GT", ">");
	}
}
