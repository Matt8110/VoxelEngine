package Blocks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

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
	
	public static List<Chunk> renderChunks = Collections.synchronizedList(new ArrayList<Chunk>());
	public static List<Chunk> waitingChunks = Collections.synchronizedList(new ArrayList<Chunk>());
	public static List<Chunk> buildChunks = Collections.synchronizedList(new ArrayList<Chunk>());
	public static int blockTextures;
	
	public static int chunkWidth = 16;
	public static int chunkHeight = 128;
	
	public static int lastPlayerChunkX = (int)Math.floor(Camera.position.x / chunkWidth);
	public static int lastPlayerChunkZ = (int)Math.floor(Camera.position.z / chunkWidth);
	public static int playerChunkX = lastPlayerChunkX;
	public static int playerChunkZ = lastPlayerChunkZ;
	public static int currentChunk;
	
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
		
		
//		for (int i = (int)((int)Camera.position.x/chunkWidth - viewDistance/2); i < viewDistance; i++) {
//			for (int j = (int)((int)Camera.position.z/chunkWidth - viewDistance/2); j < viewDistance; j++) {
				for (int i = 0; i < viewDistance; i++) {
					for (int j = 0; j < viewDistance; j++) {
				buildChunks.add(new Chunk(i, j));
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
		
		for (int i = 0; i < waitingChunks.size(); i++) {
			
			renderChunks.add(waitingChunks.get(i));
			
			
			if (waitingChunks.get(i).wasRebuilt) {
				waitingChunks.get(i).wasRebuilt = false;
				
				int rChunk = getChunkListLocation(waitingChunks.get(i).position.x, waitingChunks.get(i).position.y, renderChunks);
				
				renderChunks.remove(rChunk);
				
			}
			
			waitingChunks.remove(i);
			
		}
		
		
		for (int i = 0; i < renderChunks.size(); i++) {
			
			try {
				
				renderChunks.get(i).renderAndUpdate();
				
				if ((renderChunks.get(i).needsRebuilt || renderChunks.get(i).needsRegenerated) && !renderChunks.get(i).beingRebuilt) {
					renderChunks.get(i).beingRebuilt = true;
					buildChunks.add(renderChunks.get(i));
				}
				
			}catch(Exception e) {
				System.err.println("WARNING: Failed to render");
			}
			
			
			
		}
		
		
		
	}

	public static int getChunkListLocation(float x, float z, List<Chunk> list){
		
		for (int i = 0; i < list.size(); i++) {
			
			if (list.size() >= i) 
				if ((int)list.get(i).position.x == (int)x && (int)list.get(i).position.y == (int)z) {
					return i;
				}
			
			
		}
		
		return -1;
		
	}

	public static void updateChunks() {
		
		playerChunkX = (int) Math.floor(Camera.position.x / chunkWidth);
		playerChunkZ = (int) Math.floor(Camera.position.z / chunkWidth);
		
		try {
		
			if (playerChunkX > lastPlayerChunkX) {
				for (int i = 0; i < viewDistance; i++) {
					
					currentChunk = getChunkListLocation((lastPlayerChunkX - (viewDistanceDivide))*chunkWidth, ((lastPlayerChunkZ - viewDistanceDivide) + i)*chunkWidth, renderChunks);
					
					if (currentChunk != -1) {
						
						Utils.saveChangesToFile(renderChunks.get(currentChunk));
						
						renderChunks.get(currentChunk).position.x = (playerChunkX + viewDistanceDivide) * chunkWidth;
						renderChunks.get(currentChunk).position.y = ((playerChunkZ - viewDistanceDivide) + i) * chunkWidth;
						renderChunks.get(currentChunk).needsRegenerated = true;
						renderChunks.get(currentChunk).changes.clear();
					}
				}
			}
			
			if (playerChunkX < lastPlayerChunkX) {
				for (int i = 0; i < viewDistance; i++) {
					
					currentChunk = getChunkListLocation((lastPlayerChunkX + (viewDistanceDivide))*chunkWidth, ((lastPlayerChunkZ - viewDistanceDivide) + i)*chunkWidth, renderChunks);
					
					if (currentChunk != -1) {
						
						Utils.saveChangesToFile(renderChunks.get(currentChunk));
						
						renderChunks.get(currentChunk).position.x = (playerChunkX - viewDistanceDivide) * chunkWidth;
						renderChunks.get(currentChunk).position.y = ((playerChunkZ - viewDistanceDivide) + i) * chunkWidth;
						renderChunks.get(currentChunk).needsRegenerated = true;
						renderChunks.get(currentChunk).changes.clear();
						
					}
				}
			}
			
			if (playerChunkZ > lastPlayerChunkZ) {
				for (int i = 0; i < viewDistance; i++) {
					
					currentChunk = getChunkListLocation(((lastPlayerChunkX - viewDistanceDivide) + i)*chunkWidth,  (lastPlayerChunkZ - (viewDistanceDivide))*chunkWidth, renderChunks );
					
					if (currentChunk != -1) {
						
						Utils.saveChangesToFile(renderChunks.get(currentChunk));
						
						renderChunks.get(currentChunk).position.x = ((playerChunkX - viewDistanceDivide) + i) * chunkWidth;
						renderChunks.get(currentChunk).position.y = (playerChunkZ + viewDistanceDivide) * chunkWidth;
						renderChunks.get(currentChunk).needsRegenerated = true;
						renderChunks.get(currentChunk).changes.clear();
						
					}
				}
			}
			
			if (playerChunkZ < lastPlayerChunkZ) {
				
				for (int i = 0; i < viewDistance; i++) {
					
					currentChunk = getChunkListLocation(((lastPlayerChunkX - viewDistanceDivide) + i)*chunkWidth,  (lastPlayerChunkZ + (viewDistanceDivide))*chunkWidth, renderChunks );
					
					if (currentChunk != -1) {
						
						Utils.saveChangesToFile(renderChunks.get(currentChunk));
						
						renderChunks.get(currentChunk).position.x = ((playerChunkX - viewDistanceDivide) + i) * chunkWidth;
						renderChunks.get(currentChunk).position.y = (playerChunkZ - viewDistanceDivide) * chunkWidth;
						renderChunks.get(currentChunk).needsRegenerated = true;
						renderChunks.get(currentChunk).changes.clear();
						
					}
				}
			}
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
		lastPlayerChunkX = playerChunkX;
		lastPlayerChunkZ = playerChunkZ;
		
	}
	
	static class ChunkThread implements Runnable{

		private Chunk chunk;
		
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
				
					for (int i = 0; i < buildChunks.size(); i++) {
						
						chunk = buildChunks.get(i).clone();
						
						if (!chunk.needsRebuilt) {
							fillChunk();
						}
						
						buildChunk();
						
						if (chunk.needsRebuilt || chunk.needsRegenerated) {
							//renderChunks.get(getChunkListLocation(chunk.position.x, chunk.position.y, renderChunks)).needsCleaned = true;
							//renderChunks.remove(getChunkListLocation(chunk.position.x, chunk.position.y, renderChunks));
							chunk.wasRebuilt = true;
						}
						
						chunk.built = true;
						chunk.needsRebuilt = false;
						chunk.needsRegenerated = false;
						chunk.beingRebuilt = false;
						
						waitingChunks.add(chunk);
						buildChunks.remove(i);
						
						chunk = null;
					}
					
					
					
				}else {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
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
									
									/*if (Utils.ran.nextInt(100) == 0) {
										chunk.blocks[x][y][z].blockType = BlockType.COAL;
									}else if (Utils.ran.nextInt(2000) == 0) {
										chunk.blocks[x][y][z].blockType = BlockType.GOLD;
									}else if (Utils.ran.nextInt(4000) == 0) {
										chunk.blocks[x][y][z].blockType = BlockType.DIAMOND;
									}else {*/
										chunk.blocks[x][y][z].blockType = BlockType.STONE;
									//}
									
									
									chunk.blocks[x][y][z].isActive = true;
								}
								else if (y < height+50) {
									chunk.blocks[x][y][z].blockType = BlockType.DIRT;
									chunk.blocks[x][y][z].isActive = true;
								}else if (y > height+50 && y < height+51) {
									chunk.blocks[x][y][z].blockType = BlockType.GRASS;
									chunk.blocks[x][y][z].isActive = true;
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
					
					//System.out.println("Loaded chunk");
					
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
			
			for (int z = 0; z < chunkWidth; z++) {
				for (int x = 0; x < chunkWidth; x++) {
					for (int y = 0; y < chunkHeight; y++) {
						
						if (chunk.blocks[x][y][z].isActive) {
								
								addBlock(x, y, z);
							
						
						}
						
						
					}	
				}
			}
			
			chunk.vertices = Utils.convertListToArray(tempVertices);
			chunk.normals = Utils.convertListToArray(tempVertices);
			chunk.texCoords = Utils.convertListToArray(tempTexCoords);
			
			tempVertices.clear();
			tempNormals.clear();
			tempTexCoords.clear();
			
		}
		
		public void addBlock(float x, float y, float z) {
			
			
						
						blockType = chunk.blocks[(int) x][(int) y][(int) z].blockType;
						
						
						//Bottom
						
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
						}else {
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
						}else {
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
						}else {
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
						}else {
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
