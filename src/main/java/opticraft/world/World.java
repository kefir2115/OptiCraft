package opticraft.world;

import opticraft.util.BlockPos;
import opticraft.world.chunk.Chunk;

import java.util.HashMap;
import java.util.Map;

public class World {

	Map<Long, Chunk> chunks;

	public World() {
		chunks = new HashMap<>();
	}

	public void animate() {
		chunks = new HashMap<>();

		int diff = (int) ((System.currentTimeMillis() / 100) % 1000);
		for(int x = -25; x < 25; x++) {
			for(int z = -25; z < 25; z++) {
				int y = (int) (3*Math.sin(x+diff)) - 1;
				y += (int) (3*Math.cos(z+diff)) - 1;

				setBlock((short) 1, x, y + 2, z);
			}
		}
		chunks.values().forEach(Chunk::rebuildMesh);
	}

	public void setBlock(short type, int x, int y, int z) {
		int cx = Math.floorDiv(x, 16);
		int cy = Math.floorDiv(y, 16);
		int cz = Math.floorDiv(z, 16);

		long key = new BlockPos(cx, cy, cz).getPos();
		Chunk chunk = chunks.computeIfAbsent(key, k -> new Chunk(cx, cy, cz));
		chunk.setBlock(type, x, y, z);
	}
}
