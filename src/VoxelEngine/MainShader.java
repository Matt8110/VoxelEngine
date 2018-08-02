package VoxelEngine;

import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector3f;

import Core.Shader;
import Utils.Utils;

public class MainShader extends Shader{

	private int projectionMatrixPosition;
	private int viewMatrixPosition;
	private int transformationMatrixPosition;
	
	public MainShader(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader);
		
		projectionMatrixPosition = super.getUniformLocation("projectionMatrix");
		viewMatrixPosition = super.getUniformLocation("viewMatrix");
		transformationMatrixPosition = super.getUniformLocation("transformationMatrix");
		
		useShader();
		super.setMatrix4(projectionMatrixPosition, Utils.getProjectionMatrix(Main.fov, 0.1f, 1000));
		
	}
	
	public void useShader() {
		
		GL20.glUseProgram(programID);
		
		super.setMatrix4(viewMatrixPosition, Utils.getViewMatrix());
		
		//Temporary / Fallback
		super.setMatrix4(transformationMatrixPosition, Utils.getTransformtionMatrix(new Vector3f(), new Vector3f(), 1.0f));
		
	}

}
