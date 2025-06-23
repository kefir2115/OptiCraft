package opticraft.gui;

import opticraft.gui.css.CSSSelector;
import opticraft.gui.css.ElementCSS;
import opticraft.gui.items.HTMLElement;
import opticraft.util.Window;

import java.util.ArrayList;

public class GuiHelper {

	public static boolean anyUpdatedHover(Window w, HTMLElement element) {
		int changes = 0;
		if(element.children != null) for(HTMLElement e : element.children) {
			changes += update(w, e);
			anyUpdatedHover(w, e);
		}
		return changes > 0;
	}

	public static int update(Window w, HTMLElement e) {
		boolean isOver = w.m.isOver(e);
		if((e.isHover() && !isOver) || (!e.isHover() && isOver)) {
			e.setHover(!e.isHover());
			return 1;
		}
		return 0;
	}

	public static void applyCSS(HTMLElement element, ArrayList<ElementCSS> styles, ArrayList<HTMLElement> ancestors) {
		for (ElementCSS css : styles) {
			CSSSelector sel = CSSSelector.parse(css.query);
			if (sel.matches(element, ancestors)) {
				element.styles.add(css);
			}
		}

		ArrayList<HTMLElement> newAncestors = new ArrayList<>(ancestors);
		newAncestors.add(0, element);
		if (element.children != null) {
			for (HTMLElement child : element.children) {
				applyCSS(child, styles, newAncestors);
			}
		}
	}
}
