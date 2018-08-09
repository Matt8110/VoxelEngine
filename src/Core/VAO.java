package Core;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import Utils.Utils;

public class VAO {

	public int vaoID;
	public int vertexCount;
	private List<Integer> VBOs = new ArrayList<Integer>();
	
	public VAO(float[] vertices, float[] normals, float[] texCoords) {
		
		putDataInVAO(vertices, normals, texCoords);
		
	}
	
	public VAO(float[] vertices, float[] texCoords) {
		
		putDataInVAO(vertices, texCoords);
		
	}
	
	public VAO() {
		
		
	}
	
	public void putDataInVAO(float[] vertices, float[] normals, float[] texCoords) {
		
		vertexCount = vertices.length/3;
		
		vaoID = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoID);
		
		putDataInVBO(vertices, 0, 3);
		putDataInVBO(normals, 1, 3);
		putDataInVBO(texCoords, 2, 2);
		
		GL30.glBindVertexArray(0);
		
	}
	
	public void putDataInVAO(float[] vertices, float[] texCoords) {
		
		vertexCount = vertices.length/2;
		
		vaoID = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoID);
		
		putDataInVBO(vertices, 0, 2);
		putDataInVBO(texCoords, 1, 2);
		
		GL30.glBindVertexArray(0);
		
	}
	
	public void modifyVAO(float[] vertices, float[] normals, float[] texCoords) {
		
		vertexCount = vertices.length/3;
		
		GL30.glBindVertexArray(vaoID);
		
		modifyVBO(vertices, 0);
		modifyVBO(normals, 1);
		modifyVBO(texCoords, 2);
		
		GL30.glBindVertexArray(0);
		
	}
	
	public void modifyVAO(float[] vertices, float[] texCoords) {
		
		vertexCount = vertices.length/2;
		
		GL30.glBindVertexArray(vaoID);
		
		modifyVBO(vertices, 0);
		modifyVBO(texCoords, 1);
		
		GL30.glBindVertexArray(0);
		
	}
	
	private void modifyVBO(float[] data, int id) {
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBOs.get(id));
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, Utils.asFloatBuffer(data), GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
	}
	
	private void putDataInVBO(float[] data, int id, int size) {
		
		int vboID = GL15.glGenBuffers();
		VBOs.add(vboID);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, Utils.asFloatBuffer(data), GL15.GL_DYNAMIC_DRAW);
		GL20.glVertexAttribPointer(id, size, GL11.GL_FLOAT, false, 0, 0);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
	}
	
	public void render() {
		
		GL30.glBindVertexArray(vaoID);
		
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
		
		GL30.glBindVertexArray(0);
		
	}
	
	public void render2D() {
		
		GL30.glBindVertexArray(vaoID);
		
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
		
		GL30.glBindVertexArray(0);
		
	}
	
	public void cleanup() {
		
		GL30.glBindVertexArray(vaoID);
		
		for (int id : VBOs) {
			GL15.glDeleteBuffers(id);
		}
		
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(vaoID);
		
		VBOs.clear();
		
	}
	
}
