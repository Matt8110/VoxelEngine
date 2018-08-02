package VoxelEngine;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

import Blocks.BlockType;
import Blocks.ChunkManager;
import Utils.Ray;

public class Camera {

	public static Vector3f position = new Vector3f(72, 80, 72);
	public static Vector3f rotation = new Vector3f(0, 0, 0);
	public static float speed = 0.01f;
	
	private static int lastMouseX = 0, lastMouseY = 0;
	
	private static int mouseX;
	private static int mouseY;
	
	private static float lastX = position.x;
	private static float lastZ = position.y;
	
	private static Ray ray = new Ray();
	private static Vector3f rayResult;
	
	private static boolean mouseWasDown = false;
	private static boolean leftMouse = false;
	private static boolean rightMouse = false;
	
	//Block placing variables
	private static int checkX, checkY, checkZ;
	private static float checkXIf, checkYIf, checkZIf;
	private static int currentBlockToPlace = 0;
	private static int mouseWheel = 0;
	
	public static void initCamera() {
		
		rotation.x = 0;
		rotation.y = 0;
		
	}
	
	public static void updateCamera(float deltaTime) { 
		
		mouseLook();
		movement(deltaTime);
		
		addAndRemoveBlocks();
		
		lastX = position.x;
		lastZ = position.z;
		
	
		}
	
	private static void addAndRemoveBlocks() {
		
		mouseWheel = Mouse.getDWheel();
		
		if (mouseWheel > 0) {
			currentBlockToPlace += 1;
		}
		if (mouseWheel < 0) {
			currentBlockToPlace -= 1;
		}
		
		if (currentBlockToPlace >= BlockType.size()) {
			currentBlockToPlace = 0;
		}
		if (currentBlockToPlace < 0) {
			currentBlockToPlace = BlockType.size()-1;
		}
		
		leftMouse = Mouse.isButtonDown(0);
		rightMouse = Mouse.isButtonDown(1);
		
		if ((leftMouse || rightMouse) && !mouseWasDown) {
			
			mouseWasDown = true;
			
			rayResult = ray.castRayIntoBlocks(position.x, position.y, position.z, rotation.x, rotation.y, 10.0f);
				
			//Break block
			if (rayResult != null && leftMouse) {
				ray.getBlock().isActive = false;
				ChunkManager.renderChunks.get(ray.getChunk()).needsRebuilt = true;
			}
			
			//Place block
			if (rayResult != null && rightMouse) {
				placeBlock(ray.getBlockPosition(), ray.getOriginalBlockPosition(), ray.getChunk());
				ChunkManager.renderChunks.get(ray.getChunk()).needsRebuilt = true;
			}
		}
		
		if (!leftMouse && !rightMouse) {
			mouseWasDown = false;
		}
		
	}
	
