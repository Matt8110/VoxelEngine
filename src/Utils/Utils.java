package Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import Blocks.Block;
import Blocks.Chunk;
import Blocks.ChunkManager;
import VoxelEngine.Camera;

public class Utils {

	private static Matrix4f mat;
	
	public static Random ran = new Random();
	private static float[] tempArray;
	private static int randomNumber;
	
	private static Formatter format;
	
	public static FloatBuffer asFloatBuffer(float[] data) {
		
		FloatBuffer buf = BufferUtils.createFloatBuffer(data.length);
		buf.put(data);
		buf.flip();
		
		return buf;
		
	}
	
	public static void loadPlayerData() {
		
		try {
			
			Scanner scan = new Scanner(new File("playerdata.dat"));
			
			//Camera.position.x = scan.nextFloat()-3;
			//Camera.position.y = scan.nextFloat();
			//Camera.position.z = scan.nextFloat();
			
			scan.close();
			
		}catch(Exception e) {
			System.err.println("ERROR: Player data missing or corrupt");
		}
		
	}
	
	public static Block getBlock(float x, float y, float z) {
		
		int chunkX = (int)Math.floor(x / ChunkManager.chunkWidth);
		int chunkZ = (int)Math.floor(z / ChunkManager.chunkWidth);
		int blockX = (int)Math.floor(x - chunkX * ChunkManager.chunkWidth);
		int blockY = (int)y;
		int blockZ = (int)Math.floor(z - chunkZ * ChunkManager.chunkWidth);
		
		Chunk chunk = getChunkAtRenderedOnly(chunkX * ChunkManager.chunkWidth, chunkZ * ChunkManager.chunkWidth);
		
		if (chunk != null &&
				blockX >= 0 && blockX <= 15 &&
				blockY >= 0 && blockY <= 127 &&
				blockZ >= 0 && blockZ <= 15)
			return chunk.blocks[blockX][blockY][blockZ];
		else
			return null;
		
	}
	
	public static void rebuildChunkByBlock(float x, float y, float z) {
		
		int chunkX = (int)Math.floor(x / ChunkManager.chunkWidth) * ChunkManager.chunkWidth;
		int chunkZ = (int)Math.floor(z / ChunkManager.chunkWidth) * ChunkManager.chunkWidth;
		int blockX = (int) (x - chunkX);
		int blockZ = (int) (z - chunkZ);
		Chunk chunk = null;
		
		if (blockX == 15) {
			chunk = getChunkAtRenderedOnly(chunkX + ChunkManager.chunkWidth, chunkZ);
			chunk.needsRebuilt = true;
			putChunkIntoMap(chunk, ChunkManager.buildChunks);
		}
		if (blockX == 0) {
			chunk = getChunkAtRenderedOnly(chunkX - ChunkManager.chunkWidth, chunkZ);
			chunk.needsRebuilt = true;
			putChunkIntoMap(chunk, ChunkManager.buildChunks);
		}
		if (blockZ == 15) {
			chunk = getChunkAtRenderedOnly(chunkX, chunkZ + ChunkManager.chunkWidth);
			chunk.needsRebuilt = true;
			putChunkIntoMap(chunk, ChunkManager.buildChunks);
		}
		if (blockZ == 0) {
			chunk = getChunkAtRenderedOnly(chunkX, chunkZ - ChunkManager.chunkWidth);
			chunk.needsRebuilt = true;
			putChunkIntoMap(chunk, ChunkManager.buildChunks);
		}
			
		
		chunk = getChunkAtRenderedOnly(chunkX, chunkZ);
		chunk.needsRebuilt = true;
		putChunkIntoMap(chunk, ChunkManager.buildChunks);
	}
	
	public static void saveBlockBreak(float x, float y, float z) {
		
		int chunkX = (int)Math.floor(x / ChunkManager.chunkWidth) * ChunkManager.chunkWidth;
		int chunkZ = (int)Math.floor(z / ChunkManager.chunkWidth) * ChunkManager.chunkWidth;
		int blockX = (int) (x - chunkX);
		int blockY = (int) y;
		int blockZ = (int) (z - chunkZ);
		
		getChunkAtRenderedOnly(chunkX, chunkZ).changes.put(blockX + "" + blockY + "" + blockZ, "0 " + blockX + " " + blockY + " " + blockZ + " ");
		
	}
	
	public static void saveBlockPlace(float x, float y, float z, int blockType) {
		
		int chunkX = (int)Math.floor(x / ChunkManager.chunkWidth) * ChunkManager.chunkWidth;
		int chunkZ = (int)Math.floor(z / ChunkManager.chunkWidth) * ChunkManager.chunkWidth;
		int blockX = (int) (x - chunkX);
		int blockY = (int) y;
		int blockZ = (int) (z - chunkZ);
		
		getChunkAtRenderedOnly(chunkX, chunkZ).changes.put(blockX + "" + blockY + "" + blockZ, "1 " + blockType + " " + blockX + " " + blockY + " " + blockZ + " ");
		
	}
	
	public static Chunk getChunkAtRenderedOnly(float x, float z) {
		
		String chunkCheckValue = ChunkManager.getChunkListLocation(x, z, ChunkManager.renderChunks);
		Chunk chunkCheck = null;
		
		if (chunkCheckValue != null)
			chunkCheck = ChunkManager.renderChunks.get(chunkCheckValue);
		else
			chunkCheck = null;
		
		return chunkCheck;
		
	}
	
