package opticraft.gui;

import opticraft.OptiCraft;
import opticraft.gui.css.CSSParser;
import opticraft.gui.css.ElementCSS;
import opticraft.gui.items.HTMLElement;
import opticraft.util.Window;

import java.util.ArrayList;

public class HTMLGui {

	String html, css;
	public HTMLElement body;
	public boolean update;

	public HTMLGui(String html, String css) {
		this.html = html;
		this.css = css;
		this.update = true;
	}

	public void render(Window window) {
		if(this.body != null) {
			OptiCraft.get().htmlCalculator.calculateDimensions(this.body);
			GuiHelper.update(window, this.body);
			GuiHelper.anyUpdatedHover(window, this.body);
		}

		if(this.update) {
			this.body = HTMLParser.parse(this.html);
			ArrayList<ElementCSS> elist = CSSParser.parse(this.css);
			GuiHelper.applyCSS(body, elist, new ArrayList<>());

			OptiCraft.get().htmlCalculator.calculateDimensions(this.body);

			this.update = false;
		}

		HTMLRenderer.render(this.body);
	}

	public void onClose() {}
	public void onOpen() {}
}
