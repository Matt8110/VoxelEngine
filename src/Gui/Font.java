package Gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

import Utils.Utils;

public class Font {

	public int fontTex;
	public HashMap<Integer, FontChar> characters = new HashMap<Integer, FontChar>();
	public float imageWidth, imageHeight;
	
	public Font(String font) {
		
		loadFontCharacters(font);
		
		
	}
	
	private void loadFontCharacters(String font) {
		
		try {
			
			Scanner scan = new Scanner(new File(font));
			
			while (scan.hasNextLine()) {
				
				String line = scan.nextLine();
				line = line.replaceAll("=", " ");
				line = line.replaceAll("\"", "");
				String[] lineSplit = line.split(" ");
				
				int id = 0;
				float texX = 0;
				float texY = 0;
				float width = 0;
				float height = 0;
				float advance = 0;
				float xOff = 0;
				float yOff = 0;
				float origWidth = 0;
				float origHeight = 0;
				
				for (int i = 0; i < lineSplit.length; i++) {
					
					if (lineSplit[i].equalsIgnoreCase("file")) {
						fontTex = Utils.loadTexture("fonts/" + lineSplit[i+1]);
					}
					
					if (lineSplit[i].equalsIgnoreCase("scaleW")) {
						imageWidth = Float.parseFloat(lineSplit[i+1]); 
					}
					if (lineSplit[i].equalsIgnoreCase("scaleH")) {
						imageHeight = Float.parseFloat(lineSplit[i+1]); 
					}
					
					
					if (lineSplit[i].equalsIgnoreCase("id")) {
						id = Integer.parseInt(lineSplit[i+1]);
					}
					if (lineSplit[i].equalsIgnoreCase("x")) {
						texX = (1.0f/imageWidth) * Float.parseFloat(lineSplit[i+1]);
					}
					if (lineSplit[i].equalsIgnoreCase("y")) {
						texY = ((1.0f/imageHeight) * Float.parseFloat(lineSplit[i+1]));
					}
					if (lineSplit[i].equalsIgnoreCase("width")) {
						origWidth = Float.parseFloat(lineSplit[i+1]);
						width = (1.0f/imageWidth) * origWidth;
						//System.out.println(width);
					}
					if (lineSplit[i].equalsIgnoreCase("height")) {
						origHeight = Float.parseFloat(lineSplit[i+1]);
						height = (1.0f/imageHeight) * origHeight;
					}
					if (lineSplit[i].equalsIgnoreCase("xadvance")) {
						advance = Float.parseFloat(lineSplit[i+1]);
					}
					if (lineSplit[i].equalsIgnoreCase("xoffset")) {
						xOff = Float.parseFloat(lineSplit[i+1]);
					}
					if (lineSplit[i].equalsIgnoreCase("yoffset")) {
						yOff = Float.parseFloat(lineSplit[i+1]);
					}
					
					
					if (lineSplit[i].equalsIgnoreCase("chnl")) {
					characters.put(id, new FontChar(advance, texX, texY, width, height, xOff, yOff, origWidth, origHeight));
					}
					
					 
				}
				
				
				
			}
			
			scan.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
}
