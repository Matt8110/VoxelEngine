package Gui;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import Core.VAO;
import Utils.Utils;

public class Texture2D {

	public Vector2f position, scale;
	private int texture;
	private VAO vao;
	
	public Texture2D(String textureFile, Vector2f position, Vector2f scale) {
		
		this.position = position;
		this.scale = scale;
		
		init(textureFile);
		
	}
	
	/*public Texture2D(String textureFile) {
		
		position = new Vector2f();
		scale = new Vector2f();
		init(textureFile);
		
	}*/
	
	public void render() {
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		
		GL11.glDisable(GL11.GL_CULL_FACE);
		
		vao.render2D();
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		
	}
	
	private void init(String textureFile) {
		
		texture = Utils.loadTexture(textureFile);
		
		float scalerX = (2.0f / Display.getWidth());
		float scalerY = (2.0f / Display.getHeight());
		
		float scaledX = scalerX * position.x - 1.0f;
		float scaledY = scalerY * position.y - 1.0f;
		float scaledW = (scalerX * scale.x) + scaledX;
		float scaledH = (scalerY * scale.y) + scaledY;
		
		float[] vertices = {
				scaledX, scaledY,
				scaledX, scaledH,
				scaledW, scaledY,
				
				scaledW, scaledY,
				scaledX, scaledH,
				scaledW, scaledH,
				
		};
		
		float[] texCoords = {
				0, 0,
				0, 1,
				1, 0,
				
				1, 0,
				0, 1,
				1, 1
		};
		
		vao = new VAO(vertices, texCoords);
		
	}
	
}