	private static void placeBlock(Vector3f blockPosition, Vector3f rayPosition, int chunk) {
		
		
		checkX = (int) blockPosition.x;
		checkY = (int) blockPosition.y;
		checkZ = (int) blockPosition.z;
		
		checkXIf = rayPosition.x - blockPosition.x;
		checkYIf = rayPosition.y - blockPosition.y;
		checkZIf = rayPosition.z - blockPosition.z;
		
		//System.out.println(checkXIf + " " + checkYIf + " " + checkZIf);
		
		
		if (checkXIf > 0.95f || checkYIf > 0.95f || checkZIf > 0.95f) {
			// X+
			if (checkX+1 <16) {
				if (checkXIf > checkYIf && checkXIf > checkZIf) {
					ChunkManager.renderChunks.get(chunk).blocks[(int) (checkX+1)][(int) checkY][(int) checkZ].isActive = true;
					ChunkManager.renderChunks.get(chunk).blocks[(int) (checkX+1)][(int) checkY][(int) checkZ].blockType = BlockType.getByInt(currentBlockToPlace);
				}
			}else {
				ChunkManager.renderChunks.get(ChunkManager.getChunkListLocation(ChunkManager.renderChunks.get(chunk).position.x+ChunkManager.chunkWidth, ChunkManager.renderChunks.get(chunk).position.y, ChunkManager.renderChunks)).blocks[(int) (0)][(int) checkY][(int) checkZ].isActive = true;
				ChunkManager.renderChunks.get(ChunkManager.getChunkListLocation(ChunkManager.renderChunks.get(chunk).position.x+ChunkManager.chunkWidth, ChunkManager.renderChunks.get(chunk).position.y, ChunkManager.renderChunks)).blocks[(int) (0)][(int) checkY][(int) checkZ].blockType = BlockType.getByInt(currentBlockToPlace);
				ChunkManager.renderChunks.get(ChunkManager.getChunkListLocation(ChunkManager.renderChunks.get(chunk).position.x+ChunkManager.chunkWidth, ChunkManager.renderChunks.get(chunk).position.y, ChunkManager.renderChunks)).needsRebuilt = true;
			}
				
			// Y+
			if (checkYIf > checkXIf && checkYIf > checkZIf && checkY+1 < 128) {
				
				ChunkManager.renderChunks.get(chunk).blocks[(int) checkX][(int) (checkY+1)][(int) checkZ].isActive = true;
				ChunkManager.renderChunks.get(chunk).blocks[(int) checkX][(int) (checkY+1)][(int) checkZ].blockType = BlockType.getByInt(currentBlockToPlace);
			}
			// Z+
			
			if (checkZ+1 <16) {
				if (checkZIf > checkYIf && checkZIf > checkXIf) {
					ChunkManager.renderChunks.get(chunk).blocks[(int) checkX][(int) checkY][(int) (checkZ+1)].isActive = true;
					ChunkManager.renderChunks.get(chunk).blocks[(int) checkX][(int) checkY][(int) (checkZ+1)].blockType = BlockType.getByInt(currentBlockToPlace);
				}
			}else {
				ChunkManager.renderChunks.get(ChunkManager.getChunkListLocation(ChunkManager.renderChunks.get(chunk).position.x, ChunkManager.renderChunks.get(chunk).position.y+ChunkManager.chunkWidth, ChunkManager.renderChunks)).blocks[(int) checkX][(int) checkY][(int) 0].isActive = true;
				ChunkManager.renderChunks.get(ChunkManager.getChunkListLocation(ChunkManager.renderChunks.get(chunk).position.x, ChunkManager.renderChunks.get(chunk).position.y+ChunkManager.chunkWidth, ChunkManager.renderChunks)).blocks[(int)checkX][(int) checkY][(int) 0].blockType = BlockType.getByInt(currentBlockToPlace);
				ChunkManager.renderChunks.get(ChunkManager.getChunkListLocation(ChunkManager.renderChunks.get(chunk).position.x, ChunkManager.renderChunks.get(chunk).position.y+ChunkManager.chunkWidth, ChunkManager.renderChunks)).needsRebuilt = true;
			}
			
			}else if (checkXIf < 0.05f || checkYIf < 0.05f || checkZIf < 0.05f){
				
			// X-
			if (checkX-1 >= 0) {
				if (checkXIf < checkYIf &&  checkXIf < checkZIf) {
					ChunkManager.renderChunks.get(chunk).blocks[(int) (checkX-1)][(int) checkY][(int) checkZ].isActive = true;
					ChunkManager.renderChunks.get(chunk).blocks[(int) (checkX-1)][(int) checkY][(int) checkZ].blockType = BlockType.getByInt(currentBlockToPlace);
				}
			}else {
				ChunkManager.renderChunks.get(ChunkManager.getChunkListLocation(ChunkManager.renderChunks.get(chunk).position.x-ChunkManager.chunkWidth, ChunkManager.renderChunks.get(chunk).position.y, ChunkManager.renderChunks)).blocks[(int) (ChunkManager.chunkWidth-1)][(int) checkY][(int) checkZ].isActive = true;
				ChunkManager.renderChunks.get(ChunkManager.getChunkListLocation(ChunkManager.renderChunks.get(chunk).position.x-ChunkManager.chunkWidth, ChunkManager.renderChunks.get(chunk).position.y, ChunkManager.renderChunks)).blocks[(int) (ChunkManager.chunkWidth-1)][(int) checkY][(int) checkZ].blockType = BlockType.getByInt(currentBlockToPlace);
				ChunkManager.renderChunks.get(ChunkManager.getChunkListLocation(ChunkManager.renderChunks.get(chunk).position.x-ChunkManager.chunkWidth, ChunkManager.renderChunks.get(chunk).position.y, ChunkManager.renderChunks)).needsRebuilt = true;
			}
				
			// Y-
			if (checkYIf < checkXIf && checkYIf < checkZIf && checkY-1 > 0) {
				ChunkManager.renderChunks.get(chunk).blocks[(int) checkX][(int) (checkY-1)][(int) checkZ].isActive = true;
				ChunkManager.renderChunks.get(chunk).blocks[(int) checkX][(int) (checkY-1)][(int) checkZ].blockType = BlockType.getByInt(currentBlockToPlace);
			}
			// Z-
			if (checkZ-1 >= 0) {
				if (checkZIf < checkYIf && checkZIf < checkXIf) {
					ChunkManager.renderChunks.get(chunk).blocks[(int) checkX][(int) checkY][(int) (checkZ-1)].isActive = true;
					ChunkManager.renderChunks.get(chunk).blocks[(int) checkX][(int) checkY][(int) (checkZ-1)].blockType = BlockType.getByInt(currentBlockToPlace);
				}
			}else {
				ChunkManager.renderChunks.get(ChunkManager.getChunkListLocation(ChunkManager.renderChunks.get(chunk).position.x, ChunkManager.renderChunks.get(chunk).position.y-ChunkManager.chunkWidth, ChunkManager.renderChunks)).blocks[(int) checkX][(int) checkY][(int) (ChunkManager.chunkWidth-1)].isActive = true;
				ChunkManager.renderChunks.get(ChunkManager.getChunkListLocation(ChunkManager.renderChunks.get(chunk).position.x, ChunkManager.renderChunks.get(chunk).position.y-ChunkManager.chunkWidth, ChunkManager.renderChunks)).blocks[(int)checkX][(int) checkY][(int) (ChunkManager.chunkWidth-1)].blockType = BlockType.getByInt(currentBlockToPlace);
				ChunkManager.renderChunks.get(ChunkManager.getChunkListLocation(ChunkManager.renderChunks.get(chunk).position.x, ChunkManager.renderChunks.get(chunk).position.y-ChunkManager.chunkWidth, ChunkManager.renderChunks)).needsRebuilt = true;
			}
		}
		
	}
		
		
	
