package util;

import static java.lang.Math.*;

public class Matrix4f {

	public static float[] identity() {
		float[] m = new float[16];
		m[0] = 1f; m[5] = 1f; m[10] = 1f; m[15] = 1f;
		return m;
	}

	public static float[] translate(float x, float y, float z) {
		float[] m = identity();
		m[12] = x;
		m[13] = y;
		m[14] = z;
		return m;
	}

	public static float[] rotate(float angle, float x, float y, float z) {
		float[] m = identity();
		float r = angle;
		float c = (float) cos(r);
		float s = (float) sin(r);
		float oneMinusC = 1.0f - c;

		float len = (float) sqrt(x * x + y * y + z * z);
		if (len != 0f) {
			x /= len;
			y /= len;
			z /= len;
		}

		m[0]  = x * x * oneMinusC + c;
		m[1]  = y * x * oneMinusC + z * s;
		m[2]  = x * z * oneMinusC - y * s;
		m[3]  = 0;

		m[4]  = x * y * oneMinusC - z * s;
		m[5]  = y * y * oneMinusC + c;
		m[6]  = y * z * oneMinusC + x * s;
		m[7]  = 0;

		m[8]  = x * z * oneMinusC + y * s;
		m[9]  = y * z * oneMinusC - x * s;
		m[10] = z * z * oneMinusC + c;
		m[11] = 0;

		m[12] = 0;
		m[13] = 0;
		m[14] = 0;
		m[15] = 1;

		return m;
	}

	public static float[] perspective(float fov, float aspect, float zNear, float zFar) {
		float[] m = new float[16];
		float tanHalfFOV = (float) tan(Math.toRadians(fov) / 2.0f);

		m[0] = 1.0f / (aspect * tanHalfFOV);
		m[5] = 1.0f / (tanHalfFOV);
		m[10] = -(zFar + zNear) / (zFar - zNear);
		m[11] = -1.0f;
		m[14] = -(2.0f * zFar * zNear) / (zFar - zNear);
		m[15] = 0.0f;

		return m;
	}

	public static float[] multiply(float[] a, float[] b) {
		float[] result = new float[16];
		for (int row = 0; row < 4; row++)
			for (int col = 0; col < 4; col++)
				result[col + row * 4] =
						a[row * 4] * b[col] +
								a[row * 4 + 1] * b[col + 4] +
								a[row * 4 + 2] * b[col + 8] +
								a[row * 4 + 3] * b[col + 12];
		return result;
	}

	public static float[] ortho2D(float left, float right, float bottom, float top) {
		float[] mat = new float[16];
		mat[0] = 2.0f / (right - left);
		mat[5] = 2.0f / (top - bottom);
		mat[10] = -1.0f;
		mat[12] = - (right + left) / (right - left);
		mat[13] = - (top + bottom) / (top - bottom);
		mat[15] = 1.0f;
		return mat;
	}
}