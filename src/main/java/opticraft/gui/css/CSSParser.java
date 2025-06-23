package opticraft.gui.css;

import opticraft.OptiCraft;
import opticraft.util.Window;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSSParser {

	private static final Map<String, Color> COLOR_MAP = new HashMap<>();

	static {
		// Initialize with common CSS color names
		COLOR_MAP.put("black", Color.BLACK);
		COLOR_MAP.put("white", Color.WHITE);
		COLOR_MAP.put("red", Color.RED);
		COLOR_MAP.put("green", Color.GREEN);
		COLOR_MAP.put("blue", Color.BLUE);
		COLOR_MAP.put("yellow", Color.YELLOW);
		COLOR_MAP.put("cyan", Color.CYAN);
		COLOR_MAP.put("magenta", Color.MAGENTA);
		COLOR_MAP.put("gray", Color.GRAY);
		COLOR_MAP.put("darkgray", Color.DARK_GRAY);
		COLOR_MAP.put("lightgray", Color.LIGHT_GRAY);
		COLOR_MAP.put("orange", Color.ORANGE);
		COLOR_MAP.put("pink", Color.PINK);
	}

	public static int parsev(String value, int fallback, boolean horizontal) {
		Window window = OptiCraft.get().window;
		int WIN_W = window.w, WIN_H = window.h;
		if (value == null) return fallback;
		try {
			value = value.trim().toLowerCase();
			if (value.endsWith("px")) {
				return Integer.parseInt(value.substring(0, value.length() - 2));
			} else if (value.endsWith("%")) {
				int percent = Integer.parseInt(value.substring(0, value.length() - 1));
				int base = horizontal ? WIN_W : WIN_H;
				return (int) ((percent / 100.0f) * base);
			} else {
				return Integer.parseInt(value);
			}
		} catch (Exception e) {
			return fallback;
		}
	}

	public static ArrayList<ElementCSS> parse(String css) {
		ArrayList<ElementCSS> list = new ArrayList<>();

		Pattern rule = Pattern.compile("([^\\{]+)\\{([^}]+)\\}");
		Matcher matcher = rule.matcher(css);

		while (matcher.find()) {
			String selectorsRaw = matcher.group(1).trim(); // e.g. "div, .link, #id"
			String body = matcher.group(2).trim();

			// Parse properties once
			List<ElementCSS.CSSEntry> entries = new ArrayList<>();
			Matcher entryMatcher = Pattern.compile("([\\w-]+)\\s*:\\s*([^;]+);?").matcher(body);
			while (entryMatcher.find()) {
				String key = entryMatcher.group(1).trim();
				String value = entryMatcher.group(2).trim();
				entries.add(new ElementCSS.CSSEntry(key, value));
			}

			// Split multi-selectors by comma, trim, parse individually
			for (String rawSelector : selectorsRaw.split(",")) {
				rawSelector = rawSelector.trim();
				if (rawSelector.isEmpty()) continue;

				ElementCSS element = new ElementCSS(rawSelector);
				element.entries.addAll(entries); // share parsed properties
				list.add(element);
			}
		}

		return list;
	}



	public static Color color(String cssColor) {
		if (cssColor == null || cssColor.trim().isEmpty()) {
			return null;
		}

		String color = cssColor.trim().toLowerCase();

		// Check if it's a named color
		if (COLOR_MAP.containsKey(color)) {
			return COLOR_MAP.get(color);
		}

		// Check for hex format: #rgb or #rrggbb
		if (color.startsWith("#")) {
			return parseHexColor(color);
		}

		// Check for rgb(r, g, b) format
		if (color.startsWith("rgb(")) {
			return parseRgbColor(color);
		}

		// Check for rgba(r, g, b, a) format
		if (color.startsWith("rgba(")) {
			return parseRgbaColor(color);
		}

		// If no known format, return null or throw exception
		return null;
	}

	private static Color parseHexColor(String hex) {
		try {
			if (hex.length() == 4) { // #rgb format
				int r = Integer.parseInt(hex.substring(1, 2), 16) * 17;
				int g = Integer.parseInt(hex.substring(2, 3), 16) * 17;
				int b = Integer.parseInt(hex.substring(3, 4), 16) * 17;
				return new Color(r, g, b);
			} else if (hex.length() == 7) { // #rrggbb format
				int r = Integer.parseInt(hex.substring(1, 3), 16);
				int g = Integer.parseInt(hex.substring(3, 5), 16);
				int b = Integer.parseInt(hex.substring(5, 7), 16);
				return new Color(r, g, b);
			} else if (hex.length() == 9) { // #rrggbbaa format
				int r = Integer.parseInt(hex.substring(1, 3), 16);
				int g = Integer.parseInt(hex.substring(3, 5), 16);
				int b = Integer.parseInt(hex.substring(5, 7), 16);
				int a = Integer.parseInt(hex.substring(7, 9), 16);
				return new Color(r, g, b, a);
			}
		} catch (NumberFormatException e) {
			// Fall through to return null
		}
		return null;
	}

	private static Color parseRgbColor(String rgb) {
		Pattern pattern = Pattern.compile("rgb\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)");
		Matcher matcher = pattern.matcher(rgb);
		if (matcher.matches()) {
			try {
				int r = Integer.parseInt(matcher.group(1));
				int g = Integer.parseInt(matcher.group(2));
				int b = Integer.parseInt(matcher.group(3));
				return new Color(
						clamp(r, 0, 255),
						clamp(g, 0, 255),
						clamp(b, 0, 255)
				);
			} catch (NumberFormatException e) {
				// Fall through to return null
			}
		}
		return null;
	}

	private static Color parseRgbaColor(String rgba) {
		Pattern pattern = Pattern.compile("rgba\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*([01]?\\d?\\d|0?\\.\\d+)\\s*\\)");
		Matcher matcher = pattern.matcher(rgba);
		if (matcher.matches()) {
			try {
				int r = Integer.parseInt(matcher.group(1));
				int g = Integer.parseInt(matcher.group(2));
				int b = Integer.parseInt(matcher.group(3));
				float a = Float.parseFloat(matcher.group(4));
				return new Color(
						clamp(r, 0, 255),
						clamp(g, 0, 255),
						clamp(b, 0, 255),
						clamp(a, 0f, 1f)
				);
			} catch (NumberFormatException e) {
				// Fall through to return null
			}
		}
		return null;
	}

	private static int clamp(int value, int min, int max) {
		return Math.min(Math.max(value, min), max);
	}

	private static float clamp(float value, float min, float max) {
		return Math.min(Math.max(value, min), max);
	}
}
