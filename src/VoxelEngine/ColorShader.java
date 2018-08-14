package VoxelEngine;

import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector3f;

import Core.Shader;
import Utils.Utils;

public class ColorShader extends Shader{

	private int projectionMatrixPosition;
	private int viewMatrixPosition;
	private int transformationMatrixPosition;
	private Vector3f position = new Vector3f();
	private Vector3f rotation = new Vector3f();
	
	public ColorShader(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader);
		
		projectionMatrixPosition = super.getUniformLocation("projectionMatrix");
		viewMatrixPosition = super.getUniformLocation("viewMatrix");
		transformationMatrixPosition = super.getUniformLocation("transformationMatrix");
		
		useShader();
		super.setMatrix4(projectionMatrixPosition, Utils.getProjectionMatrix(Main.fov, Main.near, Main.far));
		
	}
	
	public void resetProjectionMatrix() {
		super.setMatrix4(projectionMatrixPosition, Utils.getProjectionMatrix(Main.fov, Main.near, Main.far));
	}
	
	public void useShader() {
		
		GL20.glUseProgram(programID);
		
		super.setMatrix4(viewMatrixPosition, Utils.getViewMatrix());
		
	}
	
	public void setTransformation(float x, float y, float z, float scale) {
		
		position.x = x;
		position.y = y;
		position.z = z;
		
		super.setMatrix4(transformationMatrixPosition, Utils.getTransformtionMatrix(position, rotation, scale));
		
	}

}
