package opticraft;

import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import opticraft.gui.HTMLCalculator;
import opticraft.gui.HTMLGui;
import opticraft.gui.HTMLParser;
import opticraft.gui.css.CSSParser;
import opticraft.network.PacketUtils;
import opticraft.render.Shader;
import opticraft.render.overlay.Rect;
import opticraft.render.r3d.Model;
import opticraft.util.OBJLoader;
import opticraft.util.Window;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import opticraft.render.overlay.FontRenderer;
import opticraft.util.Accounts;
import opticraft.util.Matrix4f;

import java.awt.*;
import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.ArrayList;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.*;

public class OptiCraft {
	public Window window;
	private int width = 1000, height = 600;
	private int shaderProgram;
	private float angle = 0.0f;
	private HTMLGui gui;

	public Accounts acc;
	public FontRenderer font;
	public PacketUtils pack;
	public HTMLCalculator htmlCalculator;
	private ArrayList<Model> models;

	private static OptiCraft i;
	public static OptiCraft get() { return i; }

	public void run() {
		i = this;
		acc = new Accounts();
		acc.loadAccounts();
//		MicrosoftAuthResult res = Accounts.login();
//		if(res == null) {
//			System.out.println("Account is null!");
//			return;
//		}
		pack = new PacketUtils();
		htmlCalculator = new HTMLCalculator();

		init();
		loop();

		glfwFreeCallbacks(window.gl);
		glfwDestroyWindow(window.gl);
		glfwTerminate();
		glfwSetErrorCallback(null).free();
		for(Model m : models) {
			m.cleanup();
		}
		font.cleanup();
	}

	private void init() {
		GLFWErrorCallback.createPrint(System.err).set();
		if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

		window = new Window(glfwCreateWindow(width, height, "OptiCraft", NULL, NULL));
		if (window.gl == NULL) throw new RuntimeException("Failed to create GLFW window");
		window.w = width;
		window.h = height;

		glfwSetWindowSizeCallback(window.gl, (w, width, height) -> {
			window.w = width;
			window.h = height;
		});
		glfwSetCursorPosCallback(window.gl, (w, xpos, ypos) -> {
			window.m.x = (int) xpos;
			window.m.y = (int) ypos;
		});

		glfwMakeContextCurrent(window.gl);
		glfwSwapInterval(1);
		glfwShowWindow(window.gl);
		GL.createCapabilities();

		try {
			shaderProgram = new Shader("assets/shaders/model").shaderId;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try {
			models = new ArrayList<>();
			models.add(OBJLoader.loadModel("assets/objects/block.obj"));

			font = new FontRenderer("assets/fonts/font.ttf");
		} catch(Exception e) {
			e.printStackTrace();
		}

		new Rect();

		gui = new HTMLGui("""
					<body>
				          <div class="container">
				            <a class="link">aaaaaaaaaaaaaaaaaaaaa</a>
				          </div>
				          Siemano
				          <div class="box">
				          	<p>1</p>
				          	<p>2 aaaa</p>
				          	<p>3 bbbbbbbb</p>
				          </div>
				        </body>
				""", """
    				body {
    					color: red;
    					width: 100%;
    					height: 100%;
    					
    					padding: 10%;
    				}
					a {
						color: #00f;
						
						margin: 20px;
						padding: 10px 50px;
						
						background: #ffff00;
					}
					div {
						background: #f0f;
					}
					body, a {
						padding-left: 150px;
					}
    				a:hover {
    					padding: 15px 40px;
    					margin-left: 40px;
    					color: #0f0;
    				}
    				
    				.box {
    					display: flex;
    					flex-direction: row;
    					
    					position: absolute;
    					top: 50%;
    					left: 50%;
    				}
    				.box > p {
    					margin: 0 10px;
    					padding: 10px;
    				}
    				.box > p:hover {
    					background: #f00;
    				}
				""");
	}
	private void loop() {
		glEnable(GL_DEPTH_TEST);

		while (!glfwWindowShouldClose(window.gl)) {
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

			glfwSwapBuffers(window.gl);
			glfwPollEvents();
		}
	}

	private void renderOverlay() {
		glViewport(0, 0, window.w, window.h);
		if(gui != null) {
			gui.render(window);
		}
	}

	public static void main(String[] args) {
		new OptiCraft().run();
	}
}