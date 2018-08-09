package Blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import Core.VAO;

public class Chunk implements Cloneable{

	public Vector2f position = new Vector2f();
	public int chunkWidth = ChunkManager.chunkWidth;
	public int chunkHeight = ChunkManager.chunkHeight;
	public volatile Block[][][] blocks = new Block[chunkWidth][chunkHeight][chunkWidth];
	public Map<String, String> changes = new HashMap<String, String>();
	
	public boolean built = false;
	public boolean needsRebuilt = false;
	public boolean beingRebuilt = false;
	public boolean needsRegenerated = false;
	public boolean needsCleaned = false;
	
	public float[] vertices;
	public float[] normals;
	public float[] texCoords;
	
	private VAO vao;
	
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
				//System.out.println(vertices.length);
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
			vao.render();
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
