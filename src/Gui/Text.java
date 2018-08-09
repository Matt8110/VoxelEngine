package Gui;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import Core.VAO;

public class Text {

	public VAO vao;
	private float x, y;
	private String text;
	private Font font;
	private float cursorPos;
	private int size;
	private float scale;
	
	public float finalWidth = 0.0f;
	
	private float padding = 10;
	
	public Text(Font font, String text, float x, float y, float scale) {
		
		size = text.length()*6;
		
		setPosition(x, y);
		setText(text);
		this.font = font;
		this.scale = scale;
		
		int size = text.length()*12;
		float vertices[] = new float[size];
		float texCoords[] = new float[size];
		
		cursorPos = x;
		
		for (int i = 0; i < text.length(); i++) {
			
			float width = font.characters.get((int)text.charAt(i)).origWidth*scale;
			float height = font.characters.get((int)text.charAt(i)).origHeight*scale;
			float advance = (font.characters.get((int)text.charAt(i)).advance-padding)*scale;
			float xOff = font.characters.get((int)text.charAt(i)).xOff*scale;
			float yOff = font.characters.get((int)text.charAt(i)).yOff*scale;
			float texX = font.characters.get((int)text.charAt(i)).texCoordX;
			float texY = font.characters.get((int)text.charAt(i)).texCoordY;
			float texW = font.characters.get((int)text.charAt(i)).width;
			float texH = font.characters.get((int)text.charAt(i)).height;
			
			float incX = 2f/Display.getWidth();
			float incY = 2f/Display.getHeight();
			
			float finalX = cursorPos;
			float finalY = (y+yOff);
			
			float fixedX = finalX*incX-1f;
			float fixedY = finalY*incY-1f;
			float fixedW = (finalX+width)*incX-1f;
			float fixedH = (finalY+height)*incY-1f;
			
			vertices[i*12] = fixedX;
			vertices[i*12+1] = -fixedY;
			vertices[i*12+2] = fixedX;
			vertices[i*12+3] = -fixedH;
			vertices[i*12+4] = fixedW;
			vertices[i*12+5] = -fixedY;
			vertices[i*12+6] = fixedW;
			vertices[i*12+7] = -fixedY;
			vertices[i*12+8] = fixedX;
			vertices[i*12+9] = -fixedH;
			vertices[i*12+10] = fixedW;
			vertices[i*12+11] = -fixedH;
			
			texCoords[i*12] = texX;
			texCoords[i*12+1] = texY;
			texCoords[i*12+2] = texX;
			texCoords[i*12+3] = texY+texH;
			texCoords[i*12+4] = texX+texW;
			texCoords[i*12+5] = texY;
			texCoords[i*12+6] = texX+texW;
			texCoords[i*12+7] = texY;
			texCoords[i*12+8] = texX;
			texCoords[i*12+9] = texY+texH;
			texCoords[i*12+10] = texX+texW;
			texCoords[i*12+11] = texY+texH;
			
			if (cursorPos > finalWidth) {
				finalWidth = cursorPos;
			}
			
			cursorPos += advance;
			
		}
		
		 vao = new VAO(vertices, texCoords);
		
		
	}
	
	public void updateText(String text) {
		
		this.text = text;
		
		int size = text.length()*12;
		float vertices[] = new float[size];
		float texCoords[] = new float[size];
		
		cursorPos = x;
		
		for (int i = 0; i < text.length(); i++) {
			
			float width = font.characters.get((int)text.charAt(i)).origWidth*scale;
			float height = font.characters.get((int)text.charAt(i)).origHeight*scale;
			float advance = (font.characters.get((int)text.charAt(i)).advance-padding)*scale;
			float xOff = font.characters.get((int)text.charAt(i)).xOff*scale;
			float yOff = font.characters.get((int)text.charAt(i)).yOff*scale;
			float texX = font.characters.get((int)text.charAt(i)).texCoordX;
			float texY = font.characters.get((int)text.charAt(i)).texCoordY;
			float texW = font.characters.get((int)text.charAt(i)).width;
			float texH = font.characters.get((int)text.charAt(i)).height;
			
			float incX = 2f/Display.getWidth();
			float incY = 2f/Display.getHeight();
			
			float finalX = cursorPos;
			float finalY = (y+yOff);
			
			float fixedX = finalX*incX-1f;
			float fixedY = finalY*incY-1f;
			float fixedW = (finalX+width)*incX-1f;
			float fixedH = (finalY+height)*incY-1f;
			
			vertices[i*12] = fixedX;
			vertices[i*12+1] = -fixedY;
			vertices[i*12+2] = fixedX;
			vertices[i*12+3] = -fixedH;
			vertices[i*12+4] = fixedW;
			vertices[i*12+5] = -fixedY;
			vertices[i*12+6] = fixedW;
			vertices[i*12+7] = -fixedY;
			vertices[i*12+8] = fixedX;
			vertices[i*12+9] = -fixedH;
			vertices[i*12+10] = fixedW;
			vertices[i*12+11] = -fixedH;
			
			texCoords[i*12] = texX;
			texCoords[i*12+1] = texY;
			texCoords[i*12+2] = texX;
			texCoords[i*12+3] = texY+texH;
			texCoords[i*12+4] = texX+texW;
			texCoords[i*12+5] = texY;
			texCoords[i*12+6] = texX+texW;
			texCoords[i*12+7] = texY;
			texCoords[i*12+8] = texX;
			texCoords[i*12+9] = texY+texH;
			texCoords[i*12+10] = texX+texW;
			texCoords[i*12+11] = texY+texH;
			
			if (cursorPos > finalWidth) {
				finalWidth = cursorPos;
				//System.out.println(finalWidth);
			}
			
			
			
			cursorPos += advance;
			
		}
		
		vao.modifyVAO(vertices, texCoords);
		
	}
	
	public void render() {
		
		//GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, font.fontTex);
		
		vao.render2D();
		
		//GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}

	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
}
