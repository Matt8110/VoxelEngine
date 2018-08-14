package Blocks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import Gui.Texture2D;
import Utils.Utils;
import VoxelEngine.Camera;
import VoxelEngine.Main;
import net.jlibnoise.generator.Perlin;

public class ChunkManager {

	
	public static int viewDistance = 15;//15
	public static int viewDistanceDivide = viewDistance/2;
	
	public static Map<String, Chunk> renderChunks = Collections.synchronizedMap(new ConcurrentHashMap <String, Chunk>());
	public static Map<String, Chunk> waitingChunks = Collections.synchronizedMap(new ConcurrentHashMap <String, Chunk>());
	public static Map<String, Chunk> buildChunks = Collections.synchronizedMap(new ConcurrentHashMap <String, Chunk>());
	public static int blockTextures;
	
	public static int chunkWidth = 16;
	public static int chunkHeight = 128;
	
	public static int lastPlayerChunkX = (int)Math.floor(Camera.position.x / chunkWidth);
	public static int lastPlayerChunkZ = (int)Math.floor(Camera.position.z / chunkWidth);
	public static int playerChunkX = lastPlayerChunkX;
	public static int playerChunkZ = lastPlayerChunkZ;
	public static String currentChunkLoc;
	public static String otherChunkLoc;
	public static Chunk currentChunk, otherChunk;
	
	public static Texture2D background = new Texture2D("black.png", new Vector2f(0, 0), new Vector2f(Display.getWidth(), Display.getHeight()));
	public static Texture2D generating = new Texture2D("generating.png", new Vector2f(Display.getWidth()/2.0f-64, Display.getHeight()/2.0f-64), new Vector2f(Display.getWidth()/2.0f+64, Display.getHeight()/2.0f+64));
	
	public static boolean hasLoaded = false;
	
	public static void initChunks() {
		
		Camera.position.x = viewDistance/2.0f * chunkWidth;
		Camera.position.z = viewDistance/2.0f * chunkWidth;
		
		lastPlayerChunkX = (int)Math.floor(Camera.position.x / chunkWidth);
		lastPlayerChunkZ = (int)Math.floor(Camera.position.z / chunkWidth);
		playerChunkX = lastPlayerChunkX;
		playerChunkZ = lastPlayerChunkZ;
		
		blockTextures = Utils.loadTexture("blocks.png");
		
				for (int i = 0; i < viewDistance; i++) {
					for (int j = 0; j < viewDistance; j++) {
				Utils.putChunkIntoMap(new Chunk(i, j), buildChunks);
			}
		}
		
		Thread chunkThread = new Thread(new ChunkThread());
		chunkThread.start();
		
		chunkThread.setPriority(Thread.MIN_PRIORITY);
		
	}
	
	public static void renderChunks() {
		
		//Add/remove chunks
		updateChunks();
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, blockTextures);
		
		for (Chunk chunk : renderChunks.values()) {
			
			chunk.renderAndUpdate();
			
			
			if (chunk.needsRebuilt || chunk.needsRegenerated) {
				chunk.beingRebuilt = true;
				Utils.putChunkIntoMap(chunk, buildChunks);
			}
			
		}
		
