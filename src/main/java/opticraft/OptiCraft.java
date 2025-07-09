package opticraft;

import opticraft.gui.HTMLCalculator;
import opticraft.gui.HTMLGui;
import opticraft.network.PacketUtils;
import opticraft.render.overlay.FontRenderer;
import opticraft.render.overlay.Rect;
import opticraft.util.Accounts;
import opticraft.util.Camera;
import opticraft.util.Matrix4f;
import opticraft.util.Window;
import opticraft.world.World;
import opticraft.world.WorldRenderer;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class OptiCraft {
	public Window window;
	public Camera camera;
	private int width = 1000, height = 600;
	private HTMLGui gui;
	public World world;

	public Accounts acc;
	public FontRenderer font;
	public PacketUtils pack;
	public HTMLCalculator htmlCalculator;
	public WorldRenderer worldRenderer;
	float[] projection = new float[16];

	long lastUpdate = -1;

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

		camera = new Camera(new Vector3f(0, 0, 0));

		init();
		loop();

		glfwFreeCallbacks(window.gl);
		glfwDestroyWindow(window.gl);
		glfwTerminate();
		glfwSetErrorCallback(null).free();

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

		setProjection();

		glfwSetWindowSizeCallback(window.gl, (w, width, height) -> {
			window.w = this.width = width;
			window.h = this.height = height;

			setProjection();
		});
		glfwSetCursorPosCallback(window.gl, (w, xpos, ypos) -> {
			float dx = (float) (xpos - window.m.x);
			float dy = (float) (ypos - window.m.y);

			window.m.x = (int) xpos;
			window.m.y = (int) ypos;

			camera.processMouseMovement(dx, dy);
		});

		glfwMakeContextCurrent(window.gl);
		glfwSwapInterval(1);
		glfwShowWindow(window.gl);
		GL.createCapabilities();

		try {
			worldRenderer = new WorldRenderer();

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
//		gui = null;
//		world = new World();
	}
	private void loop() {
		glEnable(GL_DEPTH_TEST);

		long currentTime = System.nanoTime();

		while (!glfwWindowShouldClose(window.gl)) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			if(world!=null) {
				world.animate();

				worldRenderer.renderWorld(world, camera.getViewMatrix().get(new float[16]), projection);

				float deltaTime = (currentTime - lastUpdate);

				boolean forward = glfwGetKey(window.gl, GLFW_KEY_W) == GLFW_PRESS;
				boolean backward = glfwGetKey(window.gl, GLFW_KEY_S) == GLFW_PRESS;
				boolean left = glfwGetKey(window.gl, GLFW_KEY_A) == GLFW_PRESS;
				boolean right = glfwGetKey(window.gl, GLFW_KEY_D) == GLFW_PRESS;
				boolean up = glfwGetKey(window.gl, GLFW_KEY_SPACE) == GLFW_PRESS;
				boolean down = glfwGetKey(window.gl, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS;

				camera.processKeyboard(forward, backward, left, right, up, down, deltaTime);
			}
			renderOverlay();

			glfwSwapBuffers(window.gl);
			glfwPollEvents();
		}

		lastUpdate = currentTime;
	}

	private void renderOverlay() {
		glUseProgram(0);
		glViewport(0, 0, window.w, window.h);
		if(gui != null) {
			gui.render(window);
		}

		font.renderText(
				String.format("XYZ: %s / %s / %s", camera.getPosition().x, camera.getPosition().y, camera.getPosition().z),
				0, 0, 1
		);
		font.renderText(
				String.format("ROT: %s / %S", camera.yaw, camera.pitch),
				0, FontRenderer.FONT_SIZE + 5, 1
		);
	}

	public void setGui(HTMLGui gui) {
		if(this.gui != null) this.gui.onClose();
		this.gui = gui;
		this.gui.onOpen();

		if(this.gui != null) glfwSetInputMode(window.gl, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		else glfwSetInputMode(window.gl, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
	}


	private void setProjection() {
		projection = Matrix4f.perspective(45.0f, (float) width / height, 0.1f, 100.0f);
	}

	public static void main(String[] args) {
		new OptiCraft().run();
	}
}