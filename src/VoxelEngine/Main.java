package VoxelEngine;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import Blocks.ChunkManager;
import Core.Shader;
import Gui.Texture2D;

public class Main {

	public static MainShader shader;
	public static Shader shader2D;
	
	public static int fov = 70;
	public static float mouseSensitivity = 0.1f;
	
	public static long lastTime = 0;
	public static float deltaTime = 0;
	public static boolean isRunning = true;
	
	public static boolean fullscreen = true;
	
	public static void main(String args[]) {
		
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		createWindow();
		
		@SuppressWarnings("unused")
		int startMemory = GL11.glGetInteger(0x9049);
		
		Mouse.setGrabbed(true);
		
		shader = new MainShader("shaders/vertex.vert", "shaders/fragment.frag");
		shader2D = new Shader("shaders/2d.vert", "shaders/2d.frag");
		
		Texture2D crosshair = new Texture2D("crosshair.png", new Vector2f(Display.getWidth()/2-16, Display.getHeight()/2-16), new Vector2f(32, 32));
		
		
		Camera.initCamera();
		
		ChunkManager.initChunks();
		
		
		
		lastTime = System.currentTimeMillis();
		
		while (!Display.isCloseRequested()) {
			
			deltaTime = System.currentTimeMillis() - lastTime;
			lastTime = System.currentTimeMillis();
			
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glClearColor(0.4f, 0.4f, 1.0f, 1.0f);
			
			shader.useShader();
			
			//System.out.println((startMemory - GL11.glGetInteger(0x9049))/1000);
			
			ChunkManager.renderChunks();
			
			Camera.updateCamera(deltaTime);
			
			//Drawing 2D stuff
			shader2D.useShader();
			crosshair.render();
			
			//Draw loading screen to avoid seeing chunks loading
			if (!ChunkManager.hasLoaded && !ChunkManager.buildChunks.isEmpty()) {
				ChunkManager.background.render();
				//ChunkManager.generating.render();
			}else {
				ChunkManager.hasLoaded = true;
			}
			
			
			Display.update();
			//Display.sync(60);
			
		}
		
		Display.destroy();
		isRunning = false;
		
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
		
	}

	
}
