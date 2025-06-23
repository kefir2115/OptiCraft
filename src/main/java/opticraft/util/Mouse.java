package opticraft.util;

import opticraft.gui.items.HTMLElement;

public class Mouse {

	public int x, y;

	public Mouse(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public boolean isOver(HTMLElement e) {
		return x >= e.x && x <= e.x + e.w && y >= e.y && y <= e.y + e.h;
	}
}
