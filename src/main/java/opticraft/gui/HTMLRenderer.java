package opticraft.gui;

import opticraft.OptiCraft;
import opticraft.gui.css.CSSParser;
import opticraft.gui.items.HTMLElement;
import opticraft.gui.items.HTMLInput;
import opticraft.gui.items.HTMLText;
import opticraft.render.overlay.FontRenderer;
import opticraft.render.overlay.Rect;
import opticraft.util.Window;

import java.awt.*;

public class HTMLRenderer {

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
}
