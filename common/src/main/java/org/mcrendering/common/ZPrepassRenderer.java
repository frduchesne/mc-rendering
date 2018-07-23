package org.mcrendering.common;

import static org.lwjgl.opengl.GL11.GL_LESS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import org.joml.Matrix4f;
import org.mcrendering.schematicreader.World;

public class ZPrepassRenderer implements IRenderer {
    private ShaderProgram firstPassShaderProgram;
    private World world;
    private Window window;
    private Camera camera;

    public ZPrepassRenderer(Window window, Camera camera, World world) {
    	this.window = window;
    	this.camera = camera;
    	this.world = world;
    }
    
	@Override
	public void initApplication(Window window, Camera camera) throws Exception {
	}

	@Override
	public void initRendering() throws Exception {
    	firstPassShaderProgram = new ShaderProgram();
    	firstPassShaderProgram.createVertexShader(Utils.loadResource("/shaders/z_prepass.vs"));
    	firstPassShaderProgram.createFragmentShader(Utils.loadResource("/shaders/z_prepass.fs"));
    	firstPassShaderProgram.link();

    	firstPassShaderProgram.createUniform("viewMatrix");
    	firstPassShaderProgram.createUniform("projectionMatrix");
    	firstPassShaderProgram.createUniform("texture_sampler");
	}

	@Override
	public void render() {
    	glDepthFunc(GL_LESS);
        firstPassShaderProgram.bind();

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        firstPassShaderProgram.setUniform("viewMatrix", viewMatrix);
        firstPassShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        firstPassShaderProgram.setUniform("texture_sampler", 0);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, world.getTextureId());
        
        world.render(camera.getPosition());

        firstPassShaderProgram.unbind();
	}

	@Override
	public void input(MouseInput mouseInput) {
	}

	@Override
	public void cleanupRendering() {
        if (firstPassShaderProgram != null) {
        	firstPassShaderProgram.cleanup();
        }
	}

	@Override
	public void cleanupApplication() {
	}

}
