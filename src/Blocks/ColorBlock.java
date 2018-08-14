package Blocks;

import org.lwjgl.opengl.GL11;

import Core.VAO;

public class ColorBlock {

	private VAO vao = new VAO();
	
	public ColorBlock() {
		
		float[] vertices = {
			0, 0, 0,
			1, 0, 0,
			0, 1, 0,
			0, 1, 0,
			1, 0, 0,
			1, 1, 0,//Back
			0, 0, 1,
			1, 0, 1,
			0, 1, 1,
			0, 1, 1,
			1, 0, 1,
			1, 1, 1,//Front
			0, 0, 0,
			0, 0, 1,
			0, 1, 0,
			0, 1, 0,
			0, 0, 1,
			0, 1, 1,//Left
			1, 0, 0,
			1, 0, 1,
			1, 1, 0,
			1, 1, 0,
			1, 0, 1,
			1, 1, 1,//Right
			0, 0, 0,
			0, 0, 1,
			1, 0, 1,
			1, 0, 1,
			0, 0, 0,
			1, 0, 0,//Bottom
			0, 1, 0,
			0, 1, 1,
			1, 1, 1,
			1, 1, 1,
			0, 1, 0,
			1, 1, 0,//Top
			
		};
		
		float[] colors = {
			1.0f, 0.0f, 0.0f,
			1.0f, 0.0f, 0.0f,
			1.0f, 0.0f, 0.0f,
			1.0f, 0.0f, 0.0f,
			1.0f, 0.0f, 0.0f,
			1.0f, 0.0f, 0.0f,//Back
			
			0.0f, 1.0f, 0.0f,
			0.0f, 1.0f, 0.0f,
			0.0f, 1.0f, 0.0f,
			0.0f, 1.0f, 0.0f,
			0.0f, 1.0f, 0.0f,
			0.0f, 1.0f, 0.0f,//Front
			
			0.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f,//Left
			
			1.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f,//Right
			
			1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 1.0f,//Bottom
			
			1.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 1.0f,//Top
			
		};
		
		vao.putDataInColorVAO(vertices, colors);
		
	}
	
	public void render() {
		
		GL11.glDisable(GL11.GL_CULL_FACE);
		vao.renderColors();
		GL11.glEnable(GL11.GL_CULL_FACE);
		
	}
	
}
