package opticraft.render.overlay;

import opticraft.render.Shader;
import opticraft.util.Window;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.ARBVertexArrayObject.*;
import static org.lwjgl.opengl.GL20.*;

public class Rect {

	private static int program = -1;
	private static int vbo, ebo, vao;

	public Rect() {
		try {
			if(program == -1) {
				float[] vertices = {
						-0.5f,  0.5f, // top-left
						0.5f,  0.5f, // top-right
						0.5f, -0.5f, // bottom-right
						-0.5f, -0.5f  // bottom-left
				};

				int[] indices = {
						0, 1, 2,
						2, 3, 0
				};

				String vert = Files.readString(Paths.get("assets/shaders/rect.vert"));
				String frag = Files.readString(Paths.get("assets/shaders/rect.frag"));
				program = new Shader(frag, vert).shaderId;

				vao = glGenVertexArrays();
				glBindVertexArray(vao);

				vbo = glGenBuffers();
				glBindBuffer(GL_ARRAY_BUFFER, vbo);
				glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

				ebo = glGenBuffers();
				glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
				glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

				glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
				glEnableVertexAttribArray(0);

				glBindVertexArray(0);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void renderRect(Window window, float x, float y, float w, float h, Color color) {
		// Convert screen coords to NDC
		float ndcX = (x / window.w) * 2.0f - 1.0f;
		float ndcY = 1.0f - ((y + h) / window.h) * 2.0f; // y flipped & bottom aligned

		float ndcW = (w / window.w) * 2.0f;
		float ndcH = (h / window.h) * 2.0f;

		glUseProgram(program);

		int locPos = glGetUniformLocation(program, "uPosition");
		int locSize = glGetUniformLocation(program, "uSize");
		int locColor = glGetUniformLocation(program, "uColor");

		if (locPos < 0 || locSize < 0 || locColor < 0) {
			System.err.println("Shader uniform location not found");
			return;
		}

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glDepthMask(false);

		glUniform2f(locPos, ndcX + ndcW / 2.0f, ndcY + ndcH / 2.0f); // center-based
		glUniform2f(locSize, ndcW, ndcH);
		float[] rgba = color.getRGBComponents(null);
		glUniform4f(locColor, rgba[0], rgba[1], rgba[2], rgba[3]);

		glBindVertexArray(vao);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

		glDepthMask(true);
		glDisable(GL_BLEND);
	}
}