	public static void putChunkIntoMap(Chunk chunk, Map<String, Chunk> map) {
		
		map.put((int)chunk.position.x + "" + (int)chunk.position.y, chunk);
		
	}
	
	public static void removeChunkFromMap(Chunk chunk, Map<String, Chunk> map) {
			
		map.remove((int)chunk.position.x + "" + (int)chunk.position.y);
			
	}
	
	public static Chunk getChunkAt(float x, float z) {
		
		String chunkCheckValue = ChunkManager.getChunkListLocation(x, z, ChunkManager.renderChunks);
		Chunk chunkCheck = null;
		
		if (chunkCheckValue != null)
			chunkCheck = ChunkManager.renderChunks.get(chunkCheckValue);
		else {
			chunkCheckValue = ChunkManager.getChunkListLocation(x, z, ChunkManager.buildChunks);
			
			if (chunkCheckValue != null)
				chunkCheck = ChunkManager.buildChunks.get(chunkCheckValue);
			else{
				
				chunkCheckValue = ChunkManager.getChunkListLocation(x, z, ChunkManager.waitingChunks);
				
				if (chunkCheckValue != null)
					chunkCheck = ChunkManager.waitingChunks.get(chunkCheckValue);
				else
					chunkCheck = null;
				
			}
		}
		
		//System.out.println(chunkCheckValue);
		
		return chunkCheck;
		
	}
	
	public static void savePlayerDataToFile() {
		
		try {
			
			Formatter format = new Formatter(new File("playerdata.dat"));
			
			format.format("%f %f %f", Camera.position.x, Camera.position.y, Camera.position.z);
			
			format.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void saveChangesToFile(Chunk chunk) {
		
		try {
			
			if (!chunk.changes.isEmpty()) {
			
				format = new Formatter(new File("chunks/" + (int)chunk.position.x + "" + (int)chunk.position.y + ".chunk"));
				
				for (String str : chunk.changes.values()) {
					format.format(str);
				}
				
				format.close();
			
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public static Matrix4f getProjectionMatrix(int fov, float NEAR_PLANE, float FAR_PLANE) {
		
		mat = new Matrix4f();
		
		float aspectRatio = (float) Display.getWidth()/(float) Display.getHeight();
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(fov / 2.0f))) * aspectRatio);
		float x_scale = y_scale / aspectRatio;
		float frustum_length = FAR_PLANE - NEAR_PLANE;
        
        
        mat.m00 = x_scale;
        mat.m11 = y_scale;
        mat.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
        mat.m23 = -1;
        mat.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
        mat.m33 = 0;
        
        
		return mat;
		
	}
	
	public static Matrix4f getTransformtionMatrix(Vector3f position, Vector3f rotation, float scale) {
		
		mat = new Matrix4f();
		mat.setIdentity();
		
		mat.translate(position);
		mat.rotate((float)Math.toRadians(rotation.x), new Vector3f(1, 0, 0));
		mat.rotate((float)Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
		mat.rotate((float)Math.toRadians(rotation.z), new Vector3f(0, 0, 1));
		mat.scale(new Vector3f(scale, scale, scale));
		
		return mat;
		
	}
	
	public static Matrix4f getViewMatrix() {
		
		mat = new Matrix4f();
		mat.setIdentity();
		
		mat.rotate((float)Math.toRadians(Camera.rotation.x), new Vector3f(1, 0, 0));
		mat.rotate((float)Math.toRadians(Camera.rotation.y), new Vector3f(0, 1, 0));
		mat.rotate((float)Math.toRadians(Camera.rotation.z), new Vector3f(0, 0, 1));
		mat.translate(new Vector3f(-Camera.position.x, -Camera.position.y, -Camera.position.z));
		
		return mat;
		
	}
	
	public static Matrix4f getCullingViewMatrix() {
		
		mat = new Matrix4f();
		mat.setIdentity();
		
		mat.rotate((float)Math.toRadians(Camera.rotation.x), new Vector3f(1, 0, 0));
		mat.rotate((float)Math.toRadians(Camera.rotation.y), new Vector3f(0, 1, 0));
		mat.rotate((float)Math.toRadians(Camera.rotation.z), new Vector3f(0, 0, 1));
		mat.translate(new Vector3f(-Camera.position.x, -Camera.position.y, -Camera.position.z));
		
		return mat;
		
	}
	
	public static int loadTexture(String texture) {
		
		try {
			
			Texture tex = TextureLoader.getTexture("PNG", new FileInputStream(new File(texture)), GL11.GL_NEAREST);
			
			return tex.getTextureID();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return -1;
		
	}
	
	public static float[] convertListToArray(List<Float> data) {
		
		tempArray = new float[data.size()];
		
		for (int i = 0; i < data.size(); i++) {
			tempArray[i] = data.get(i);
		}
		
		return tempArray;
		
	}
	
	public static float Pick(float... thing) {
		
		randomNumber = ran.nextInt(thing.length);
		return thing[randomNumber];
		
	}
	
	public static String Pick(String... thing) {
		
		randomNumber = ran.nextInt(thing.length);
		return thing[randomNumber];
		
	}
	
}
