package opticraft.world.chunk;

import opticraft.render.r3d.Direction;
import opticraft.util.BlockPos;
import opticraft.world.block.Block;

import java.util.ArrayList;
import java.util.List;

public class Chunk {

	BlockPos chunkPos;
	Block[][][] data;
	public ChunkMesh mesh;

	public Chunk(int x, int y, int z) {
		this.data = new Block[16][16][16];
		this.chunkPos = new BlockPos(x, y, z);
		this.mesh = new ChunkMesh();
	}


	public void setBlock(short type, int x, int y, int z) {
		if (!isInsideChunk(x, y, z)) return;

		int lx = Math.floorMod(x, 16);
		int ly = Math.floorMod(y, 16);
		int lz = Math.floorMod(z, 16);

		Block b = data[lx][ly][lz];
		if(b == null) data[lx][ly][lz] = new Block(type, x, y, z);
		else {
			b.type = type;
			b.setData("");
		}
	}

	public void setBlockData(String dataStr, int x, int y, int z) {
		if (!isInsideChunk(x, y, z)) return;

		int lx = Math.floorMod(x, 16);
		int ly = Math.floorMod(y, 16);
		int lz = Math.floorMod(z, 16);

		Block b = data[lx][ly][lz];
		if (b != null) b.setData(dataStr);
	}

	public Block getBlock(int x, int y, int z) {
		try {
			if(x < 0 || x > 16 || y < 0 || y > 16 || z < 0 || z > 16) return null;
			return data[x][y][z];
		} catch(ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	public BlockPos getPos() {
		return chunkPos;
	}

	public Block[][][] getData() {
		return data;
	}

	private boolean isInsideChunk(int x, int y, int z) {
		int cx = Math.floorDiv(x, 16);
		int cy = Math.floorDiv(y, 16);
		int cz = Math.floorDiv(z, 16);
		return cx == chunkPos.getX() && cy == chunkPos.getY() && cz == chunkPos.getZ();
	}

	public void rebuildMesh() {
		List<Float> vertices = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();
		int indexOffset = 0;

		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 16; y++) {
				for (int z = 0; z < 16; z++) {
					Block b = data[x][y][z];
					if (b == null) continue;

					float bx = chunkPos.getX() * 16 + x;
					float by = chunkPos.getY() * 16 + y;
					float bz = chunkPos.getZ() * 16 + z;

					// Only add faces if there is air next to it
					for (Direction dir : Direction.values()) {
						int nx = x + dir.dx;
						int ny = y + dir.dy;
						int nz = z + dir.dz;

						if (getBlock(nx, ny, nz) == null) {
							addFace(vertices, indices, bx, by, bz, dir, indexOffset);
							indexOffset += 4;
						}
					}
				}
			}
		}

		// convert to array
		float[] vArray = new float[vertices.size()];
		int[] iArray = new int[indices.size()];
		for (int i = 0; i < vArray.length; i++) vArray[i] = vertices.get(i);
		for (int i = 0; i < iArray.length; i++) iArray[i] = indices.get(i);

		mesh.build(vArray, iArray);
	}

	private void addFace(List<Float> v, List<Integer> i, float x, float y, float z, Direction d, int offset) {
		float[][] face = FaceData.getFaceVertices(d);
		for (float[] p : face) {
			v.add(x + p[0]);
			v.add(y + p[1]);
			v.add(z + p[2]);
		}
		i.add(offset); i.add(offset + 1); i.add(offset + 2);
		i.add(offset + 2); i.add(offset + 3); i.add(offset);
	}

	public ChunkMesh getMesh() {
		return mesh;
	}
}
