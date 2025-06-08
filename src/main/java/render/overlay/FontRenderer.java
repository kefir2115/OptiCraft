package render.overlay;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.system.MemoryStack;
import util.Matrix4f;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBTruetype.*;

public class FontRenderer {
	private static final int FONT_BITMAP_WIDTH = 512;
	private static final int FONT_BITMAP_HEIGHT = 512;
	private static final int ASCII_START = 32;
	private static final int ASCII_END = 128;

	private static final int MAX_CHARS = 1024;
	private static final int VERTICES_PER_CHAR = 6;
	private static final int FLOATS_PER_VERTEX = 4;

	private FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(MAX_CHARS * VERTICES_PER_CHAR * FLOATS_PER_VERTEX);

	private int vao, vbo, shader, texId;
	private STBTTBakedChar.Buffer charData;

	public FontRenderer(String fontPath, int fontSize) throws Exception {
		ByteBuffer fontBuffer = ioResourceToByteBuffer(fontPath, 512 * 1024);
		ByteBuffer bitmap = BufferUtils.createByteBuffer(FONT_BITMAP_WIDTH * FONT_BITMAP_HEIGHT);
		charData = STBTTBakedChar.create(ASCII_END - ASCII_START);

		fontBuffer.rewind();
		if (stbtt_BakeFontBitmap(fontBuffer, fontSize, bitmap, FONT_BITMAP_WIDTH, FONT_BITMAP_HEIGHT, ASCII_START, charData) <= 0) {
			throw new RuntimeException("Failed to bake font.");
		}

		texId = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texId);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, FONT_BITMAP_WIDTH, FONT_BITMAP_HEIGHT, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

		vao = glGenVertexArrays();
		vbo = glGenBuffers();

		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, MAX_CHARS * VERTICES_PER_CHAR * FLOATS_PER_VERTEX * Float.BYTES, GL_DYNAMIC_DRAW);

		glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0); // aPos
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES); // aUV
		glEnableVertexAttribArray(1);

		glBindVertexArray(0);

		shader = compileShader("assets/shaders/font.vert", "assets/shaders/font.frag");
	}

	public void renderText(String text, float x, float y, float scale, int windowWidth, int windowHeight) {
		glUseProgram(shader);

		// Set projection
		float[] projection = Matrix4f.ortho2D(0, windowWidth, windowHeight, 0);
		int projectionLoc = glGetUniformLocation(shader, "projection");
		FloatBuffer projBuf = BufferUtils.createFloatBuffer(16);
		projBuf.put(projection).flip();
		glUniformMatrix4fv(projectionLoc, false, projBuf);

		// Set texture
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, texId);
		glUniform1i(glGetUniformLocation(shader, "fontTexture"), 0);

		// Prepare batched vertex buffer
		vertexBuffer.clear();

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		try (MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer xBuf = stack.floats(x);
			FloatBuffer yBuf = stack.floats(y);

			for (char c : text.toCharArray()) {
				if (c < ASCII_START || c >= ASCII_END) continue;

				STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);
				stbtt_GetBakedQuad(charData, FONT_BITMAP_WIDTH, FONT_BITMAP_HEIGHT, c - ASCII_START, xBuf, yBuf, q, true);

				float x0 = q.x0() * scale;
				float y0 = q.y0() * scale;
				float x1 = q.x1() * scale;
				float y1 = q.y1() * scale;

				vertexBuffer.put(new float[]{
						x0, y0, q.s0(), q.t0(),
						x1, y0, q.s1(), q.t0(),
						x1, y1, q.s1(), q.t1(),

						x0, y0, q.s0(), q.t0(),
						x1, y1, q.s1(), q.t1(),
						x0, y1, q.s0(), q.t1()
				});
			}
		}

		vertexBuffer.flip();

		// Upload and draw
		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
		glDrawArrays(GL_TRIANGLES, 0, vertexBuffer.limit() / 4);
		glBindVertexArray(0);
		glUseProgram(0);
	}

	public void cleanup() {
		glDeleteTextures(texId);
		glDeleteProgram(shader);
		glDeleteBuffers(vbo);
		glDeleteVertexArrays(vao);
	}

	private ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
		Path path = Paths.get(resource);
		if (!Files.exists(path)) throw new IOException("Font file does not exist: " + resource);
		if (!Files.isReadable(path)) throw new IOException("Font file not readable: " + resource);
		byte[] bytes = Files.readAllBytes(path);
		ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
		buffer.put(bytes);
		buffer.flip();
		return buffer;
	}

	private int compileShader(String vertexPath, String fragmentPath) throws IOException {
		String vert = Files.readString(Paths.get(vertexPath));
		String frag = Files.readString(Paths.get(fragmentPath));

		int vertId = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertId, vert);
		glCompileShader(vertId);
		if (glGetShaderi(vertId, GL_COMPILE_STATUS) == GL_FALSE)
			throw new RuntimeException("Vertex shader error: " + glGetShaderInfoLog(vertId));

		int fragId = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragId, frag);
		glCompileShader(fragId);
		if (glGetShaderi(fragId, GL_COMPILE_STATUS) == GL_FALSE)
			throw new RuntimeException("Fragment shader error: " + glGetShaderInfoLog(fragId));

		int programId = glCreateProgram();
		glAttachShader(programId, vertId);
		glAttachShader(programId, fragId);
		glLinkProgram(programId);
		if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE)
			throw new RuntimeException("Program link error: " + glGetProgramInfoLog(programId));

		glDeleteShader(vertId);
		glDeleteShader(fragId);
		return programId;
	}
}