package VoxelEngine;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import Blocks.BlockType;
import Blocks.Chunk;
import Blocks.ChunkManager;
import Utils.Ray;

public class Camera {

	public static Vector3f position = new Vector3f(0, 120, 0);
	public static Vector3f rotation = new Vector3f(0, 0, 0);
	public static float speed = 0.0040f;
	public static float staticSpeed = 0.0040f;
	public static float walkSpeed = 0.0040f;
	public static float runSpeed = 0.0060f;
	public static float playerHeight = 1.75f;
	public static float vSpeed = 0;
	public static float gravity = 0.000050f;
	public static float maxFallSpeed = -0.1f;
	public static boolean canJump = true;
	public static boolean letGoOfSpace = true;
	public static float rotSum = 0;
	public static float slideSpeed = 0.0000005f;
	
	private static int lastMouseX = 0, lastMouseY = 0;
	
	private static int mouseX;
	private static int mouseY;
	
	private static float lastX = position.x;
	private static float lastY = position.y;
	private static float lastZ = position.z;
	
	private static float lastXInc = 0;
	private static float lastZInc = 0;
	
	private static Ray ray = new Ray();
	private static Vector3f rayResult;
	
	private static boolean mouseRightWasDown = false;
	private static boolean mouseLeftWasDown = false;
	private static boolean leftMouse = false;
	private static boolean rightMouse = false;
	
	private static int chunkX, chunkZ;
	
	//Block placing variables
	private static int checkX, checkY, checkZ;
	private static float checkXIf, checkYIf, checkZIf;
	private static int currentBlockToPlace = 0;
	private static int mouseWheel = 0;
	
	private static float boundX1, boundX2, boundY2, boundZ1, boundZ2;
	private static float boundBoxSize = 0.60f / 2.0f;
	
	public static Matrix4f MVP = new Matrix4f();
	
	public static void initCamera() {
		
		rotation.x = 0;
		rotation.y = 0;
		
	}
	
	public static void updateCamera(float deltaTime) { 
		
		mouseLook();
		movement(deltaTime);
		handlePlayerCollisions(deltaTime);
		
		MVP = Matrix4f.mul(Utils.Utils.getProjectionMatrix(Main.fov, Main.near, Main.far), Utils.Utils.getCullingViewMatrix(), null);
		
		addAndRemoveBlocks();
		
		Main.text.updateText("Block: " + BlockType.getByInt(currentBlockToPlace).toString());
		
		lastX = position.x;
		lastY = position.y;
		lastZ = position.z;
	
		}
	
	private static void handlePlayerCollisions(float deltaTime) {
		
		boundX1 = position.x - boundBoxSize;
		boundX2 = position.x + boundBoxSize;
		boundY2 = position.y + playerHeight;
		boundZ1 = position.z - boundBoxSize;
		boundZ2 = position.z + boundBoxSize;
		
		if (lastXInc == 0)
			lastXInc = 0.0015f * deltaTime;
		if (lastZInc == 0)
			lastZInc = 0.0015f * deltaTime;
		
		if (checkCollisionAt(boundX1, position.y, position.z)){
			
			position.x += lastXInc;
			
		}
		else if (checkCollisionAt(boundX1, position.y + playerHeight-0.2f, position.z)){
			
			position.x += lastXInc;
			
		}
		
		
		if (checkCollisionAt(boundX2, position.y, position.z)){
			
			position.x -= lastXInc;
			
		}
		else if (checkCollisionAt(boundX2, position.y + playerHeight-0.2f, position.z)){
			
			position.x -= lastXInc;
			
		}
		if (checkCollisionAt(position.x, position.y, boundZ1)){
			
			position.z += lastZInc;
			
		}
		else if (checkCollisionAt(position.x, position.y + playerHeight-0.2f, boundZ1)){
			
			position.z += lastZInc;
			
		}
		
		if (checkCollisionAt(position.x, position.y, boundZ2)){
			
			position.z -= lastZInc;
			
		}
		else if (checkCollisionAt(position.x, position.y + playerHeight-0.2f, boundZ2)){
			
			position.z -= lastZInc;
			
		}
		
		
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			
			if (canJump && letGoOfSpace) {
				canJump = false;
				position.y += 0.2f;
				vSpeed = 0.01f;
			}
			
			letGoOfSpace = false;
		}else {
			letGoOfSpace = true;
		}
		
