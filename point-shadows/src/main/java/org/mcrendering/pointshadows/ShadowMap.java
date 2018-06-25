package org.mcrendering.pointshadows;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL42.*;

public class ShadowMap {

    public static final int SHADOW_LENGTH = 1024;

    private final int depthMapFBO;

    private final int shadow_tex;

    public ShadowMap() throws Exception {

    	// cf shadowmapping.cpp from sb7code
    	
        // Create a FBO to render the depth map
        depthMapFBO = glGenFramebuffers();

        // Create the depth map texture
        this.shadow_tex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.shadow_tex);
        glTexStorage2D(GL_TEXTURE_2D, 11, GL_DEPTH_COMPONENT32F, SHADOW_LENGTH, SHADOW_LENGTH);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

        // Attach the the depth map texture to the FBO
        glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, this.shadow_tex, 0);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new Exception("Could not create FrameBuffer");
        }
        
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public int getDepthMapFBO() {
        return depthMapFBO;
    }

    public int getDepthMapTexture() {
    	return shadow_tex;
    }
    
    public void cleanup() {
        glDeleteFramebuffers(depthMapFBO);
        glDeleteTextures(shadow_tex);
    }
}
