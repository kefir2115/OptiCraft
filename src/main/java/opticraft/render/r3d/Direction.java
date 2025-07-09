package opticraft.render.r3d;

public enum Direction {
	UP(0, 1, 0), DOWN(0, -1, 0),
	LEFT(-1, 0, 0), RIGHT(1, 0, 0),
	FRONT(0, 0, -1), BACK(0, 0, 1);

	public final int dx, dy, dz;
	Direction(int dx, int dy, int dz) {
		this.dx = dx; this.dy = dy; this.dz = dz;
	}
}