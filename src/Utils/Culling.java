package Utils;

import org.lwjgl.util.vector.Matrix4f;

public class Culling {
	
	private static int A = 0;
	private static int B = 1;
	private static int C = 2;
	private static int D = 3;
	
	private static float length;
	private static float distance;
	private static float[] rightPlane;
	private static float[] leftPlane;
	private static float[] bottomPlane;
	private static float[] topPlane;
	private static float[] nearPlane;
	private static float[] farPlane;
	
	public static boolean checkVisible(Matrix4f mat, float xPos, float yPos, float zPos, float radius)
	{
	 
		leftPlane = new float[4];
		leftPlane[A] = mat.m03  + mat.m00;
		leftPlane[B] = mat.m13  + mat.m10;
		leftPlane[C] = mat.m23 + mat.m20;
		leftPlane[D] = mat.m33 + mat.m30;
	 
		length = (float) Math.sqrt(leftPlane[A] * leftPlane[A] + leftPlane[B] * leftPlane[B] + leftPlane[C] * leftPlane[C]);
		leftPlane[A] /= length;
		leftPlane[B] /= length;
		leftPlane[C] /= length;
		leftPlane[D] /= length;
	 
		distance = leftPlane[A] * xPos + leftPlane[B] * yPos + leftPlane[C] * zPos + leftPlane[D];
		if (distance <= -radius)
		{
			return false;
		}
		
		rightPlane = new float[4];
		rightPlane[A] = mat.m03  - mat.m00;
		rightPlane[B] = mat.m13  - mat.m10;
		rightPlane[C] = mat.m23 - mat.m20;
		rightPlane[D] = mat.m33 - mat.m30;
	 
		
		length = (float) Math.sqrt(rightPlane[A] * rightPlane[A] + rightPlane[B] * rightPlane[B] + rightPlane[C] * rightPlane[C]);
		rightPlane[A] /= length;
		rightPlane[B] /= length;
		rightPlane[C] /= length;
		rightPlane[D] /= length;
	 
		distance = rightPlane[A] * xPos + rightPlane[B] * yPos + rightPlane[C] * zPos + rightPlane[D];
		if (distance <= -radius)
		{
			return false;
		}
	 
		
		bottomPlane = new float[4];
		bottomPlane[A] = mat.m03  + mat.m01;
		bottomPlane[B] = mat.m13  + mat.m11;
		bottomPlane[C] = mat.m23 + mat.m21;
		bottomPlane[D] = mat.m33 + mat.m31;
	 
		
		length = (float) Math.sqrt(bottomPlane[A] * bottomPlane[A] + bottomPlane[B] * bottomPlane[B] + bottomPlane[C] * bottomPlane[C]);
		bottomPlane[A] /= length;
		bottomPlane[B] /= length;
		bottomPlane[C] /= length;
		bottomPlane[D] /= length;
	 
		distance = bottomPlane[A] * xPos + bottomPlane[B] * yPos + bottomPlane[C] * zPos + bottomPlane[D];
		if (distance <= -radius)
		{
			return false;
		}
	 
		
		topPlane = new float[4];
		topPlane[A] = mat.m03  - mat.m01;
		topPlane[B] = mat.m13  - mat.m11;
		topPlane[C] = mat.m23 - mat.m21;
		topPlane[D] = mat.m33 - mat.m31;
	 
		
		length = (float) Math.sqrt(topPlane[A] * topPlane[A] + topPlane[B] * topPlane[B] + topPlane[C] * topPlane[C]);
		topPlane[A] /= length;
		topPlane[B] /= length;
		topPlane[C] /= length;
		topPlane[D] /= length;
	 
		distance = topPlane[A] * xPos + topPlane[B] * yPos + topPlane[C] * zPos + topPlane[D];
		if (distance <= -radius)
		{
			return false;
		}
	 
		
		nearPlane = new float[4];
		nearPlane[A] = mat.m03  + mat.m02;
		nearPlane[B] = mat.m13  + mat.m12;
		nearPlane[C] = mat.m23 + mat.m22;
		nearPlane[D] = mat.m33 + mat.m32;
	 
		
		length = (float) Math.sqrt(nearPlane[A] * nearPlane[A] + nearPlane[B] * nearPlane[B] + nearPlane[C] * nearPlane[C]);
		nearPlane[A] /= length;
		nearPlane[B] /= length;
		nearPlane[C] /= length;
		nearPlane[D] /= length;
	 
		distance = nearPlane[A] * xPos + nearPlane[B] * yPos + nearPlane[C] * zPos + nearPlane[D];
		if (distance <= -radius)
		{
			return false;
		}
		
		farPlane = new float[4];
		farPlane[A] = mat.m03  - mat.m02;
		farPlane[B] = mat.m13  - mat.m12;
		farPlane[C] = mat.m23 - mat.m22;
		farPlane[D] = mat.m33 - mat.m32;
	 
		
		length = (float) Math.sqrt(farPlane[A] * farPlane[A] + farPlane[B] * farPlane[B] + farPlane[C] * farPlane[C]);
		farPlane[A] /= length;
		farPlane[B] /= length;
		farPlane[C] /= length;
		farPlane[D] /= length;
	 
		distance = farPlane[A] * xPos + farPlane[B] * yPos + farPlane[C] * zPos + farPlane[D];
		if (distance <= -radius)
		{
			return false;
		}
	 
		
		return true;
	}
	
}
