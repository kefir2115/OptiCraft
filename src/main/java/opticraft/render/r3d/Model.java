package opticraft.render.r3d;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.system.MemoryUtil;

public class Model {
	private int vaoId;
	private int vboId;
	private int eboId;
	private int vertexCount;

	public Model(float[] interleavedVertices, int[] indices) {
		vertexCount = indices.length;

		vaoId = glGenVertexArrays();
		glBindVertexArray(vaoId);

		vboId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
		FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(interleavedVertices.length);
		vertexBuffer.put(interleavedVertices).flip();
		glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

		int stride = 8 * Float.BYTES;
		glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 3 * Float.BYTES);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, stride, 5 * Float.BYTES);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		MemoryUtil.memFree(vertexBuffer);

		eboId = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
		IntBuffer indexBuffer = MemoryUtil.memAllocInt(indices.length);
		indexBuffer.put(indices).flip();
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
		MemoryUtil.memFree(indexBuffer);

		glBindVertexArray(0);
	}

	public void render() {
		glBindVertexArray(vaoId);
		glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
		glBindVertexArray(0);
	}

	public void cleanup() {
		glDisableVertexAttribArray(0);
		glDeleteBuffers(vboId);
		glDeleteBuffers(eboId);
		glDeleteVertexArrays(vaoId);
	}
}