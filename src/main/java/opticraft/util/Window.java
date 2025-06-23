package opticraft.util;

public class Window {

	public Mouse m;
	public int w, h;
	public long gl;

	public Window(long gl) {
		this.gl = gl;
		this.m = new Mouse(0, 0);
		this.w = 0;
		this.h = 0;
	}
}
