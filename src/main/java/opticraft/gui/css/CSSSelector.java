package opticraft.gui.css;

import opticraft.gui.items.HTMLElement;
import opticraft.gui.items.Attribute;

import java.util.*;

public class CSSSelector {
	public List<SelectorPart> parts = new ArrayList<>();

	public static CSSSelector parse(String raw) {
		CSSSelector sel = new CSSSelector();
		String[] tokens = raw.trim().split("(?=\\s|>|\\+|~)");

		SelectorPart current = new SelectorPart();
		for (String token : tokens) {
			token = token.trim();
			if (token.isEmpty()) continue;

			if (token.equals(">")) {
				sel.parts.add(current);
				current = new SelectorPart();
				current.combinator = Combinator.CHILD;
			} else if (token.equals("+")) {
				sel.parts.add(current);
				current = new SelectorPart();
				current.combinator = Combinator.ADJACENT;
			} else if (token.equals("~")) {
				sel.parts.add(current);
				current = new SelectorPart();
				current.combinator = Combinator.SIBLING;
			} else if (token.equals(" ")) {
				sel.parts.add(current);
				current = new SelectorPart();
				current.combinator = Combinator.DESCENDANT;
			} else {
				current.selectors.addAll(parseSimple(token));
			}
		}
		sel.parts.add(current);
		return sel;
	}

	private static List<SimpleSelector> parseSimple(String token) {
		List<SimpleSelector> list = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		char mode = 0;

		for (int i = 0; i < token.length(); i++) {
			char c = token.charAt(i);
			if (c == '#' || c == '.') {
				if (sb.length() > 0) {
					list.add(new SimpleSelector(mode, sb.toString()));
				}
				mode = c;
				sb = new StringBuilder();
			} else if (c == ':') {
				if (token.startsWith(":hover", i)) {
					i += ":hover".length() - 1;
				}
			} else {
				sb.append(c);
			}
		}
		if (sb.length() > 0) {
			list.add(new SimpleSelector(mode, sb.toString()));
		}
		return list;
	}

	public boolean matches(HTMLElement element, List<HTMLElement> ancestors) {
		return matchesRecursive(parts.size() - 1, element, ancestors, 0);
	}

	private boolean matchesRecursive(int index, HTMLElement current, List<HTMLElement> ancestors, int ancestorOffset) {
		if (index < 0) return true;
		if (current == null) return false;

		SelectorPart part = parts.get(index);
		if (!part.matches(current)) return false;

		if (index == 0) return true;

		SelectorPart prev = parts.get(index - 1);

		switch (prev.combinator) {
			case DESCENDANT:
				for (int i = ancestorOffset; i < ancestors.size(); i++) {
					if (matchesRecursive(index - 1, ancestors.get(i), ancestors, i + 1)) {
						return true;
					}
				}
				break;

			case CHILD:
				if (ancestorOffset < ancestors.size() && matchesRecursive(index - 1, ancestors.get(ancestorOffset), ancestors, ancestorOffset + 1)) {
					return true;
				}
				break;

			case ADJACENT:
			case SIBLING:
				// You need parent context here — not implemented unless you track parents
				break;
		}
		return false;
	}

	public static class SelectorPart {
		public List<SimpleSelector> selectors = new ArrayList<>();
		public Combinator combinator = Combinator.DESCENDANT;

		public boolean matches(HTMLElement el) {
			if (selectors.isEmpty()) return false;
			for (SimpleSelector s : selectors) {
				if (!s.matches(el)) return false;
			}
			return true;
		}
	}

	public static class SimpleSelector {
		public char type; // 0 = tag, '.' = class, '#' = id
		public String value;

		public SimpleSelector(char type, String value) {
			this.type = type;
			this.value = value;
		}

		public boolean matches(HTMLElement el) {
			switch (type) {
				case 0:
					return value.equalsIgnoreCase(el.tagName);
				case '#': {
					Attribute idAttr = el.getAttribute("id");
					return idAttr != null && idAttr.getString().equals(value);
				}
				case '.': {
					Attribute classAttr = el.getAttribute("class");
					if (classAttr == null) return false;
					String[] classes = classAttr.getString().split("\\s+");
					for (String c : classes) {
						if (c.equals(value)) return true;
					}
					return false;
				}
			}
			return false;
		}
	}

	public enum Combinator {
		DESCENDANT,
		CHILD,
		ADJACENT,   // +
		SIBLING     // ~
	}
}
