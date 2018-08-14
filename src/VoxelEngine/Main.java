package VoxelEngine;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import Blocks.Chunk;
import Blocks.ChunkManager;
import Blocks.ColorBlock;
import Core.Shader;
import Gui.Font;
import Gui.Text;
import Gui.Texture2D;
import Utils.*;

public class Main {

	public static MainShader shader;
	public static ColorShader colorShader;
	public static Shader shader2D, textShader;
	
	public static int fov = 90;
	public static float near = 0.01f;
	public static float far = 1000.0f;
	public static float mouseSensitivity = 0.075f;
	
	public static long lastTime = 0, fpsLastTime = 0, fps = 0, lastTickTime;
	public static float deltaTime = 0;
	public static boolean isRunning = true;
	
	public static boolean fullScreenButtonsWereDown = false;
	public static boolean fullscreen = true;
	
	public static Font font;
	public static Text text, memUse, renChunks;
	
	public static FBO selectionFBO;
	
	public static int renderCalls = 0;
	
	public static State state = State.GAME;
	
	public static void main(String args[]) {
		
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		createWindow();
		
		@SuppressWarnings("unused")
		int startMemory = GL11.glGetInteger(0x9049);
		
		selectionFBO = new FBO(512, 512);
		selectionFBO.addDepthAttachment();
		selectionFBO.addTextureAttachment();
		
		Mouse.setGrabbed(true);
		
		shader = new MainShader("shaders/vertex.vert", "shaders/fragment.frag");
		shader2D = new Shader("shaders/2d.vert", "shaders/2d.frag");
		textShader = new Shader("shaders/text.vert", "shaders/text.frag");
		colorShader = new ColorShader("shaders/vertexColor.vert", "shaders/fragmentColor.frag");
		
		font = new Font("Fonts/candara.fnt");
		
		Texture2D crosshair = new Texture2D("crosshair.png", new Vector2f(Display.getWidth()/2-16, Display.getHeight()/2-16), new Vector2f(32, 32));
		
		
		Camera.initCamera();
		
		Utils.loadPlayerData();
		
		ChunkManager.initChunks();
		
		text = new Text(font, "                       ", 16, 16, 0.5f);
		memUse = new Text(font, "                       ", 16, 64, 0.5f);
		renChunks = new Text(font, "                       ", 16, 96, 0.5f);
		
		lastTime = System.currentTimeMillis();
		fpsLastTime = System.currentTimeMillis();
		
		while (!Display.isCloseRequested()) {
			
			deltaTime = System.currentTimeMillis() - lastTime;
			lastTime = System.currentTimeMillis();
			
			if (Keyboard.isKeyDown(Keyboard.KEY_RMENU) && Keyboard.isKeyDown(Keyboard.KEY_RETURN)) {
				
				if (!fullScreenButtonsWereDown) {
					fullScreenButtonsWereDown = true;
					switchFullscreen();
				}
				
			}else {
				fullScreenButtonsWereDown = false;
			}
			
			if (System.currentTimeMillis() - fpsLastTime > 1000) {
				fpsLastTime = System.currentTimeMillis();
				memUse.updateText("FPS: " + String.valueOf(fps));
				fps = 0;
			}
			
			fps++;
			
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			
			if (state == State.MAIN_MENU) {
				
				shader2D.useShader();
				
				
				
			}
			
			if (state == State.GAME) {
				
				GL11.glClearColor(0.54f, 0.70f, 1.0f, 1.0f);
			
				shader.useShader();
				
				//System.out.println((startMemory - GL11.glGetInteger(0x9049))/1000);
				
				//memUse.updateText(String.valueOf((startMemory - GL11.glGetInteger(0x9049))/1000));
				//memUse.updateText(String.valueOf(ChunkManager.renderChunks.size()));
				
				
				
				
				shader.setTransformation(0, 0, 0, 1.0f);
				ChunkManager.renderChunks();
				
				renChunks.updateText("Rendering " + renderCalls + "/" + ChunkManager.renderChunks.size() + " Chunks");
				//renChunks.updateText("X: " + (int)Camera.position.x + " Y: " + (int)Camera.position.y + " Z: " + (int)Camera.position.z);	
				
				Camera.updateCamera(deltaTime);
				
				//Drawing 2D stuff
				shader2D.useShader();
				crosshair.render();
				
				//Draw loading screen to avoid seeing chunks loading
				if (!ChunkManager.hasLoaded && !ChunkManager.buildChunks.isEmpty()) {
					//ChunkManager.background.render();
					//ChunkManager.generating.render();
				}else {
					ChunkManager.hasLoaded = true;
				}
				
				//Drawing text
				textShader.useShader();
				text.render();
				memUse.render();
				renChunks.render();
				
				//memUse.updateText(String.valueOf(renderCalls));
				renderCalls = 0;
			
			}
			
			Display.update();
			//Display.sync(60);
			
		}
		
		Display.destroy();
		isRunning = false;
		
		for (Chunk chunk : ChunkManager.renderChunks.values()) {
			Utils.saveChangesToFile(chunk);
			
		}
		
		Utils.savePlayerDataToFile();
		
		}
	
	public static void switchFullscreen() {
		
		fullscreen = !fullscreen;
		
		try {
			
			if (fullscreen) {
				Display.setDisplayMode(Display.getDesktopDisplayMode());
				
			}else {
				Display.setDisplayMode(new DisplayMode(1024, 768));
			}
			
			GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
			
			shader.useShader();
			shader.resetProjectionMatrix();
			
			Display.setFullscreen(fullscreen);
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void createWindow() {
		
		try {
			
			if (fullscreen) {
				Display.setDisplayMode(Display.getDesktopDisplayMode());
			}else {
				Display.setDisplayMode(new DisplayMode(1024, 768));
			}
			
			Display.setFullscreen(fullscreen);
			
			Display.create();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable (GL11.GL_BLEND); 
		GL11.glBlendFunc (GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		//GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
		//GL11.glEnable(GL30.GL_CLIP_DISTANCE1);
		
		//GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		
	}
	
}