		for (Chunk chunk : renderChunks.values()) {
			chunk.renderAndUpdateTransparent();
		}
		
		
	}

	public static String getChunkListLocation(float x, float z, Map<String, Chunk> list){
		
		if (list.containsKey((int)x + "" + (int)z))
				return (int)x + "" + (int)z;
		
		return null;
		
	}

	public static void updateChunks() {
		
		playerChunkX = (int) (Camera.position.x / chunkWidth);
		playerChunkZ = (int) (Camera.position.z / chunkWidth);
		
		//TODO: May cause errors if the last player chunk position is reset after one runs
		
		if (playerChunkX > lastPlayerChunkX) {
			
			for (int i = 0; i < viewDistance; i++) {
			
				int oldPosX = (lastPlayerChunkX - viewDistanceDivide) * chunkWidth;
				int oldPosZ = (lastPlayerChunkZ - viewDistanceDivide + i) * chunkWidth;
				int newPosX = (playerChunkX + viewDistanceDivide) * chunkWidth;
				int newPosZ = (playerChunkZ - viewDistanceDivide + i) * chunkWidth;
				
				//Rebuild chunk next to new chunk to get rid of culling errors
				int otherChunkX = (playerChunkX + viewDistanceDivide - 1) * chunkWidth;
				int otherChunkZ = (playerChunkZ - viewDistanceDivide + i) * chunkWidth;
				otherChunk = Utils.getChunkAt(otherChunkX, otherChunkZ);
				otherChunk.needsRebuilt = true;
				
				currentChunk = Utils.getChunkAt(oldPosX, oldPosZ);
				
				Utils.removeChunkFromMap(currentChunk, renderChunks);
				
				currentChunk.position.x = newPosX;
				currentChunk.position.y = newPosZ;
				currentChunk.needsRegenerated = true;
				
				Utils.putChunkIntoMap(currentChunk, renderChunks);
			
			}
			lastPlayerChunkX = playerChunkX;
			
		}else if (playerChunkX < lastPlayerChunkX) {
			
			for (int i = 0; i < viewDistance; i++) {
			
				int oldPosX = (lastPlayerChunkX + viewDistanceDivide) * chunkWidth;
				int oldPosZ = (lastPlayerChunkZ - viewDistanceDivide + i) * chunkWidth;
				int newPosX = (playerChunkX - viewDistanceDivide) * chunkWidth;
				int newPosZ = (playerChunkZ - viewDistanceDivide + i) * chunkWidth;
				
				//Rebuild chunk next to new chunk to get rid of culling errors
				int otherChunkX = (playerChunkX - viewDistanceDivide + 1) * chunkWidth;
				int otherChunkZ = (playerChunkZ - viewDistanceDivide + i) * chunkWidth;
				otherChunk = Utils.getChunkAt(otherChunkX, otherChunkZ);
				otherChunk.needsRebuilt = true;
				
				currentChunk = Utils.getChunkAt(oldPosX, oldPosZ);
				
				Utils.removeChunkFromMap(currentChunk, renderChunks);
				
				currentChunk.position.x = newPosX;
				currentChunk.position.y = newPosZ;
				currentChunk.needsRegenerated = true;
				
				Utils.putChunkIntoMap(currentChunk, renderChunks);
			
			}
			lastPlayerChunkX = playerChunkX;
			
		}else if (playerChunkZ > lastPlayerChunkZ) {
			
			for (int i = 0; i < viewDistance; i++) {
			
				int oldPosZ = (lastPlayerChunkZ - viewDistanceDivide) * chunkWidth;
				int oldPosX = (lastPlayerChunkX - viewDistanceDivide + i) * chunkWidth;
				int newPosZ = (playerChunkZ + viewDistanceDivide) * chunkWidth;
				int newPosX = (playerChunkX - viewDistanceDivide + i) * chunkWidth;
				
				//Rebuild chunk next to new chunk to get rid of culling errors
				int otherChunkZ = (playerChunkZ + viewDistanceDivide - 1) * chunkWidth;
				int otherChunkX = (playerChunkX - viewDistanceDivide + i) * chunkWidth;
				otherChunk = Utils.getChunkAt(otherChunkX, otherChunkZ);
				otherChunk.needsRebuilt = true;
				
				currentChunk = Utils.getChunkAt(oldPosX, oldPosZ);
				
				Utils.removeChunkFromMap(currentChunk, renderChunks);
				
				currentChunk.position.x = newPosX;
				currentChunk.position.y = newPosZ;
				currentChunk.needsRegenerated = true;
				
				Utils.putChunkIntoMap(currentChunk, renderChunks);
			
			}
			lastPlayerChunkZ = playerChunkZ;
			
		}else if (playerChunkZ < lastPlayerChunkZ) {
			
			for (int i = 0; i < viewDistance; i++) {
			
				int oldPosZ = (lastPlayerChunkZ + viewDistanceDivide) * chunkWidth;
				int oldPosX = (lastPlayerChunkX - viewDistanceDivide + i) * chunkWidth;
				int newPosZ = (playerChunkZ - viewDistanceDivide) * chunkWidth;
				int newPosX = (playerChunkX - viewDistanceDivide + i) * chunkWidth;
				
				//Rebuild chunk next to new chunk to get rid of culling errors
				int otherChunkZ = (playerChunkZ - viewDistanceDivide + 1) * chunkWidth;
				int otherChunkX = (playerChunkX - viewDistanceDivide + i) * chunkWidth;
				otherChunk = Utils.getChunkAt(otherChunkX, otherChunkZ);
				otherChunk.needsRebuilt = true;
				
				currentChunk = Utils.getChunkAt(oldPosX, oldPosZ);
				
				Utils.removeChunkFromMap(currentChunk, renderChunks);
				
				currentChunk.position.x = newPosX;
				currentChunk.position.y = newPosZ;
				currentChunk.needsRegenerated = true;
				
				Utils.putChunkIntoMap(currentChunk, renderChunks);
			
			}
			
			
			lastPlayerChunkZ = playerChunkZ;
		}
		
		
		
	}
	
	static class ChunkThread implements Runnable{

		private Chunk chunk;
		private Chunk chunkCheckNegZ, chunkCheckPosZ, chunkCheckNegX, chunkCheckPosX;
		
		private List<Float> tempVertices = new ArrayList<Float>();
		private List<Float> tempNormals = new ArrayList<Float>();
		private List<Float> tempTexCoords = new ArrayList<Float>();
		
		//private float height;
		private float texCoordX, texCoordY;
		private float textureIncrement = 32.0f/1024.0f;
		
		private BlockType blockType;
		
		private Perlin p = new Perlin();
		
		public void run() {
			
			while (Main.isRunning) {
				
					if (!buildChunks.isEmpty()) {
						
						for (Chunk chunkFor : buildChunks.values()) {
							
							chunk = chunkFor;
							
							if (!chunk.needsRebuilt && !chunk.filled) {
								
								fillChunk();
							}
								
								
							if (chunk.filled) {
								
								chunkCheckNegZ = Utils.getChunkAt(chunk.position.x, chunk.position.y - chunkWidth);
								chunkCheckPosZ = Utils.getChunkAt(chunk.position.x, chunk.position.y + chunkWidth);
								chunkCheckNegX = Utils.getChunkAt(chunk.position.x - chunkWidth, chunk.position.y);
								chunkCheckPosX = Utils.getChunkAt(chunk.position.x + chunkWidth, chunk.position.y);
								
								buildChunk();
								
								chunk.needsRebuilt = false;
								chunk.needsRegenerated = false;
								chunk.beingRebuilt = false;
								chunk.built = true;
								chunk.transparentBuilt = true;
								chunk.filled = false;
								
								Utils.putChunkIntoMap(chunk, renderChunks);
								Utils.removeChunkFromMap(chunkFor, buildChunks);
							}
							if (!chunk.built)
								chunk.filled = true;
							
						}
						
					}else {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				
				
			}
			
		}
		
		public void fillChunk() {
				
				for (int z = 0; z < chunkWidth; z++) {
					for (int x = 0; x < chunkWidth; x++) {
						
						float height = (float) (p.getValue((chunk.position.x + x)/100.0f, (chunk.position.y + z)/100.0f, 0.1f)+1)*10;
						
						
						for (int y = 0; y < chunkHeight; y++) {
							//float caves = (float) (p.getValue((chunk.position.x + x)/20.0f, (chunk.position.y + z)/20.0f, (y)/5.0f-100) + 0.5f);
							
							
							if (!chunk.needsRegenerated)
								chunk.blocks[x][y][z] = new Block();
							else
								chunk.blocks[x][y][z].isActive = false;
							
							
								
								if (y < height+45) {
									
										chunk.blocks[x][y][z].blockType = BlockType.STONE;
									
									
									chunk.blocks[x][y][z].isActive = true;
								}
								else if (y < height+50) {
									chunk.blocks[x][y][z].blockType = BlockType.DIRT;
									chunk.blocks[x][y][z].isActive = true;
								}else if (y > height+50 && y < height+51) {
									chunk.blocks[x][y][z].blockType = BlockType.GRASS;
									chunk.blocks[x][y][z].isActive = true;
								}
								
								if (y < 60 && !chunk.blocks[x][y][z].isActive) {
									chunk.blocks[x][y][z].isActive = true;
									chunk.blocks[x][y][z].blockType = BlockType.WATER;
								}
	
								
								//if (caves > 1) {
								//	chunk.blocks[x][y][z].isActive = false;
								//}
							
						}
					}
				}
				
				try {
					
					File file = new File("chunks/" + (int)chunk.position.x + "" + (int)chunk.position.y + ".chunk");
					
				if (file.exists()) {
					
					Scanner scan = new Scanner(file);
					
					while (scan.hasNext()) {
						
						int identifier = scan.nextInt();
						
						if (identifier == 1) {
							
							int blockType = scan.nextInt();
							int xPos = scan.nextInt();
							int yPos = scan.nextInt();
							int zPos = scan.nextInt();
							
							chunk.changes.put(xPos + "" + yPos + "" + zPos, "1 " + blockType + " " + xPos + " " + yPos + " " + zPos + " ");
							
							chunk.blocks[xPos][yPos][zPos].isActive = true;
							chunk.blocks[xPos][yPos][zPos].blockType = BlockType.getByInt(blockType);
							
						}
						
						if (identifier == 0) {
							
							int xPos = scan.nextInt();
							int yPos = scan.nextInt();
							int zPos = scan.nextInt();
							
							chunk.changes.put(xPos + "" + yPos + "" + zPos, "0 " + xPos + " " + yPos + " " + zPos + " ");
							
							chunk.blocks[xPos][yPos][zPos].isActive = false;
							
						}
						
					}
					
					scan.close();
			
				}
					}catch(Exception e) {
						e.printStackTrace();
					}
			
		}
		
		public void buildChunk() {
			
			for (int z = 0; z < chunkWidth; z++) 
				for (int x = 0; x < chunkWidth; x++) 
					for (int y = 0; y < chunkHeight; y++) {
						
						if (chunk.blocks[x][y][z].isActive && !chunk.blocks[x][y][z].blockType.transparent) {
							
								addBlock(x, y, z);
								
								
							
						}
			}
			
			chunk.vertices = Utils.convertListToArray(tempVertices);
			chunk.normals = Utils.convertListToArray(tempVertices);
			chunk.texCoords = Utils.convertListToArray(tempTexCoords);
			
			tempVertices.clear();
			tempNormals.clear();
			tempTexCoords.clear();
			
			for (int z = 0; z < chunkWidth; z++)
				for (int x = 0; x < chunkWidth; x++)
					for (int y = 0; y < chunkHeight; y++) {
						
						if (chunk.blocks[x][y][z].blockType.transparent && chunk.blocks[x][y][z].isActive) {
							
							addBlock(x, y, z);
						
						}
						
					}
			
			chunk.transparentVertices = Utils.convertListToArray(tempVertices);
			chunk.transparentNormals = Utils.convertListToArray(tempVertices);
			chunk.transparentTexCoords = Utils.convertListToArray(tempTexCoords);
			
			tempVertices.clear();
			tempNormals.clear();
			tempTexCoords.clear();
			
			
		}
		
		public void addBlock(float x, float y, float z) {
			
			
						
						blockType = chunk.blocks[(int) x][(int) y][(int) z].blockType;
						
						
						//Bottom
						
						//for (int i = 0; i < 18; i += 3) {
						//	tempNormals.add(0.0f);
						//	tempNormals.add(-1.0f);
						//	tempNormals.add(0.0f);
						//}
						if ((int)y != 0) {
							if (!chunk.blocks[(int) x][(int) y-1][(int) z].isActive || (chunk.blocks[(int) x][(int) y-1][(int) z].blockType.transparent) && !chunk.blocks[(int) x][(int) y][(int) z].blockType.transparent) {
						
								
								
							texCoordY = (float)((int)(blockType.textureBottom/32.0f));
							texCoordX = (blockType.textureBottom - texCoordY*32.0f)*textureIncrement;
							texCoordY *= textureIncrement;
							
							tempTexCoords.add(texCoordX+textureIncrement);
							tempTexCoords.add(texCoordY);
							
							tempVertices.add(chunk.position.x + x+1);
							tempVertices.add(y);
							tempVertices.add(chunk.position.y + z);
							
							
							tempTexCoords.add(texCoordX);
							tempTexCoords.add(texCoordY+textureIncrement);
							
							tempVertices.add(chunk.position.x + x);
							tempVertices.add(y);
							tempVertices.add(chunk.position.y + z+1);
							
							
							tempTexCoords.add(texCoordX);
							tempTexCoords.add(texCoordY);
							
							tempVertices.add(chunk.position.x + x);
							tempVertices.add(y);
							tempVertices.add(chunk.position.y + z);
							
							tempTexCoords.add(texCoordX+textureIncrement);
							tempTexCoords.add(texCoordY+textureIncrement);
							
							tempVertices.add(chunk.position.x + x+1);
							tempVertices.add(y);
							tempVertices.add(chunk.position.y + z+1);
							
							
							tempTexCoords.add(texCoordX);
							tempTexCoords.add(texCoordY+textureIncrement);
							
							tempVertices.add(chunk.position.x + x);
							tempVertices.add(y);
							tempVertices.add(chunk.position.y + z+1);
							
							
							tempTexCoords.add(texCoordX+textureIncrement);
							tempTexCoords.add(texCoordY);
							
							tempVertices.add(chunk.position.x + x+1);
							tempVertices.add(y);
							tempVertices.add(chunk.position.y + z);
						
							}
						}
						
						//Top
						
						if ((int)y != chunkHeight-1) {
							if (!chunk.blocks[(int) x][(int) y+1][(int) z].isActive || (chunk.blocks[(int) x][(int) y+1][(int) z].blockType.transparent) && !chunk.blocks[(int) x][(int) y][(int) z].blockType.transparent) {
								
						//for (int i = 0; i < 18; i += 3) {
							
						//	tempNormals.add(0.0f);
						//	tempNormals.add(1.0f);
						//	tempNormals.add(0.0f);
							
							
						//}
								
						texCoordY = (float)((int)(blockType.textureTop/32.0f));
						texCoordX = (blockType.textureTop - texCoordY*32.0f)*textureIncrement;
						texCoordY *= textureIncrement;
						
						tempTexCoords.add(texCoordX);
						tempTexCoords.add(texCoordY);
						
						tempVertices.add(chunk.position.x + x);
						tempVertices.add(y+1);
						tempVertices.add(chunk.position.y + z);
						
						
						tempTexCoords.add(texCoordX);
						tempTexCoords.add(texCoordY+textureIncrement);
						
						tempVertices.add(chunk.position.x + x);
						tempVertices.add(y+1);
						tempVertices.add(chunk.position.y + z+1);
						
						
						tempTexCoords.add(texCoordX+textureIncrement);
						tempTexCoords.add(texCoordY);
						
						tempVertices.add(chunk.position.x + x+1);
						tempVertices.add(y+1);
						tempVertices.add(chunk.position.y + z);
						
						
						tempTexCoords.add(texCoordX+textureIncrement);
						tempTexCoords.add(texCoordY);
						
						tempVertices.add(chunk.position.x + x+1);
						tempVertices.add(y+1);
						tempVertices.add(chunk.position.y + z);
						
						
						tempTexCoords.add(texCoordX);
						tempTexCoords.add(texCoordY+textureIncrement);
						
						tempVertices.add(chunk.position.x + x);
						tempVertices.add(y+1);
						tempVertices.add(chunk.position.y + z+1);
						
						
						tempTexCoords.add(texCoordX+textureIncrement);
						tempTexCoords.add(texCoordY+textureIncrement);
						
						tempVertices.add(chunk.position.x + x+1);
						tempVertices.add(y+1);
						tempVertices.add(chunk.position.y + z+1);
						
							}
						}
						
						//Back
						
						texCoordY = (float)((int)(blockType.textureBack/32.0f));
						texCoordX = (blockType.textureBack - texCoordY*32.0f)*textureIncrement;
						texCoordY *= textureIncrement;
						
						if ((int)z != 0) {
							if (!chunk.blocks[(int) x][(int) y][(int) z-1].isActive || (chunk.blocks[(int) x][(int) y][(int) z-1].blockType.transparent) && !chunk.blocks[(int) x][(int) y][(int) z].blockType.transparent) {
						
						
						
						tempTexCoords.add(texCoordX);
						tempTexCoords.add(texCoordY+textureIncrement);
						
						tempVertices.add(chunk.position.x + x);
						tempVertices.add(y);
						tempVertices.add(chunk.position.y + z);
						
						
						tempTexCoords.add(texCoordX);
						tempTexCoords.add(texCoordY);
						
						tempVertices.add(chunk.position.x + x);
						tempVertices.add(y+1);
						tempVertices.add(chunk.position.y + z);
						
						
						tempTexCoords.add(texCoordX+textureIncrement);
						tempTexCoords.add(texCoordY+textureIncrement);
						
						tempVertices.add(chunk.position.x + x+1);
						tempVertices.add(y);
						tempVertices.add(chunk.position.y + z);
						
						
						tempTexCoords.add(texCoordX+textureIncrement);
						tempTexCoords.add(texCoordY+textureIncrement);
						
						tempVertices.add(chunk.position.x + x+1);
						tempVertices.add(y);
						tempVertices.add(chunk.position.y + z);
						
						
						tempTexCoords.add(texCoordX);
						tempTexCoords.add(texCoordY);
						
						tempVertices.add(chunk.position.x + x);
						tempVertices.add(y+1);
						tempVertices.add(chunk.position.y + z);
						
						
						tempTexCoords.add(texCoordX+textureIncrement);
						tempTexCoords.add(texCoordY);
						
						tempVertices.add(chunk.position.x + x+1);
						tempVertices.add(y+1);
						tempVertices.add(chunk.position.y + z);
						
							}
						}else if (chunkCheckNegZ != null){
							
							if (!chunkCheckNegZ.blocks[(int) x][(int) y][(int) (15)].isActive || chunkCheckNegZ.blocks[(int) x][(int) y][(int) (15)].blockType.transparent && !chunk.blocks[(int) x][(int) y][(int) z].blockType.transparent) {
							
							tempTexCoords.add(texCoordX);
							tempTexCoords.add(texCoordY+textureIncrement);
							
							tempVertices.add(chunk.position.x + x);
							tempVertices.add(y);
							tempVertices.add(chunk.position.y + z);
							
							
							tempTexCoords.add(texCoordX);
							tempTexCoords.add(texCoordY);
							
							tempVertices.add(chunk.position.x + x);
							tempVertices.add(y+1);
							tempVertices.add(chunk.position.y + z);
							
							
							tempTexCoords.add(texCoordX+textureIncrement);
							tempTexCoords.add(texCoordY+textureIncrement);
							
							tempVertices.add(chunk.position.x + x+1);
							tempVertices.add(y);
							tempVertices.add(chunk.position.y + z);
							
							
							tempTexCoords.add(texCoordX+textureIncrement);
							tempTexCoords.add(texCoordY+textureIncrement);
							
							tempVertices.add(chunk.position.x + x+1);
							tempVertices.add(y);
							tempVertices.add(chunk.position.y + z);
							
							
							tempTexCoords.add(texCoordX);
							tempTexCoords.add(texCoordY);
							
							tempVertices.add(chunk.position.x + x);
							tempVertices.add(y+1);
							tempVertices.add(chunk.position.y + z);
							
							
							tempTexCoords.add(texCoordX+textureIncrement);
							tempTexCoords.add(texCoordY);
							
							tempVertices.add(chunk.position.x + x+1);
							tempVertices.add(y+1);
							tempVertices.add(chunk.position.y + z);
							}
						}
						
						//Front
						
						texCoordY = (float)((int)(blockType.textureFront/32.0f));
						texCoordX = (blockType.textureFront - texCoordY*32.0f)*textureIncrement;
						texCoordY *= textureIncrement;
						
						if ((int)z != chunkWidth-1) {
							if (!chunk.blocks[(int) x][(int) y][(int) z+1].isActive || (chunk.blocks[(int) x][(int) y][(int) z+1].blockType.transparent) && !chunk.blocks[(int) x][(int) y][(int) z].blockType.transparent) {
						
						tempTexCoords.add(texCoordX+textureIncrement);
						tempTexCoords.add(texCoordY+textureIncrement);
						
						tempVertices.add(chunk.position.x + x+1);
						tempVertices.add(y);
						tempVertices.add(chunk.position.y + z+1);
						
						
						tempTexCoords.add(texCoordX);
						tempTexCoords.add(texCoordY);
						
						tempVertices.add(chunk.position.x + x);
						tempVertices.add(y+1);
						tempVertices.add(chunk.position.y + z+1);
						
						
						tempTexCoords.add(texCoordX);
						tempTexCoords.add(texCoordY+textureIncrement);
						
						tempVertices.add(chunk.position.x + x);
						tempVertices.add(y);
						tempVertices.add(chunk.position.y + z+1);
						
						
						
						
						tempTexCoords.add(texCoordX+textureIncrement);
						tempTexCoords.add(texCoordY);
						
						tempVertices.add(chunk.position.x + x+1);
						tempVertices.add(y+1);
						tempVertices.add(chunk.position.y + z+1);
						
						
						tempTexCoords.add(texCoordX);
						tempTexCoords.add(texCoordY);
						
						tempVertices.add(chunk.position.x + x);
						tempVertices.add(y+1);
						tempVertices.add(chunk.position.y + z+1);
						
						
						tempTexCoords.add(texCoordX+textureIncrement);
						tempTexCoords.add(texCoordY+textureIncrement);
						
						tempVertices.add(chunk.position.x + x+1);
						tempVertices.add(y);
						tempVertices.add(chunk.position.y + z+1);
							}
						}else if (chunkCheckPosZ != null){
							
							if (!chunkCheckPosZ.blocks[(int) x][(int) y][(int) (0)].isActive || chunkCheckPosZ.blocks[(int) x][(int) y][(int) (0)].blockType.transparent && !chunk.blocks[(int) x][(int) y][(int) z].blockType.transparent) {
							
							tempTexCoords.add(texCoordX+textureIncrement);
							tempTexCoords.add(texCoordY+textureIncrement);
							
							tempVertices.add(chunk.position.x + x+1);
							tempVertices.add(y);
							tempVertices.add(chunk.position.y + z+1);
							
							
							tempTexCoords.add(texCoordX);
							tempTexCoords.add(texCoordY);
							
							tempVertices.add(chunk.position.x + x);
							tempVertices.add(y+1);
							tempVertices.add(chunk.position.y + z+1);
							
							
							tempTexCoords.add(texCoordX);
							tempTexCoords.add(texCoordY+textureIncrement);
							
							tempVertices.add(chunk.position.x + x);
							tempVertices.add(y);
							tempVertices.add(chunk.position.y + z+1);
							
							
							
							
							tempTexCoords.add(texCoordX+textureIncrement);
							tempTexCoords.add(texCoordY);
							
							tempVertices.add(chunk.position.x + x+1);
							tempVertices.add(y+1);
							tempVertices.add(chunk.position.y + z+1);
							
							
							tempTexCoords.add(texCoordX);
							tempTexCoords.add(texCoordY);
							
							tempVertices.add(chunk.position.x + x);
							tempVertices.add(y+1);
							tempVertices.add(chunk.position.y + z+1);
							
							
							tempTexCoords.add(texCoordX+textureIncrement);
							tempTexCoords.add(texCoordY+textureIncrement);
							
							tempVertices.add(chunk.position.x + x+1);
							tempVertices.add(y);
							tempVertices.add(chunk.position.y + z+1);
							
							}
						}
						
						//Left
						
						texCoordY = (float)((int)(blockType.textureSides/32.0f));
						texCoordX = (blockType.textureSides - texCoordY*32.0f)*textureIncrement;
						texCoordY *= textureIncrement;
						
						if ((int)x != 0) {
							if (!chunk.blocks[(int) x-1][(int) y][(int) z].isActive || (chunk.blocks[(int) x-1][(int) y][(int) z].blockType.transparent) && !chunk.blocks[(int) x][(int) y][(int) z].blockType.transparent) {
						
						tempTexCoords.add(texCoordX+textureIncrement);
						tempTexCoords.add(texCoordY+textureIncrement);
						
						tempVertices.add(chunk.position.x + x);
						tempVertices.add(y);
						tempVertices.add(chunk.position.y + z+1);
						
						
						tempTexCoords.add(texCoordX);
						tempTexCoords.add(texCoordY);
						
						tempVertices.add(chunk.position.x + x);
						tempVertices.add(y+1);
						tempVertices.add(chunk.position.y + z);
						
						
						tempTexCoords.add(texCoordX);
						tempTexCoords.add(texCoordY+textureIncrement);
						
						tempVertices.add(chunk.position.x + x);
						tempVertices.add(y);
						tempVertices.add(chunk.position.y + z);
						
						
						
						
						tempTexCoords.add(texCoordX+textureIncrement);
						tempTexCoords.add(texCoordY);
						
						tempVertices.add(chunk.position.x + x);
						tempVertices.add(y+1);
						tempVertices.add(chunk.position.y + z+1);
						
						
						tempTexCoords.add(texCoordX);
						tempTexCoords.add(texCoordY);
						
						tempVertices.add(chunk.position.x + x);
						tempVertices.add(y+1);
						tempVertices.add(chunk.position.y + z);
						
						
						tempTexCoords.add(texCoordX+textureIncrement);
						tempTexCoords.add(texCoordY+textureIncrement);
						
						tempVertices.add(chunk.position.x + x);
						tempVertices.add(y);
						tempVertices.add(chunk.position.y + z+1);
							}
						}else if (chunkCheckNegX != null){
							
							if (!chunkCheckNegX.blocks[(int) 15][(int) y][(int) (z)].isActive || chunkCheckNegX.blocks[(int) 15][(int) y][(int) (z)].blockType.transparent && !chunk.blocks[(int) x][(int) y][(int) z].blockType.transparent) {
							
							tempTexCoords.add(texCoordX+textureIncrement);
							tempTexCoords.add(texCoordY+textureIncrement);
							
							tempVertices.add(chunk.position.x + x);
							tempVertices.add(y);
							tempVertices.add(chunk.position.y + z+1);
							
							
							tempTexCoords.add(texCoordX);
							tempTexCoords.add(texCoordY);
							
							tempVertices.add(chunk.position.x + x);
							tempVertices.add(y+1);
							tempVertices.add(chunk.position.y + z);
							
							
							tempTexCoords.add(texCoordX);
							tempTexCoords.add(texCoordY+textureIncrement);
							
							tempVertices.add(chunk.position.x + x);
							tempVertices.add(y);
							tempVertices.add(chunk.position.y + z);
							
							
							
							
							tempTexCoords.add(texCoordX+textureIncrement);
							tempTexCoords.add(texCoordY);
							
							tempVertices.add(chunk.position.x + x);
							tempVertices.add(y+1);
							tempVertices.add(chunk.position.y + z+1);
							
							
							tempTexCoords.add(texCoordX);
							tempTexCoords.add(texCoordY);
							
							tempVertices.add(chunk.position.x + x);
							tempVertices.add(y+1);
							tempVertices.add(chunk.position.y + z);
							
							
							tempTexCoords.add(texCoordX+textureIncrement);
							tempTexCoords.add(texCoordY+textureIncrement);
							
							tempVertices.add(chunk.position.x + x);
							tempVertices.add(y);
							tempVertices.add(chunk.position.y + z+1);
							}
						}
						
						//Right
						
						/*texCoordY = (float)((int)(blockType.textureSides/32.0f));
						texCoordX = (blockType.textureSides - texCoordY*32.0f)*textureIncrement;
						texCoordY *= textureIncrement;*/
						
						if ((int)x != chunkWidth-1) {
							if (!chunk.blocks[(int) x+1][(int) y][(int) z].isActive || (chunk.blocks[(int) x+1][(int) y][(int) z].blockType.transparent) && !chunk.blocks[(int) x][(int) y][(int) z].blockType.transparent) {
						
						tempTexCoords.add(texCoordX);
						tempTexCoords.add(texCoordY+textureIncrement);
						
						tempVertices.add(chunk.position.x + x+1);
						tempVertices.add(y);
						tempVertices.add(chunk.position.y + z);
						
						
						tempTexCoords.add(texCoordX);
						tempTexCoords.add(texCoordY);
						
						tempVertices.add(chunk.position.x + x+1);
						tempVertices.add(y+1);
						tempVertices.add(chunk.position.y + z);
						
						
						tempTexCoords.add(texCoordX+textureIncrement);
						tempTexCoords.add(texCoordY+textureIncrement);
						
						tempVertices.add(chunk.position.x + x+1);
						tempVertices.add(y);
						tempVertices.add(chunk.position.y + z+1);
						
						
						tempTexCoords.add(texCoordX+textureIncrement);
						tempTexCoords.add(texCoordY+textureIncrement);
						
						tempVertices.add(chunk.position.x + x+1);
						tempVertices.add(y);
						tempVertices.add(chunk.position.y + z+1);
						
						
						tempTexCoords.add(texCoordX);
						tempTexCoords.add(texCoordY);
						
						tempVertices.add(chunk.position.x + x+1);
						tempVertices.add(y+1);
						tempVertices.add(chunk.position.y + z);
						
						
						tempTexCoords.add(texCoordX+textureIncrement);
						tempTexCoords.add(texCoordY);
						
						tempVertices.add(chunk.position.x + x+1);
						tempVertices.add(y+1);
						tempVertices.add(chunk.position.y + z+1);
							}
						}else if (chunkCheckPosX != null){
							
							if (!chunkCheckPosX.blocks[(int) 0][(int) y][(int) (z)].isActive || chunkCheckPosX.blocks[(int) 0][(int) y][(int) (z)].blockType.transparent && !chunk.blocks[(int) x][(int) y][(int) z].blockType.transparent) {
							
							tempTexCoords.add(texCoordX);
							tempTexCoords.add(texCoordY+textureIncrement);
							
							tempVertices.add(chunk.position.x + x+1);
							tempVertices.add(y);
							tempVertices.add(chunk.position.y + z);
							
							
							tempTexCoords.add(texCoordX);
							tempTexCoords.add(texCoordY);
							
							tempVertices.add(chunk.position.x + x+1);
							tempVertices.add(y+1);
							tempVertices.add(chunk.position.y + z);
							
							
							tempTexCoords.add(texCoordX+textureIncrement);
							tempTexCoords.add(texCoordY+textureIncrement);
							
							tempVertices.add(chunk.position.x + x+1);
							tempVertices.add(y);
							tempVertices.add(chunk.position.y + z+1);
							
							
							tempTexCoords.add(texCoordX+textureIncrement);
							tempTexCoords.add(texCoordY+textureIncrement);
							
							tempVertices.add(chunk.position.x + x+1);
							tempVertices.add(y);
							tempVertices.add(chunk.position.y + z+1);
							
							
							tempTexCoords.add(texCoordX);
							tempTexCoords.add(texCoordY);
							
							tempVertices.add(chunk.position.x + x+1);
							tempVertices.add(y+1);
							tempVertices.add(chunk.position.y + z);
							
							
							tempTexCoords.add(texCoordX+textureIncrement);
							tempTexCoords.add(texCoordY);
							
							tempVertices.add(chunk.position.x + x+1);
							tempVertices.add(y+1);
							tempVertices.add(chunk.position.y + z+1);
							}
						}
			
			
			
			
		}
		
		
	}
	

	
	
}
