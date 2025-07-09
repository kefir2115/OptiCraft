package opticraft.util;

public class BlockPos {
	private static final int NUM_X_BITS = 1 + MathHelper.calculateLogBaseTwo(MathHelper.roundUpToPowerOfTwo(30000000));
	private static final int NUM_Z_BITS = NUM_X_BITS;
	private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
	private static final int Y_SHIFT = NUM_Z_BITS;
	private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
	private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
	private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
	private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;

	long pos;

	public BlockPos(int x, int y, int z) {
		pos = ((long)x & X_MASK) << X_SHIFT | ((long)y & Y_MASK) << Y_SHIFT | ((long) z & Z_MASK);
	}

	public BlockPos(long l) {
		this.pos = l;
	}

	public long getPos()
	{
		return this.pos;
	}

	public int getX() {
		return (int)(pos << 64 - X_SHIFT - NUM_X_BITS >> 64 - NUM_X_BITS);
	}

	public int getY() {
		return (int)(pos << 64 - Y_SHIFT - NUM_Y_BITS >> 64 - NUM_Y_BITS);
	}

	public int getZ() {
		return (int)(pos << 64 - NUM_Z_BITS >> 64 - NUM_Z_BITS);
	}

	public float[] getModelMatrix() {
		return Matrix4f.translate(getX(), getY(), getZ());
	}
}