	public static void movement(float deltaTime) {
		
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			position.x += Math.cos(Math.toRadians(rotation.y-90))*speed*deltaTime;
			position.z += Math.sin(Math.toRadians(rotation.y-90))*speed*deltaTime;
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			position.x -= Math.cos(Math.toRadians(rotation.y-90))*speed*deltaTime;
			position.z -= Math.sin(Math.toRadians(rotation.y-90))*speed*deltaTime;
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			position.x += Math.cos(Math.toRadians(rotation.y-180))*speed*deltaTime;
			position.z += Math.sin(Math.toRadians(rotation.y-180))*speed*deltaTime;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			position.x += Math.cos(Math.toRadians(rotation.y))*speed*deltaTime;
			position.z += Math.sin(Math.toRadians(rotation.y))*speed*deltaTime;
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			position.y += speed*deltaTime;
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			position.y -= speed*deltaTime;
		}
		
	}
	
	public static void mouseLook() {
		
		mouseX = Mouse.getX();
		mouseY = Mouse.getY();
		
		rotation.y += (Mouse.getX() - lastMouseX) * Main.mouseSensitivity;
		rotation.x -= (Mouse.getY() - lastMouseY) * Main.mouseSensitivity;
		
		
		if (mouseX > Display.getWidth()/2+64)
			Mouse.setCursorPosition(Display.getWidth()/2-64, mouseY);
		if (mouseX < Display.getWidth()/2-64)
			Mouse.setCursorPosition(Display.getWidth()/2+64, mouseY);
		if (mouseY > Display.getHeight()/2+64)
			Mouse.setCursorPosition(mouseX, Display.getHeight()/2-64);
		if (mouseY < Display.getHeight()/2-64)
			Mouse.setCursorPosition(mouseX, Display.getHeight()/2+64);
		
		lastMouseX = Mouse.getX();
		lastMouseY = Mouse.getY();
		
		if (rotation.y > 359)
			rotation.y = 0;
		if (rotation.y < 0)
			rotation.y = 359;
		
		if (rotation.x < -89)
			rotation.x = -89;
		if (rotation.x > 89)
			rotation.x = 89;
		
		
	}
	
}
