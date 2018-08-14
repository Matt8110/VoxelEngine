package Blocks;

import Core.VAO;
import VoxelEngine.Main;

public class BlockSelection {

	private VAO vao = new VAO();
	
	public BlockSelection() {
		
		float[] vertices = {
				-0.01f, -0.01f, -0.01f,
				1.01f, -0.01f, -0.01f,//Back bottom
				-0.01f, 1.01f, -0.01f,
				1.01f, 1.01f, -0.01f,//Back top
				-0.01f, -0.01f, 1.01f,
				1.01f, -0.01f, 1.01f,//Front bottom
				-0.01f, 1.01f, 1.01f,
				1.01f, 1.01f, 1.01f,//Front top
				-0.01f, -0.01f, -0.01f,
				-0.01f, 1.01f, -0.01f,//Back left
				-0.01f, -0.01f, 1.01f,
				-0.01f, 1.01f, 1.01f,//Front left
				1.01f, -0.01f, -0.01f,
				1.01f, 1.01f, -0.01f,//Back right
				1.01f, -0.01f, 1.01f,
				1.01f, 1.01f, 1.01f,//Front right
				-0.01f, -0.01f, -0.01f,
				-0.01f, -0.01f, 1.01f,//Left bottom
				-0.01f, 1.01f, -0.01f,
				-0.01f, 1.01f, 1.01f,//Left top
				1.01f, -0.01f, -0.01f,
				1.01f, -0.01f, 1.01f,//Right bottom
				1.01f, 1.01f, -0.01f,
				1.01f, 1.01f, 1.01f//Right top
		};
		
		float[] colors = {
				1, 1, 1,
				1, 1, 1,//Back bottom
				1, 1, 1,
				1, 1, 1,//Back top
				1, 1, 1,
				1, 1, 1,//Front bottom
				1, 1, 1,
				1, 1, 1,//Front top
				1, 1, 1,
				1, 1, 1,//Back left
				1, 1, 1,
				1, 1, 1,//Front left
				1, 1, 1,
				1, 1, 1,//Back right
				1, 1, 1,
				1, 1, 1,//Front right
				1, 1, 1,
				1, 1, 1,//Left bottom
				1, 1, 1,
				1, 1, 1,//Left top
				1, 1, 1,
				1, 1, 1,//Right bottom
				1, 1, 1,
				1, 1, 1//Right top
		};

		
		vao.putDataInColorVAO(vertices, colors);
		
	}
	
	public void render(float x, float y, float z) {
		
		Main.colorShader.setTransformation(x, y, z, 1.0f);
		vao.renderLines();
		
	}
	
}
