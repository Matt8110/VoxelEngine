package VoxelEngine;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import Blocks.Block;
import Blocks.BlockSelection;
import Utils.Utils;
import Blocks.BlockType;
import Blocks.Chunk;
import Blocks.ChunkManager;
import Blocks.ColorBlock;
import Utils.Ray;

public class Camera {

	public static Vector3f position = new Vector3f(0, 120, 0);
	public static Vector3f rotation = new Vector3f(0, 0, 0);
	public static float speed = 0.0040f;
	public static float staticSpeed = 0.0040f;
	public static float walkSpeed = 0.0040f;
	public static float runSpeed = 0.00650f;
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
	
	private static boolean mouseRightWasDown = false;
	private static boolean mouseLeftWasDown = false;
	private static boolean leftMouse = false;
	private static boolean rightMouse = false;
	
	private static int chunkX, chunkZ;
	
	//Block placing variables
	private static int checkX, checkY, checkZ;
	private static int currentBlockToPlace = 0;
	private static int mouseWheel = 0;
	private static FloatBuffer colorValue = BufferUtils.createFloatBuffer(3);
	
	private static float boundX1, boundX2, boundY2, boundZ1, boundZ2;
	private static float boundBoxSize = 0.60f / 2.0f;
	
	private static BlockSelection blockSelection;
	private static ColorBlock cb;
	
	public static Matrix4f MVP = new Matrix4f();
	
	public static void initCamera() {
		
		blockSelection = new BlockSelection();
		cb = new ColorBlock();
		
		rotation.x = 0;
		rotation.y = 0;
		
	}
	
	public static void updateCamera(float deltaTime) { 
		
		mouseLook();
		movement(deltaTime);
		handlePlayerCollisions(deltaTime);
		
		MVP = Matrix4f.mul(Utils.getProjectionMatrix(Main.fov, Main.near, Main.far), Utils.getCullingViewMatrix(), null);
		
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
		
		if (rayVal < 1 + playerHeight - 0.04f && rayVal != -1) {
			
			if (boundY2 + 0.4f < 128) {
				if (checkCollisionAt(position.x, boundY2 + 0.2f, position.z)) {
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
		
		
		
		String chunkToCheck = ChunkManager.getChunkListLocation(chunkX * ChunkManager.chunkWidth, chunkZ * ChunkManager.chunkWidth, ChunkManager.renderChunks);
		
		if (chunkToCheck != null) {
			Chunk chunk = ChunkManager.renderChunks.get(chunkToCheck);
			
			
			if (chunk.blocks[checkX][checkY][checkZ].isActive && chunk.blocks[checkX][checkY][checkZ].blockType.collideable) {
				return true;
			}
		}
		
		return false;
		
	}
	
	private static void addAndRemoveBlocks() {
		
		Block block = null;
		
		//Cast ray for block interaction
		if (!ChunkManager.renderChunks.isEmpty())
			block = ray.castRay(position.x, position.y, position.z, rotation.x, rotation.y, 6.0f, 0.01f);
		
		//Draw the white lines around the selected block
		if (block != null) {
			Main.colorShader.useShader();
			blockSelection.render(ray.getBlockPosition().x, ray.getBlockPosition().y, ray.getBlockPosition().z);
			
			//Drawing the cube to know which side you're placing on
			Main.selectionFBO.bindFBO();
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			Main.colorShader.setTransformation(ray.getBlockPosition().x, ray.getBlockPosition().y, ray.getBlockPosition().z, 1.0f);
			cb.render();
			
			//GL11.glReadPixels(Main.selectionFBO.width/2, Main.selectionFBO.height/2, 1, 1, GL11.GL_RGB, GL11.GL_FLOAT, colorValue);
			
			Main.shader.useShader();
		}
		
		//Block selection
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
		
		//Block breaking
		if (!mouseLeftWasDown && leftMouse) {
			
			mouseLeftWasDown = true;
			breakBlock(block);
			
		}
		
		//Block placing
		if (!mouseRightWasDown && rightMouse) {
			
			mouseRightWasDown = true;
			placeBlock(block);
			
		}
		
		
		if (!leftMouse) {
			mouseLeftWasDown = false;
		}
		if (!rightMouse) {
			mouseRightWasDown = false;
		}
		
		Main.selectionFBO.unbindFBO();
		
	}
	
	private static void breakBlock(Block block) {
		
		if (block != null) {
			
			Vector3f blockPosition = ray.getBlockPosition();
			
			block.isActive = false;
			Utils.saveBlockBreak(blockPosition.x, blockPosition.y, blockPosition.z);
			Utils.rebuildChunkByBlock((int)blockPosition.x, (int)blockPosition.y, (int)blockPosition.z);
				
		}
		
	}
	
	private static void placeBlock(Block block) {
		
		if (block != null) {
			
			GL11.glReadPixels(Main.selectionFBO.width/2, Main.selectionFBO.height/2, 1, 1, GL11.GL_RGB, GL11.GL_FLOAT, colorValue);
		
			float r = colorValue.get(0);
			float g = colorValue.get(1);
			float b = colorValue.get(2);
			Vector3f blockPosition = ray.getBlockPosition();
			Vector3f blockPlacePosition = blockPosition;
			
			
			
				
			//Back z-
			if (r == 1.0f && g == 0.0f && b == 0.0f)
				blockPlacePosition.z -= 1;
			//Front z+
			if (r == 0.0f && g == 1.0f && b == 0.0f)
				blockPlacePosition.z += 1;
			//Left x-
			if (r == 0.0f && g == 0.0f && b == 1.0f) 
				blockPlacePosition.x -= 1;
			//Right x+
			if (r == 1.0f && g == 1.0f && b == 0.0f)
				blockPlacePosition.x += 1;
			//Bottom y-
			if (r == 1.0f && g == 1.0f && b == 1.0f)
				blockPlacePosition.y -= 1;
			//Bottom y+
			if (r == 1.0f && g == 0.0f && b == 1.0f)
				blockPlacePosition.y += 1;
				
			if ( !(((int)blockPlacePosition.x == (int)position.x && (int)blockPlacePosition.z == (int)position.z && (int)blockPlacePosition.y == (int)position.y - 1) ||
				   ((int)blockPlacePosition.x == (int)position.x && (int)blockPlacePosition.y == (int)position.y && (int)blockPlacePosition.z == (int)position.z))) {
				
				Utils.getBlock(blockPlacePosition.x, blockPlacePosition.y, blockPlacePosition.z).isActive = true;
				Utils.getBlock(blockPlacePosition.x, blockPlacePosition.y, blockPlacePosition.z).blockType = BlockType.getByInt(currentBlockToPlace);
				Utils.rebuildChunkByBlock(blockPlacePosition.x, blockPlacePosition.y, blockPlacePosition.z);
				
				Utils.saveBlockPlace(blockPlacePosition.x, blockPlacePosition.y, blockPlacePosition.z, currentBlockToPlace);
			
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
