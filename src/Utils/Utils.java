package Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.util.Formatter;
import java.util.List;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import Blocks.Chunk;
import VoxelEngine.Camera;

public class Utils {

	private static Matrix4f mat;
	
	public static Random ran = new Random();
	private static float[] tempArray;
	private static int randomNumber;
	
	private static Formatter format;
	
	public static FloatBuffer asFloatBuffer(float[] data) {
		
		FloatBuffer buf = BufferUtils.createFloatBuffer(data.length);
		buf.put(data);
		buf.flip();
		
		return buf;
		
	}
	
	public static void saveChangesToFile(Chunk chunk) {
		
		try {
			
			if (!chunk.changes.isEmpty()) {
			
				format = new Formatter(new File("chunks/" + (int)chunk.position.x + "" + (int)chunk.position.y + ".chunk"));
				
				for (String str : chunk.changes.values()) {
					format.format(str);
				}
				
				format.close();
			
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public static Matrix4f getProjectionMatrix(int fov, float NEAR_PLANE, float FAR_PLANE) {
		
		mat = new Matrix4f();
		
		float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
        float y_scale = (float) ((1f / Math.tan(Math.toRadians(fov/2f))));
        float x_scale = y_scale/aspectRatio;
        float frustum_length = FAR_PLANE - NEAR_PLANE;
        
        
        mat.m00 = x_scale;
        mat.m11 = y_scale;
        mat.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
        mat.m23 = -1;
        mat.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
        mat.m33 = 0;
        
        
		return mat;
		
	}
	
	public static Matrix4f getTransformtionMatrix(Vector3f position, Vector3f rotation, float scale) {
		
		mat = new Matrix4f();
		mat.setIdentity();
		
		mat.translate(position);
		mat.rotate((float)Math.toRadians(rotation.x), new Vector3f(1, 0, 0));
		mat.rotate((float)Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
		mat.rotate((float)Math.toRadians(rotation.z), new Vector3f(0, 0, 1));
		mat.scale(new Vector3f(scale, scale, scale));
		
		return mat;
		
	}
	
	public static Matrix4f getViewMatrix() {
		
		mat = new Matrix4f();
		mat.setIdentity();
		
		mat.rotate((float)Math.toRadians(Camera.rotation.x), new Vector3f(1, 0, 0));
		mat.rotate((float)Math.toRadians(Camera.rotation.y), new Vector3f(0, 1, 0));
		mat.rotate((float)Math.toRadians(Camera.rotation.z), new Vector3f(0, 0, 1));
		mat.translate(new Vector3f(-Camera.position.x, -Camera.position.y, -Camera.position.z));
		
		return mat;
		
	}
	
	public static int loadTexture(String texture) {
		
		try {
			
			Texture tex = TextureLoader.getTexture("PNG", new FileInputStream(new File(texture)), GL11.GL_NEAREST);
			
			return tex.getTextureID();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return -1;
		
	}
	
	public static float[] convertListToArray(List<Float> data) {
		
		tempArray = new float[data.size()];
		
		for (int i = 0; i < data.size(); i++) {
			tempArray[i] = data.get(i);
		}
		
		return tempArray;
		
	}
	
	public static float Pick(float... thing) {
		
		randomNumber = ran.nextInt(thing.length);
		return thing[randomNumber];
		
	}
	
	public static String Pick(String... thing) {
		
		randomNumber = ran.nextInt(thing.length);
		return thing[randomNumber];
		
	}
	
}
