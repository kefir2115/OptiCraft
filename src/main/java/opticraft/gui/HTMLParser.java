package opticraft.gui;

import opticraft.OptiCraft;
import opticraft.gui.css.CSSParser;
import opticraft.gui.css.ElementCSS;
import opticraft.gui.items.*;
import opticraft.render.overlay.FontRenderer;
import opticraft.render.overlay.Rect;
import opticraft.util.Window;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HTMLParser {

	public static void render(HTMLElement e) {
		render(e, e);
	}
	private static void render(HTMLElement e, HTMLElement parent) {
		Window win = OptiCraft.get().window;
		FontRenderer fr = OptiCraft.get().font;

		String bg;
		if((bg = e.getStyle("background")) != null) {
			Color color = CSSParser.color(bg);
			if(color != null) Rect.renderRect(win, e.x, e.y, e.w, e.h, color);
		}
		if(e instanceof HTMLText) {
			Color c = CSSParser.color(parent.getStyle("color"));
			String content = ((HTMLText) e).content;
			fr.renderText(content, e.x + e.padding.left, e.y + e.padding.top, 1, c);

			return;
		} else if (e instanceof HTMLInput) {
			Rect.renderRect(OptiCraft.get().window, e.x, e.y, e.w, e.h, new Color(255, 255, 255, 100));
		}

		for (HTMLElement child : e.children) {
			render(child, e);
		}
	}


	public static HTMLElement parse(String html) {
		AtomicReference<HTMLDiv> body = new AtomicReference<>();
		ArrayList<HTMLDiv> depth = new ArrayList<>();

		// Patterns for matching tags and attributes
		Pattern tagPattern = Pattern.compile("<(/?)(\\w+)([^>]*)>");
		Pattern attrPattern = Pattern.compile("(\\w+)=\"([^\"]*)\"");

		// Current position in the HTML string
		int pos = 0;

		Matcher matcher = tagPattern.matcher(html);
		while (matcher.find()) {
			// Handle text content before the current tag
			if (matcher.start() > pos) {
				String textContent = html.substring(pos, matcher.start()).trim();
				if (!textContent.isEmpty()) {
					HTMLText textNode = new HTMLText();
					textNode.content = textContent;

					if (!depth.isEmpty()) {
						depth.get(depth.size() - 1).children.add(textNode);
					} else if (body.get() != null) {
						body.get().children.add(textNode);
					}
				}
			}

			boolean isClosing = !matcher.group(1).isEmpty();
			String tagName = matcher.group(2);
			String attrString = matcher.group(3);

			if (isClosing) {
				// Handle closing tag
				if (!depth.isEmpty() && depth.get(depth.size() - 1).tagName.equals(tagName)) {
					depth.remove(depth.size() - 1);
				}
			} else {
				// Handle opening tag
				HTMLDiv element = tagName.equals("input") ? new HTMLInput() : new HTMLDiv();
				element.tagName = tagName;

				// Parse attributes
				Matcher attrMatcher = attrPattern.matcher(attrString);
				ArrayList<Attribute> attributes = new ArrayList<>();
				while (attrMatcher.find()) {
					attributes.add(new Attribute(attrMatcher.group(1), attrMatcher.group(2)));
				}
				element.attributes = attributes;

				// Add to appropriate parent
				if (tagName.equals("body")) {
					body.set(element);
				} else if (!depth.isEmpty()) {
					depth.get(depth.size() - 1).children.add(element);
				} else if (body.get() != null) {
					body.get().children.add(element);
				}

				// Push to depth stack if not self-closing
				if (!attrString.contains("/") && !VOID_ELEMENTS.contains(tagName)) {
					depth.add(element);
				}
			}

			pos = matcher.end();
		}

		// Handle any remaining text after last tag
		if (pos < html.length()) {
			String textContent = html.substring(pos).trim();
			if (!textContent.isEmpty()) {
				HTMLText textNode = new HTMLText();
				textNode.content = textContent;

				if (!depth.isEmpty()) {
					depth.get(depth.size() - 1).children.add(textNode);
				} else if (body.get() != null) {
					body.get().children.add(textNode);
				}
			}
		}

		return body.get();
	}

	// List of void elements that don't need closing tags
	private static final Set<String> VOID_ELEMENTS = Set.of(
			"area", "base", "br", "col", "embed", "hr",
			"img", "input", "link", "meta", "param",
			"source", "track", "wbr"
	);
}
