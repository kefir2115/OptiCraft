package opticraft.gui.items;

import opticraft.OptiCraft;
import opticraft.gui.css.CSSParser;
import opticraft.gui.css.MultiDim;
import opticraft.render.overlay.FontRenderer;

public class HTMLText extends HTMLElement {

	public String content;

	public HTMLText() {
		this.children = null;
		this.tagName = "text";
		this.content = "";
	}

	public void setText(String content) {
		this.content = content.replace("<br>", "\n");
	}

	public String getText() {
		return content;
	}

	@Override
	public String getInnerHTML() {
		return getText();
	}

	@Override
	public String getHTML() {
		return this.getInnerHTML();
	}
}