package Utils;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

public class FBO {

	private int fboID;
	private int textureID, depthBufferTextureID;
	public int width, height;
	
	public FBO(int width, int height) {
		
		this.width = width;
		this.height = height;
		
		fboID = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboID);
		GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
		
		//GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		
		
	}
	
	public void addDepthAttachment() {
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboID);
		
		int depthBuffer = GL30.glGenRenderbuffers();
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
		GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, width, height);
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		
	}
	
	public void addDepthTextureAttachment() {
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboID);
		
		depthBufferTextureID = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthBufferTextureID);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT16, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, depthBufferTextureID, 0);
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		
	}
	
	public void addTextureAttachment() {
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboID);
		
		textureID = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, textureID, 0);
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		
	}
	
	public void bindFBO() {
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboID);
		GL11.glViewport(0, 0, width, height);
		
	}
	
	public void unbindFBO() {
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
		
	}
	
	public int getTexture() {
		
		return textureID;
		
	}
	
	public int getDepthBufferTexture() {
		
		return depthBufferTextureID;
		
	}
	
}
