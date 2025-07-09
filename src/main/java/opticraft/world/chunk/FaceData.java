package opticraft.world.chunk;

import opticraft.render.r3d.Direction;

public class FaceData {
	public static float[][] getFaceVertices(Direction dir) {
		switch (dir) {
			case UP: return new float[][]{
					{0, 1, 0}, {1, 1, 0}, {1, 1, 1}, {0, 1, 1}};
			case DOWN: return new float[][]{
					{0, 0, 0}, {0, 0, 1}, {1, 0, 1}, {1, 0, 0}};
			case FRONT: return new float[][]{
					{0, 0, 0}, {1, 0, 0}, {1, 1, 0}, {0, 1, 0}};
			case BACK: return new float[][]{
					{0, 0, 1}, {0, 1, 1}, {1, 1, 1}, {1, 0, 1}};
			case LEFT: return new float[][]{
					{0, 0, 0}, {0, 1, 0}, {0, 1, 1}, {0, 0, 1}};
			case RIGHT: return new float[][]{
					{1, 0, 0}, {1, 0, 1}, {1, 1, 1}, {1, 1, 0}};
			default: return null;
		}
	}
}