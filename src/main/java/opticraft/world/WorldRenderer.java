package opticraft.world;

import opticraft.render.Shader;
import opticraft.render.r3d.Model;
import opticraft.util.OBJLoader;
import opticraft.world.block.Block;
import opticraft.world.chunk.Chunk;

import java.io.IOException;

import static org.lwjgl.opengl.GL20.*;

public class WorldRenderer {

	private int viewLoc, projLoc, modelLoc;
	int modelRenderProgram;
	Model blockModel;

	public WorldRenderer() {
		try {
			modelRenderProgram = new Shader("assets/shaders/model").shaderId;
			blockModel = OBJLoader.loadModel("assets/objects/block.obj");

			viewLoc = glGetUniformLocation(modelRenderProgram, "view");
			projLoc = glGetUniformLocation(modelRenderProgram, "projection");
			modelLoc = glGetUniformLocation(modelRenderProgram, "model");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	public void renderWorld(World world, float[] view, float[] projection) {
		glUseProgram(modelRenderProgram);

		glUniformMatrix4fv(viewLoc, false, view);
		glUniformMatrix4fv(projLoc, false, projection);


		for(Chunk chunk : world.chunks.values()) {
			renderChunk(chunk);
		}
	}

	public void renderChunk(Chunk chunk) {
		Block[][][] data = chunk.getData();
		for(int i = 0; i < 4096; i++) {
			Block block = data[i % 16][i / 16 % 16][i / 256];
			if(block == null) continue;

			glUniformMatrix4fv(modelLoc, false, chunk.getPos().getModelMatrix());
			chunk.getMesh().render();
		}
	}
}