		float rayValue = ray.castHeightRay(position.x, position.y, position.z, 90, 0, 10.0f);
		
		if (rayValue > 1 + playerHeight) {
			
			if (ray.getBlock().blockType == BlockType.ICE)
				slideSpeed = 0.000003f;
			else
				slideSpeed = 0.00005f;
			
			position.y += vSpeed * deltaTime;
			
			if (vSpeed > maxFallSpeed)
				vSpeed -= gravity * deltaTime;
			
			
		}
		else if (rayValue != -1){
			
			if (ray.getBlock().blockType == BlockType.ICE)
				slideSpeed = 0.000003f;
			else
				slideSpeed = 0.00005f;
			
			canJump = true;
			vSpeed = 0;
		}
		
		float rayVal = ray.castHeightRay(position.x, position.y, position.z, 90, 0, 10.0f);
		
		if (rayVal < 1 + playerHeight - 0.02f && rayVal != -1) {
			
			if (boundY2 + 0.1f < 128) {
				if (checkCollisionAt(position.x, boundY2 + 0.1f, position.z)) {
					position.y = lastY;
					vSpeed = 0;
				}else {
					position.y += 0.01f * deltaTime;
				}
			}
			
			
		}
		
	}
	
	private static boolean checkCollisionAt(float x, float y, float z) {
		
		if (y >= 128) {
			y = 127;
		}
		
		chunkX = (int) Math.floor(x / ChunkManager.chunkWidth);
		chunkZ = (int) Math.floor(z / ChunkManager.chunkWidth);
		
		checkX = (int) (x - (chunkX * ChunkManager.chunkWidth));
		checkY = (int) (y - 1.60f);
		checkZ = (int) (z - (chunkZ * ChunkManager.chunkWidth));
		
		
		
		int chunkToCheck = ChunkManager.getChunkListLocation(chunkX * ChunkManager.chunkWidth, chunkZ * ChunkManager.chunkWidth, ChunkManager.renderChunks);
		
		if (chunkToCheck != -1) {
			Chunk chunk = ChunkManager.renderChunks.get(chunkToCheck);
			
			
			if (chunk.blocks[checkX][checkY][checkZ].isActive) {
				return true;
			}
		}
		
		return false;
		
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
		
		if (!mouseLeftWasDown && leftMouse) {
			
			mouseLeftWasDown = true;
			
			rayResult = ray.castRayIntoBlocks(position.x, position.y, position.z, rotation.x, rotation.y, 10.0f);
				
			//Break block
			if (rayResult != null) {
				ray.getBlock().isActive = false;
				ChunkManager.renderChunks.get(ray.getChunk()).needsRebuilt = true;
				
				//if (ChunkManager.renderChunks.get(ray.getChunk()).changes.containsKey((int)ray.getBlockPosition().x + "" + (int)ray.getBlockPosition().y + "" + (int)ray.getBlockPosition().z))
					//ChunkManager.renderChunks.get(ray.getChunk()).changes.remove((int)ray.getBlockPosition().x + "" + (int)ray.getBlockPosition().y + "" + (int)ray.getBlockPosition().z);
				//else
					ChunkManager.renderChunks.get(ray.getChunk()).changes.put((int)ray.getBlockPosition().x + "" + (int)ray.getBlockPosition().y + "" + (int)ray.getBlockPosition().z,"0 " + (int)ray.getBlockPosition().x + " " + (int)ray.getBlockPosition().y + " " + (int)ray.getBlockPosition().z + " ");
				
			}
			
			
		}
		
		if (!mouseRightWasDown && rightMouse) {
			
			mouseRightWasDown = true;
			
			rayResult = ray.castRayIntoBlocks(position.x, position.y, position.z, rotation.x, rotation.y, 10.0f);
				
		
			//Place block
			if (rayResult != null) {
				placeBlock(ray.getBlockPosition(), ray.getOriginalBlockPosition(), ray.getChunk());
				ChunkManager.renderChunks.get(ray.getChunk()).needsRebuilt = true;
			}
		}
		
		
		if (!leftMouse) {
			mouseLeftWasDown = false;
		}
		if (!rightMouse) {
			mouseRightWasDown = false;
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
		
		if (!ChunkManager.renderChunks.get(chunk).blocks[checkX][checkY][checkZ].blockType.canUse)
			if (checkXIf > 0.95f || checkYIf > 0.95f || checkZIf > 0.95f) {
				
			// X+
			if (checkX+1 <16) {
				if (checkXIf > checkYIf && checkXIf > checkZIf) {
					
					int blockID = currentBlockToPlace;
					ChunkManager.renderChunks.get(chunk).changes.put((int) (checkX+1) + "" + (int) (checkY) + "" + (int) (checkZ), "1 " + blockID + " " + (int) (checkX+1) + " " + (int) checkY + " " + (int) checkZ + " ");
					
					ChunkManager.renderChunks.get(chunk).blocks[(int) (checkX+1)][(int) checkY][(int) checkZ].isActive = true;
					ChunkManager.renderChunks.get(chunk).blocks[(int) (checkX+1)][(int) checkY][(int) checkZ].blockType = BlockType.getByInt(currentBlockToPlace);
				}
			}else if (checkX+1 == 16 && checkXIf > 0.95f){
				
				Chunk tempChunk = ChunkManager.renderChunks.get(ChunkManager.getChunkListLocation(ChunkManager.renderChunks.get(chunk).position.x+ChunkManager.chunkWidth, ChunkManager.renderChunks.get(chunk).position.y, ChunkManager.renderChunks));
				
				int blockID = currentBlockToPlace;
				tempChunk.changes.put((int) (0) + "" + (int) (checkY) + "" + (int) (checkZ),"1 " + blockID + " " + (int) (0) + " " + (int) checkY + " " + (int) checkZ + " ");
				
				tempChunk.blocks[(int) (0)][(int) checkY][(int) checkZ].isActive = true;
				tempChunk.blocks[(int) (0)][(int) checkY][(int) checkZ].blockType = BlockType.getByInt(currentBlockToPlace);
				tempChunk.needsRebuilt = true;
			}
				
			// Y+
			if (checkYIf > checkXIf && checkYIf > checkZIf && checkY+1 < 120) {
				
				int blockID = currentBlockToPlace;
				Chunk tempChunk = ChunkManager.renderChunks.get(chunk);
				tempChunk.changes.put((int) (checkX) + "" + (int) (checkY+1) + "" + (int) (checkZ),"1 " + blockID + " " + (int) (checkX) + " " + (int) (checkY+1) + " " + (int) checkZ + " ");
				
				if (checkX+tempChunk.position.x == (int)position.x && checkY+1 == (int)position.y - 1 && checkZ+tempChunk.position.y == (int)position.z) {}
				else {
					ChunkManager.renderChunks.get(chunk).blocks[(int) checkX][(int) (checkY+1)][(int) checkZ].isActive = true;
					ChunkManager.renderChunks.get(chunk).blocks[(int) checkX][(int) (checkY+1)][(int) checkZ].blockType = BlockType.getByInt(currentBlockToPlace);
				}
				
			}
			// Z+
			if (checkZ+1 <16) {
				if (checkZIf > checkYIf && checkZIf > checkXIf) {
					
					int blockID = currentBlockToPlace;
					ChunkManager.renderChunks.get(chunk).changes.put((int) (checkX) + "" + (int) (checkY) + "" + (int) (checkZ+1), "1 " + blockID + " " + (int) (checkX) + " " + (int) checkY + " " + (int) (checkZ+1) + " ");
					
					ChunkManager.renderChunks.get(chunk).blocks[(int) checkX][(int) checkY][(int) (checkZ+1)].isActive = true;
					ChunkManager.renderChunks.get(chunk).blocks[(int) checkX][(int) checkY][(int) (checkZ+1)].blockType = BlockType.getByInt(currentBlockToPlace);
				}
			}else if (checkZ+1 == 16 && checkZIf > 0.95f){
				
				Chunk tempChunk = ChunkManager.renderChunks.get(ChunkManager.getChunkListLocation(ChunkManager.renderChunks.get(chunk).position.x, ChunkManager.renderChunks.get(chunk).position.y+ChunkManager.chunkWidth, ChunkManager.renderChunks));
				
				int blockID = currentBlockToPlace;
				tempChunk.changes.put((int) (checkX) + "" + (int) (checkY) + "" + (int) (0), "1 " + blockID + " " + (int) checkX + " " + (int) checkY + " " + (int) 0 + " ");
				
				tempChunk.blocks[(int) checkX][(int) checkY][(int) 0].isActive = true;
				tempChunk.blocks[(int)checkX][(int) checkY][(int) 0].blockType = BlockType.getByInt(currentBlockToPlace);
				tempChunk.needsRebuilt = true;
				
			}
			
			}else if (checkXIf < 0.05f || checkYIf < 0.05f || checkZIf < 0.05f){
				
			// X-
			if (checkX-1 >= 0) {
				if (checkXIf < checkYIf &&  checkXIf < checkZIf) {
					
					int blockID = currentBlockToPlace;
					ChunkManager.renderChunks.get(chunk).changes.put((int) (checkX-1) + "" + (int) (checkY) + "" + (int) (checkZ), "1 " + blockID + " " + (int) (checkX-1) + " " + (int) checkY + " " + (int) checkZ + " ");
					
					ChunkManager.renderChunks.get(chunk).blocks[(int) (checkX-1)][(int) checkY][(int) checkZ].isActive = true;
					ChunkManager.renderChunks.get(chunk).blocks[(int) (checkX-1)][(int) checkY][(int) checkZ].blockType = BlockType.getByInt(currentBlockToPlace);
				}
			}else if (checkX-1 < 0 && checkXIf < 0.05f){
				
				Chunk tempChunk = ChunkManager.renderChunks.get(ChunkManager.getChunkListLocation(ChunkManager.renderChunks.get(chunk).position.x-ChunkManager.chunkWidth, ChunkManager.renderChunks.get(chunk).position.y, ChunkManager.renderChunks));
				
				int blockID = currentBlockToPlace;
				tempChunk.changes.put((int) (ChunkManager.chunkWidth-1) + "" + (int) (checkY) + "" + (int) (checkZ), "1 " + blockID + " " + (int) (ChunkManager.chunkWidth-1) + " " + (int) checkY + " " + (int) checkZ + " ");
				
				tempChunk.blocks[(int) (ChunkManager.chunkWidth-1)][(int) checkY][(int) checkZ].isActive = true;
				tempChunk.blocks[(int) (ChunkManager.chunkWidth-1)][(int) checkY][(int) checkZ].blockType = BlockType.getByInt(currentBlockToPlace);
				tempChunk.needsRebuilt = true;
			}
				
			// Y-
			if (checkYIf < checkXIf && checkYIf < checkZIf && checkY-1 > 0) {
				
				int blockID = currentBlockToPlace;
				ChunkManager.renderChunks.get(chunk).changes.put((int) (checkX) + "" + (int) (checkY-1) + "" + (int) (checkZ), "1 " + blockID + " " + (int) (checkX) + " " + (int) (checkY-1) + " " + (int) checkZ + " ");
				
				ChunkManager.renderChunks.get(chunk).blocks[(int) checkX][(int) (checkY-1)][(int) checkZ].isActive = true;
				ChunkManager.renderChunks.get(chunk).blocks[(int) checkX][(int) (checkY-1)][(int) checkZ].blockType = BlockType.getByInt(currentBlockToPlace);
			}
			// Z-
			if (checkZ-1 >= 0) {
				if (checkZIf < checkYIf && checkZIf < checkXIf) {
					
					int blockID = currentBlockToPlace;
					ChunkManager.renderChunks.get(chunk).changes.put((int) (checkX) + "" + (int) (checkY) + "" + (int) (checkZ-1), "1 " + blockID + " " + (int) (checkX) + " " + (int) checkY + " " + (int) (checkZ-1) + " ");
					
					ChunkManager.renderChunks.get(chunk).blocks[(int) checkX][(int) checkY][(int) (checkZ-1)].isActive = true;
					ChunkManager.renderChunks.get(chunk).blocks[(int) checkX][(int) checkY][(int) (checkZ-1)].blockType = BlockType.getByInt(currentBlockToPlace);
				}
			}else if (checkZ-1 < 0 && checkZIf < 0.05f){
				
				Chunk tempChunk = ChunkManager.renderChunks.get(ChunkManager.getChunkListLocation(ChunkManager.renderChunks.get(chunk).position.x, ChunkManager.renderChunks.get(chunk).position.y-ChunkManager.chunkWidth, ChunkManager.renderChunks));
				
				int blockID = currentBlockToPlace;
				tempChunk.changes.put((int) (checkX) + "" + (int) (checkY) + "" + (int) (ChunkManager.chunkWidth-1), "1 " + blockID + " " + (int) (checkX) + " " + (int) checkY + " " + (int) (ChunkManager.chunkWidth-1) + " ");
				
				tempChunk.blocks[(int) checkX][(int) checkY][(int) (ChunkManager.chunkWidth-1)].isActive = true;
				tempChunk.blocks[(int)checkX][(int) checkY][(int) (ChunkManager.chunkWidth-1)].blockType = BlockType.getByInt(currentBlockToPlace);
				tempChunk.needsRebuilt = true;
			}
		}
		
	}
		
		
	
	public static void movement(float deltaTime) {
		
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
			staticSpeed = runSpeed;
		else
			staticSpeed = walkSpeed;
		
		
		if (Keyboard.isKeyDown(Keyboard.KEY_W) && Keyboard.isKeyDown(Keyboard.KEY_A)) {
			if (speed < staticSpeed) {
				speed += slideSpeed * deltaTime;
			}else
				speed = staticSpeed;
			rotSum = rotation.y-180+45;
		}else if (Keyboard.isKeyDown(Keyboard.KEY_W) && Keyboard.isKeyDown(Keyboard.KEY_D)) {
			if (speed < staticSpeed) {
				speed += slideSpeed * deltaTime;
			}else
				speed = staticSpeed;
			rotSum = rotation.y-45;
		}else if (Keyboard.isKeyDown(Keyboard.KEY_S) && Keyboard.isKeyDown(Keyboard.KEY_A)) {
			if (speed < staticSpeed) {
				speed += slideSpeed * deltaTime;
			}else
				speed = staticSpeed;
			rotSum = rotation.y-270+45;
		}else if (Keyboard.isKeyDown(Keyboard.KEY_S) && Keyboard.isKeyDown(Keyboard.KEY_D)) {
			if (speed < staticSpeed) {
				speed += slideSpeed * deltaTime;
			}else
				speed = staticSpeed;
			rotSum = rotation.y-270-45;
		}
		
		else if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			if (speed < staticSpeed) {
				speed += slideSpeed * deltaTime;
			}else
				speed = staticSpeed;
			rotSum = rotation.y-90;
		}
		
		else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			if (speed < staticSpeed) {
				speed += slideSpeed * deltaTime;
			}else
				speed = staticSpeed;
			rotSum = rotation.y-270;
		}
		
		else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			if (speed < staticSpeed) {
				speed += slideSpeed * deltaTime;
			}else
				speed = staticSpeed;
			rotSum = rotation.y-180;
		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			if (speed < staticSpeed) {
				speed += slideSpeed * deltaTime;
			}else
				speed = staticSpeed;
			rotSum = rotation.y;
		}
		else {
			if (speed > 0)
				speed -= slideSpeed * deltaTime;
			else
				speed = 0;
		}
		
			position.x += Math.cos(Math.toRadians(rotSum))*speed*deltaTime;
			position.z += Math.sin(Math.toRadians(rotSum))*speed*deltaTime;
		
		
		lastXInc = Math.abs(position.x - lastX);
		lastZInc = Math.abs(position.z - lastZ);
		
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
