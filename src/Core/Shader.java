package Core;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.Scanner;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class Shader {

	protected int programID;
	private int vertexID;
	private int fragmentID;
	private String vertexSource = "", fragmentSource = "";
	
	public Shader(String vertexShader, String fragmentShader) {
		
		try {
			
			Scanner scan = new Scanner(new File(vertexShader));
			
			while (scan.hasNextLine()) {
				vertexSource += scan.nextLine() + "\n";
			}
			
			scan.close();
			
			scan = new Scanner(new File(fragmentShader));
			
			while (scan.hasNextLine()) {
				fragmentSource += scan.nextLine() + "\n";
			}
			
			scan.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		vertexID = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		fragmentID = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
		
		GL20.glShaderSource(vertexID, vertexSource);
		GL20.glShaderSource(fragmentID, fragmentSource);
		
		GL20.glCompileShader(vertexID);
		GL20.glCompileShader(fragmentID);
		
		int result = GL20.glGetShaderi(vertexID, GL20.GL_COMPILE_STATUS);
		if (result == 0) {
			System.err.println("Vertex Shader");
			System.err.println(GL20.glGetShaderInfoLog(vertexID, 500));
		}
		result = GL20.glGetShaderi(fragmentID, GL20.GL_COMPILE_STATUS);
		if (result == 0) {
			System.err.println("Fragment Shader");
			System.err.println(GL20.glGetShaderInfoLog(fragmentID, 500));
		}
		
		
		programID = GL20.glCreateProgram();
		
		GL20.glAttachShader(programID, vertexID);
		GL20.glAttachShader(programID, fragmentID);
		
		GL20.glLinkProgram(programID);
		
		vertexSource = null;
		fragmentSource = null;
		
	}
	
	public int getUniformLocation(String name) {
		return GL20.glGetUniformLocation(programID, name);
	}
	
	public void setVector3(int id, Vector3f vec) {
		
		GL20.glUniform3f(id, vec.x, vec.y, vec.z);
		
	}
	
	public void setMatrix4(int id, Matrix4f mat) {
		
		FloatBuffer buf = BufferUtils.createFloatBuffer(4*4);
		mat.store(buf);
		buf.flip();
		
		GL20.glUniformMatrix4(id, false, buf);
		
		buf.clear();
		buf = null;
		
		
	}
	
	public void useShader() {
		
		GL20.glUseProgram(programID);
		
	}
	
}
