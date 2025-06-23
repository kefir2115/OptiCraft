package opticraft.render;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glDeleteShader;

public class Shader {

	public int shaderId;

	public Shader(String path) throws IOException {
		this(Files.readString(Paths.get(path + ".frag")), Files.readString(Paths.get(path + ".vert")));
	}

	public Shader(String frag, String vert) {
		int vertexShader = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexShader, vert);
		glCompileShader(vertexShader);
		if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE)
			throw new RuntimeException("Vertex shader compilation failed:\n" + glGetShaderInfoLog(vertexShader));

		int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragmentShader, frag);
		glCompileShader(fragmentShader);
		if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE)
			throw new RuntimeException("Fragment shader compilation failed:\n" + glGetShaderInfoLog(fragmentShader));

		shaderId = glCreateProgram();
		glAttachShader(shaderId, vertexShader);
		glAttachShader(shaderId, fragmentShader);
		glLinkProgram(shaderId);
		if (glGetProgrami(shaderId, GL_LINK_STATUS) == GL_FALSE)
			throw new RuntimeException("Shader linking failed:\n" + glGetProgramInfoLog(shaderId));

		glDeleteShader(vertexShader);
		glDeleteShader(fragmentShader);
	}
}
