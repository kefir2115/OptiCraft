package opticraft.gui.css;

import opticraft.gui.items.HTMLElement;

public class MultiDim {

	public int left, top, right, bottom, def, defHoriz;
	public MultiDim(HTMLElement e, String css) {
		String style;
		if(e == null || (style = e.getStyle(css)) == null) {
			left = top = right = bottom = def = defHoriz = 0;
			return;
		}

		String[] styles = {
				e.getStyle(css+"-left"),
				e.getStyle(css+"-top"),
				e.getStyle(css+"-right"),
				e.getStyle(css+"-bottom"),
		};

		String[] segs = style.split(" ");
		int segments = segs.length;
		if(segments > 1) {
			if(segments == 2) {
				top = bottom = CSSParser.parsev(segs[0], 0, false);
				left = right = CSSParser.parsev(segs[1], 0, true);
			} else if(segments == 4) {
				top = CSSParser.parsev(segs[0], 0, false);
				right = CSSParser.parsev(segs[1], 0, true);
				bottom = CSSParser.parsev(segs[2], 0, false);
				left = CSSParser.parsev(segs[3], 0, true);
			}
		} else {
			def = CSSParser.parsev(style, 0, false);
			defHoriz = CSSParser.parsev(style, 0, true);
			left = CSSParser.parsev(styles[0], defHoriz, true);
			top = CSSParser.parsev(styles[1], def, true);
			right = CSSParser.parsev(styles[2], defHoriz, true);
			bottom = CSSParser.parsev(styles[3], def, true);

		}
		left = CSSParser.parsev(styles[0], left, true);
		top = CSSParser.parsev(styles[1], top, true);
		right = CSSParser.parsev(styles[2], right, true);
		bottom = CSSParser.parsev(styles[3], bottom, true);
	}
}
