package Blocks;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import Core.VAO;
import Utils.Culling;
import VoxelEngine.Camera;
import VoxelEngine.Main;

public class Chunk implements Cloneable{

	public Vector2f position = new Vector2f();
	public int chunkWidth = ChunkManager.chunkWidth;
	public int chunkHeight = ChunkManager.chunkHeight;
	public volatile Block[][][] blocks = new Block[chunkWidth][chunkHeight][chunkWidth];
	public Map<String, String> changes = new HashMap<String, String>();
	
	public boolean built = false;
	public boolean transparentBuilt = false;
	public boolean needsRebuilt = false;
	public boolean needsRegenerated = false;
	public boolean beingRebuilt = false;
	public boolean filled = false;
	
	public float[] vertices;
	public float[] normals;
	public float[] texCoords;
	
	public float[] transparentVertices;
	public float[] transparentNormals;
	public float[] transparentTexCoords;
	
	public float averageBlockHeight = 0;
	
	private VAO vao, transparentVao;
	
	public Chunk(float x, float z) {
		
		position.x = x*chunkWidth;
		position.y = z*chunkWidth;
		
	}
	
	public void cleanup() {
		vao.cleanup();
	}
	
	public void renderAndUpdate() {
		
		//If the chunk building thread is finished, send the data to the VAO
		if (built) {
			if (vao != null) {
				vao.modifyVAO(vertices, normals, texCoords);
			}else {
				vao = new VAO(vertices, normals, texCoords);
			}
			
			vertices = null;
			normals = null;
			texCoords = null;
			
			built = false;
		}
		
		if (vao != null) {
			
			
			if (Culling.checkVisible(Camera.MVP, position.x+8, 128, position.y+8, 16) ||
				Culling.checkVisible(Camera.MVP, position.x+8, 64, position.y+8, 16) ||
				Culling.checkVisible(Camera.MVP, position.x+8, Camera.position.y, position.y+8, 16) ||
				Culling.checkVisible(Camera.MVP, position.x+8, 0, position.y+8, 16)) {
				
				vao.render();
				Main.renderCalls++;
				
			}
		}
		
	}
	
	public void renderAndUpdateTransparent() {
		
		//If the chunk building thread is finished, send the data to the VAO
		
		if (transparentBuilt) {
			
			if (transparentVao != null) {
				transparentVao.modifyVAO(transparentVertices, transparentNormals, transparentTexCoords);
			}else {
				transparentVao = new VAO(transparentVertices, transparentNormals, transparentTexCoords);
			}
			
			transparentVertices = null;
			transparentNormals = null;
			transparentTexCoords = null;
			
			transparentBuilt = false;
		}
		
		if (transparentVao != null) {
			
			if (transparentVao.vertexCount > 0)
				if (Culling.checkVisible(Camera.MVP, position.x+8, 64, position.y+8, 16) ||
					Culling.checkVisible(Camera.MVP, position.x+8, 128, position.y+8, 16) ||
					Culling.checkVisible(Camera.MVP, position.x+8, Camera.position.y, position.y+8, 16) ||
					Culling.checkVisible(Camera.MVP, position.x+8, 0, position.y+8, 16)) {
					
					transparentVao.render();
					
				}
		}
		
	}
	
	public Chunk clone() {
		
		try {
			return (Chunk) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	
}
