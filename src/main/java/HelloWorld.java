import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import network.PacketUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import render.overlay.FontRenderer;
import util.Accounts;
import util.Matrix4f;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.*;

public class HelloWorld {
	private long window;
	private int width = 1000, height = 600;
	private int shaderProgram;
	private float angle = 0.0f;

	Accounts acc;
	FontRenderer font;
	PacketUtils pack;
	private ArrayList<Model> models;

	public void run() {
		acc = new Accounts();
		acc.loadAccounts();
		MicrosoftAuthResult res = Accounts.login();
		if(res == null) {
			System.out.println("Account is null!");
			return;
		}

		pack = new PacketUtils();
		pack.ping("127.0.0.1", 25565);
		pack.login("127.0.0.1", 25565, res.getProfile().getName(), res.getProfile().getId(), res.getAccessToken());
		pack.play();

//		init();
//		loop();
//
//		glfwFreeCallbacks(window);
//		glfwDestroyWindow(window);
//		glfwTerminate();
//		glfwSetErrorCallback(null).free();
//		for(Model m : models) {
//			m.cleanup();
//		}
//		font.cleanup();
	}

	private void init() {
		GLFWErrorCallback.createPrint(System.err).set();
		if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

		window = glfwCreateWindow(width, height, "Rotating Cube - Modern OpenGL", NULL, NULL);
		if (window == NULL) throw new RuntimeException("Failed to create GLFW window");

		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);
		GL.createCapabilities();

		setupShaders();

		try {
			models = new ArrayList<>();
			models.add(OBJLoader.loadModel("assets/objects/block.obj"));

			font = new FontRenderer("assets/fonts/font.ttf", 24);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void setupShaders() {
		String vertexShaderSource = """
			#version 330 core
			layout(location = 0) in vec3 aPos;
			layout(location = 1) in vec2 aTexCoord;
			layout(location = 2) in vec3 aNormal;
								
			uniform mat4 model;
			uniform mat4 view;
			uniform mat4 projection;
								
			out vec2 TexCoord;
			out vec3 Normal;
			out vec3 FragPos;
								
			void main() {
				FragPos = vec3(model * vec4(aPos, 1.0));
				Normal = mat3(transpose(inverse(model))) * aNormal;
				TexCoord = aTexCoord;
				gl_Position = projection * view * vec4(FragPos, 1.0);
			}
		""";

		String fragmentShaderSource = """
			#version 330 core
				  
			in vec2 TexCoord;
			in vec3 Normal;
			in vec3 FragPos;
			  
			out vec4 FragColor;
			  
			uniform sampler2D texture0;
			  
			void main() {
				FragColor = vec4(1.0, 1.0, 1.0, 1.0);
			}
		""";

		int vertexShader = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexShader, vertexShaderSource);
		glCompileShader(vertexShader);
		if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE)
			throw new RuntimeException("Vertex shader compilation failed:\n" + glGetShaderInfoLog(vertexShader));

		int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragmentShader, fragmentShaderSource);
		glCompileShader(fragmentShader);
		if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE)
			throw new RuntimeException("Fragment shader compilation failed:\n" + glGetShaderInfoLog(fragmentShader));

		shaderProgram = glCreateProgram();
		glAttachShader(shaderProgram, vertexShader);
		glAttachShader(shaderProgram, fragmentShader);
		glLinkProgram(shaderProgram);
		if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE)
			throw new RuntimeException("Shader linking failed:\n" + glGetProgramInfoLog(shaderProgram));

		glDeleteShader(vertexShader);
		glDeleteShader(fragmentShader);
	}

	private void loop() {
		glEnable(GL_DEPTH_TEST);

		while (!glfwWindowShouldClose(window)) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glUseProgram(shaderProgram);

			angle += 0.01f;
			float[] model = new float[16];
			float[] view = new float[16];
			float[] projection = new float[16];

			model = Matrix4f.rotate(angle, 0.5f, 1.0f, 0.0f);
			view = Matrix4f.translate(0.0f, 0.0f, -10.0f);
			projection = Matrix4f.perspective(45.0f, (float) width / height, 0.1f, 100.0f);

			int modelLoc = glGetUniformLocation(shaderProgram, "model");
			int viewLoc = glGetUniformLocation(shaderProgram, "view");
			int projLoc = glGetUniformLocation(shaderProgram, "projection");

			glUniformMatrix4fv(modelLoc, false, model);
			glUniformMatrix4fv(viewLoc, false, view);
			glUniformMatrix4fv(projLoc, false, projection);

			for(Model m : models) {
				m.render();
			}

			glUseProgram(0);

			renderOverlay();

			glfwSwapBuffers(window);
			glfwPollEvents();
		}
	}

	private void renderOverlay() {
		int[] dimw = new int[1], dimh = new int[1];
		glfwGetWindowSize(window, dimw, dimh);

		int w = dimw[0], h = dimh[0];

		font.renderText("Test napis", 100, 100, 1.0f, w, h);
	}

	public static void main(String[] args) {
		new HelloWorld().run();
	}
}