package opticraft.world.block;

import opticraft.util.BlockPos;

public class Block {

	BlockPos pos;
	public short type;
	String data;

	public Block(short type, int x, int y, int z) {
		this.type = type;
		this.pos = new BlockPos(x, y, z);
		this.data = "";
	}

	public void setData(String data) {
		this.data = data;
	}

	public BlockPos getPos() {
		return pos;
	}
}
