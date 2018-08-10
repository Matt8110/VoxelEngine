package Utils;

import org.lwjgl.util.vector.Vector3f;
import Blocks.Block;
import Blocks.ChunkManager;

public class Ray {

	private Vector3f direction = new Vector3f();
	private int checkX, checkY, checkZ;
	private float checkXOrig, checkYOrig, checkZOrig;
	private float dirX, dirY, dirZ;
	private int chunkX, chunkZ;
	private int chunkToCheck;
	private Block block;
	
	public int getChunk() {
		return chunkToCheck;
	}
	
	public Block getBlock() {
		return block;
	}
	
	public Vector3f getOriginalRayIntersection() {
		return new Vector3f(dirX, dirY, dirZ);
	}
	
	public Vector3f getOriginalBlockPosition() {
		return new Vector3f(checkXOrig, checkYOrig, checkZOrig);
	}
	
	public Vector3f getBlockPosition() {
		return new Vector3f(checkX, checkY, checkZ);
	}
	
	public float castHeightRay(float x, float y, float z, float rotX, float rotY, float maxDistance) {
		
		direction.x = (float) Math.sin(Math.toRadians(rotY));
		direction.y = (float) -Math.tan(Math.toRadians(rotX));
		direction.z = (float) -Math.cos(Math.toRadians(rotY));
		
		direction.normalise();
		
		for (float i = 0; i < maxDistance; i += 0.5f) {
			
			dirX = direction.x * i;
			dirY = direction.y * i;
			dirZ = direction.z * i;
			
			chunkX = (int) Math.floor((x + dirX) / ChunkManager.chunkWidth);
			chunkZ = (int) Math.floor((z + dirZ) / ChunkManager.chunkWidth);
			
			checkX = (int) Math.floor((x + dirX) - chunkX * ChunkManager.chunkWidth);
			checkY = (int) Math.floor((y + dirY));
			checkZ = (int) Math.floor((z + dirZ) - chunkZ * ChunkManager.chunkWidth);
			
			//System.out.println(checkX + " " + checkY + " " + checkZ);
			
			chunkToCheck = ChunkManager.getChunkListLocation(chunkX * ChunkManager.chunkWidth, chunkZ * ChunkManager.chunkWidth, ChunkManager.renderChunks);
			
			//System.out.println(checkY);
			
			if (chunkToCheck != -1) {
				
				if (ChunkManager.renderChunks.size() >= chunkToCheck && checkX < ChunkManager.chunkWidth && checkZ < ChunkManager.chunkWidth && checkY < ChunkManager.chunkHeight)
					block = ChunkManager.renderChunks.get(chunkToCheck).blocks[checkX][checkY][checkZ];
				
				if (block.isActive) {
					return y - checkY;
				}
				
			}else {
					return -1;
				}
			
			
			
		}
		
		return 10000;
		
	}
	
	public Vector3f castRayIntoBlocks(float x, float y, float z, float rotX, float rotY, float maxDistance) {
		
		direction.x = (float) Math.sin(Math.toRadians(rotY));
		direction.y = (float) -Math.tan(Math.toRadians(rotX));
		direction.z = (float) -Math.cos(Math.toRadians(rotY));
		
		direction.normalise();
		
		for (float i = 0; i < maxDistance; i += 0.01f) {
			
			dirX = direction.x * i;
			dirY = direction.y * i;
			dirZ = direction.z * i;
			
			chunkX = (int) Math.floor((x + dirX) / ChunkManager.chunkWidth);
			chunkZ = (int) Math.floor((z + dirZ) / ChunkManager.chunkWidth);
			
			checkXOrig = (x + dirX) - chunkX * ChunkManager.chunkWidth;
			checkYOrig = (y + dirY);
			checkZOrig = (z + dirZ) - chunkZ * ChunkManager.chunkWidth;
			
			checkX = (int) Math.floor((x + dirX) - chunkX * ChunkManager.chunkWidth);
			checkY = (int) Math.floor((y + dirY));
			checkZ = (int) Math.floor((z + dirZ) - chunkZ * ChunkManager.chunkWidth);
			
			if (checkY >= 128)
				return null;
			
			//System.out.println(checkX + " " + checkY + " " + checkZ);
			
			chunkToCheck = ChunkManager.getChunkListLocation(chunkX * ChunkManager.chunkWidth, chunkZ * ChunkManager.chunkWidth, ChunkManager.renderChunks);
			
			if (chunkToCheck != -1) {
				
				block = ChunkManager.renderChunks.get(chunkToCheck).blocks[checkX][checkY][checkZ];
				
				if (block.isActive) {
					
					return new Vector3f(checkX, checkY, checkZ);
				}
				
			}
			
		}
		
		
		return null;
		
	}
	
}
