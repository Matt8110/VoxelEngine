package Blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import Gui.Texture2D;
import Utils.Utils;
import VoxelEngine.Camera;
import VoxelEngine.Main;
import net.jlibnoise.generator.Perlin;

public class ChunkManager {

	
	public static int viewDistance = 15;
	public static int viewDistanceDivide = viewDistance/2;
	
	public static List<Chunk> renderChunks = Collections.synchronizedList(new ArrayList<Chunk>());
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
		
		
		
		for (int i = 0; i < renderChunks.size(); i++) {
			
			renderChunks.get(i).renderAndUpdate();
			
			if ((renderChunks.get(i).needsRebuilt || renderChunks.get(i).needsRegenerated) && !renderChunks.get(i).beingRebuilt) {
				renderChunks.get(i).beingRebuilt = true;
				buildChunks.add(renderChunks.get(i));
				//renderChunks.remove(i);
			}
			
		}
		
	}

	public static int getChunkListLocation(float x, float z, List<Chunk> list){
		
		for (int i = 0; i < list.size(); i++) {
			
			if (list.get(i).position.x == x && list.get(i).position.y == z) {
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
					//buildChunks.add(new Chunk(playerChunkX + viewDistanceDivide, (playerChunkZ - viewDistanceDivide) + i));
					
					currentChunk = getChunkListLocation((lastPlayerChunkX - (viewDistanceDivide))*chunkWidth, ((lastPlayerChunkZ - viewDistanceDivide) + i)*chunkWidth, renderChunks);
					
					if (currentChunk != -1) {
						
						//System.out.println("WORKING");
						
						renderChunks.get(currentChunk).position.x = (playerChunkX + viewDistanceDivide) * chunkWidth;
						renderChunks.get(currentChunk).position.y = ((playerChunkZ - viewDistanceDivide) + i) * chunkWidth;
						renderChunks.get(currentChunk).needsRegenerated = true;
						
						//renderChunks.remove(currentChunk);
					}
				}
			}
			
			if (playerChunkX < lastPlayerChunkX) {
				for (int i = 0; i < viewDistance; i++) {
					//buildChunks.add(new Chunk(playerChunkX - viewDistanceDivide, (playerChunkZ - viewDistanceDivide) + i));
					
					currentChunk = getChunkListLocation((lastPlayerChunkX + (viewDistanceDivide))*chunkWidth, ((lastPlayerChunkZ - viewDistanceDivide) + i)*chunkWidth, renderChunks);
					
					if (currentChunk != -1) {
						renderChunks.get(currentChunk).position.x = (playerChunkX - viewDistanceDivide) * chunkWidth;
						renderChunks.get(currentChunk).position.y = ((playerChunkZ - viewDistanceDivide) + i) * chunkWidth;
						renderChunks.get(currentChunk).needsRegenerated = true;
					}
				}
			}
			
			if (playerChunkZ > lastPlayerChunkZ) {
				for (int i = 0; i < viewDistance; i++) {
					//buildChunks.add(new Chunk((playerChunkX - viewDistanceDivide) + i, playerChunkZ + viewDistanceDivide));
					
					currentChunk = getChunkListLocation(((lastPlayerChunkX - viewDistanceDivide) + i)*chunkWidth,  (lastPlayerChunkZ - (viewDistanceDivide))*chunkWidth, renderChunks );
					
					if (currentChunk != -1) {
						renderChunks.get(currentChunk).position.x = ((playerChunkX - viewDistanceDivide) + i) * chunkWidth;
						renderChunks.get(currentChunk).position.y = (playerChunkZ + viewDistanceDivide) * chunkWidth;
						renderChunks.get(currentChunk).needsRegenerated = true;
					}
				}
			}
			
			if (playerChunkZ < lastPlayerChunkZ) {
				
				for (int i = 0; i < viewDistance; i++) {
					//buildChunks.add(new Chunk((playerChunkX - viewDistanceDivide) + i, playerChunkZ - viewDistanceDivide));
					
					currentChunk = getChunkListLocation(((lastPlayerChunkX - viewDistanceDivide) + i)*chunkWidth,  (lastPlayerChunkZ + (viewDistanceDivide))*chunkWidth, renderChunks );
					
					if (currentChunk != -1) {
						renderChunks.get(currentChunk).position.x = ((playerChunkX - viewDistanceDivide) + i) * chunkWidth;
						renderChunks.get(currentChunk).position.y = (playerChunkZ - viewDistanceDivide) * chunkWidth;
						renderChunks.get(currentChunk).needsRegenerated = true;
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
						
						chunk = buildChunks.get(i);
						
						if (!chunk.needsRebuilt) {
							fillChunk();
						}
						
						buildChunk();
						
						
						
						if (chunk.needsRebuilt) {
							renderChunks.remove(getChunkListLocation(chunk.position.x, chunk.position.y, renderChunks));
						}
						
						chunk.built = true;
						chunk.needsRebuilt = false;
						chunk.needsRegenerated = false;
						chunk.beingRebuilt = false;
						
						renderChunks.add(chunk);
						buildChunks.remove(i);
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
						
						chunk.blocks[x][y][z] = new Block();
						
						if (y < height+50) {
							chunk.blocks[x][y][z].blockType = BlockType.DIRT;
							chunk.blocks[x][y][z].isActive = true;
						}else if (y > height+50 && y < height+51) {
							chunk.blocks[x][y][z].blockType = BlockType.GRASS;
							chunk.blocks[x][y][z].isActive = true;
						}

						
					}
				}
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
							if (!chunk.blocks[(int) x][(int) y-1][(int) z].isActive) {
						
								
								
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
							if (!chunk.blocks[(int) x][(int) y+1][(int) z].isActive) {
						
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
							if (!chunk.blocks[(int) x][(int) y][(int) z-1].isActive) {
						
						
						
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
							if (!chunk.blocks[(int) x][(int) y][(int) z+1].isActive) {
						
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
							if (!chunk.blocks[(int) x-1][(int) y][(int) z].isActive) {
						
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
							if (!chunk.blocks[(int) x+1][(int) y][(int) z].isActive) {
						
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
