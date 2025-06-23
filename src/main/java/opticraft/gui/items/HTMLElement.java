package opticraft.gui.items;

import opticraft.OptiCraft;
import opticraft.gui.css.CSSParser;
import opticraft.gui.css.ElementCSS;
import opticraft.gui.css.MultiDim;
import opticraft.util.Window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class HTMLElement {
	public ArrayList<HTMLElement> children;
	public ArrayList<Attribute> attributes;
	public ArrayList<ElementCSS> styles;
	public String tagName;

	private boolean hover;
	public int x, y, w, h;
	public MultiDim padding, margin;

	public HTMLElement() {
		this.hover = false;
		this.x = 0;
		this.y = 0;
		this.w = 0;
		this.h = 0;

		this.children = new ArrayList<>();
		this.attributes = new ArrayList<>();
		this.styles = new ArrayList<>();
		this.tagName = "UNREGISTERED";
	}

	public boolean isHover() {
		return hover;
	}

	public void setHover(boolean hover) {
		this.hover = hover;
	}

	public String getStyle(String key, int x, int y) {
		Window w = OptiCraft.get().window;
		for (int i = styles.size() - 1; i >= 0; i--) {
			ElementCSS css = styles.get(i);
			boolean flag = !css.isOnHover || isHover();

			if(flag) for (ElementCSS.CSSEntry entry : css.entries) {
				if (entry.key.equalsIgnoreCase(key)) {
					return entry.value;
				}
			}
		}
		return null;
	}

	public String getStyle(String key) {
		return getStyle(key, this.x, this.y);
	}

	public Attribute getAttribute(String key) {
		return this.attributes.stream().filter(a->a.key.equals(key)).findFirst().orElse(null);
	}

	public String getInnerHTML() {
		return this.children.stream().map(HTMLElement::getHTML).collect(Collectors.joining("\n"));
	}

	public String getHTML() {
		return String.format("<%s%s>\n%s\n</%s>",
				tagName,
				" " + this.attributes.stream().map(a -> String.format("%s=%s", a.getKey(), a.getString())).collect(Collectors.joining(" ")),
				getInnerHTML(),
				tagName);
	}
}
