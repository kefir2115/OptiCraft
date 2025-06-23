package opticraft.gui;

import opticraft.OptiCraft;
import opticraft.gui.css.CSSParser;
import opticraft.gui.css.MultiDim;
import opticraft.gui.items.HTMLElement;
import opticraft.gui.items.HTMLText;
import opticraft.render.overlay.FontRenderer;

public class HTMLCalculator {

	public HTMLElement calculateDimensions(HTMLElement element) {
		return calculateDimensions(element, 0, 0);
	}

	protected HTMLElement calculateDimensions(HTMLElement element, int parentAbsX, int parentAbsY) {
		if(element instanceof HTMLText) return calculateText((HTMLText) element, parentAbsX, parentAbsY);
		String display = element.getStyle("display");
		String position = element.getStyle("position");

		element.padding = new MultiDim(element, "padding");
		element.margin = new MultiDim(element, "margin");

		int styledWidth = CSSParser.parsev(element.getStyle("width"), 0, true);
		int styledHeight = CSSParser.parsev(element.getStyle("height"), 0, false);

		element.x = parentAbsX + element.margin.left;
		element.y = parentAbsY + element.margin.top;

		// Absolutes go first
		if ("absolute".equalsIgnoreCase(position)) {
			element.x = CSSParser.parsev(element.getStyle("left"), parentAbsX, true);
			element.y = CSSParser.parsev(element.getStyle("top"), parentAbsY, false);

			calculateSelfSize(element, styledWidth, styledHeight);
			if ("flex".equalsIgnoreCase(display)) {
				calculateFlex(element, styledWidth, styledHeight);
			} else {
				calculateBlock(element, styledWidth, styledHeight);
			}

			return element;
		}


		// Flex layout
		if ("flex".equalsIgnoreCase(display)) {
			calculateFlex(element, styledWidth, styledHeight);
		} else {
			calculateBlock(element, styledWidth, styledHeight);
		}

		return element;
	}

	private HTMLElement calculateText(HTMLText element, int parentAbsX, int parentAbsY) {
		FontRenderer fr = OptiCraft.get().font;

		element.padding = new MultiDim(null, "padding");
		element.margin = new MultiDim(null, "margin");

		int textWidth = (int) fr.getWidth(element.content, 1);
		int textHeight = FontRenderer.FONT_SIZE;

		element.w = textWidth;
		element.h = textHeight;
		element.x = parentAbsX;
		element.y = parentAbsY;

		return element;
	}

	private void calculateSelfSize(HTMLElement element, int styledWidth, int styledHeight) {
		int contentW = Math.max(styledWidth, 0);
		int contentH = Math.max(styledHeight, 0);

		element.w = element.padding.left + contentW + element.padding.right;
		element.h = element.padding.top + contentH + element.padding.bottom;
	}

	private void calculateFlex(HTMLElement element, int styledWidth, int styledHeight) {
		String direction = element.getStyle("flex-direction");
		boolean isRow = !"column".equalsIgnoreCase(direction);

		int cursor = isRow ? element.padding.left : element.padding.top;
		int maxCross = 0;

		for (HTMLElement child : element.children) {
			calculateDimensions(child,
					isRow ? element.x + cursor : element.x + element.padding.left,
					isRow ? element.y + element.padding.top : element.y + cursor);
			if (isRow) {
				cursor += child.w + child.margin.left + child.margin.right;
				maxCross = Math.max(maxCross, child.h + child.margin.top + child.margin.bottom);
			} else {
				cursor += child.h + child.margin.top + child.margin.bottom;
				maxCross = Math.max(maxCross, child.w + child.margin.left + child.margin.right);
			}
		}

		int contentW = isRow ? cursor : maxCross;
		int contentH = isRow ? maxCross : cursor;

		if (styledWidth > 0) contentW = styledWidth;
		if (styledHeight > 0) contentH = styledHeight;

		element.w = element.padding.left + contentW + element.padding.right;
		element.h = element.padding.top + contentH + element.padding.bottom;
	}

	protected HTMLElement calculateBlock(HTMLElement element, int width, int height) {
		String display = element.getStyle("display");
		if ("none".equalsIgnoreCase(display)) {
			element.w = 0;
			element.h = 0;
			return element;
		}

		element.padding = new MultiDim(element, "padding");
		element.margin = new MultiDim(element, "margin");

		int contentW = 0;
		int contentH = 0;
		int contentX = element.x + element.padding.left;
		int contentY = element.y + element.padding.top;

		if (element.children != null) {
			for (HTMLElement child : element.children) {
				calculateDimensions(child, contentX, contentY + contentH);

				contentH += child.h + child.margin.top + child.margin.bottom;
				contentW = Math.max(contentW, child.w + child.margin.left + child.margin.right);
			}
		}

		if (width != 0) contentW = width - element.padding.left - element.padding.right;
		if (height != 0) contentH = height - element.padding.top - element.padding.bottom;

		element.w = element.padding.left + contentW + element.padding.right;
		element.h = element.padding.top + contentH + element.padding.bottom;

		return element;
	}
}
