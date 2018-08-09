package Gui;

public class FontChar {

	public float advance;
	public float texCoordX;
	public float texCoordY;
	public float width;
	public float height;
	public float xOff;
	public float yOff;
	public float origWidth;
	public float origHeight;
	
	public FontChar(float advance, float texCoordX, float texCoordY, float width, float height, float xOff, float yOff, 
			float origWidth, float origHeight) {
		
		this.advance = advance;
		this.texCoordX = texCoordX;
		this.texCoordY = texCoordY;
		this.width = width;
		this.height = height;
		this.xOff = xOff;
		this.yOff = yOff;
		this.origWidth = origWidth;
		this.origHeight = origHeight;
		
	}
	
}
