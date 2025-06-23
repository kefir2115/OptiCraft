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

	int x, y, z;

	public BlockPos(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public long toLong()
	{
		return ((long)x & X_MASK) << X_SHIFT | ((long)y & Y_MASK) << Y_SHIFT | ((long) z & Z_MASK);
	}

	public static BlockPos fromLong(long l) {
		int i = (int)(l << 64 - X_SHIFT - NUM_X_BITS >> 64 - NUM_X_BITS);
		int j = (int)(l << 64 - Y_SHIFT - NUM_Y_BITS >> 64 - NUM_Y_BITS);
		int k = (int)(l << 64 - NUM_Z_BITS >> 64 - NUM_Z_BITS);
		return new BlockPos(i, j, k);
	}
}
