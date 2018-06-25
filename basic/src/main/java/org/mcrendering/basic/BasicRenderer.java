package org.mcrendering.basic;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import java.io.InputStream;

import org.joml.Matrix4f;
import org.mcrendering.common.Camera;
import org.mcrendering.common.GameEngine;
import org.mcrendering.common.IRenderer;
import org.mcrendering.common.MouseInput;
import org.mcrendering.common.ShaderProgram;
import org.mcrendering.common.Utils;
import org.mcrendering.common.Window;
import org.mcrendering.schematicreader.SchematicReader;
import org.mcrendering.schematicreader.World;

public class BasicRenderer implements IRenderer {

    private World world;

    private ShaderProgram sceneShaderProgram;
    
    private Window window;
    private Camera camera;

    @Override
    public void initApplication(Window window, Camera camera) throws Exception {
        
    	this.window = window;
    	this.camera = camera;
    	
        try (   InputStream fis = Class.forName(SchematicReader.class.getName()).getResourceAsStream("/models/model.schematic");) {
        	this.world = new SchematicReader().read(fis);
        }

        this.world.init();
        
        camera.getPosition().x = 5f;
        camera.getPosition().y = 5f;
        camera.getPosition().z = 9f;
    }

    @Override
    public void initRendering() throws Exception {
        // Create shader
        sceneShaderProgram = new ShaderProgram();
        sceneShaderProgram.createVertexShader(Utils.loadResource("/shaders/scene_vertex.vs"));
        sceneShaderProgram.createFragmentShader(Utils.loadResource("/shaders/scene_fragment.fs"));
        sceneShaderProgram.link();

        // Create uniforms for view and projection matrices
        sceneShaderProgram.createUniform("viewMatrix");
        sceneShaderProgram.createUniform("projectionMatrix");
        sceneShaderProgram.createUniform("texture_sampler");
    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 
        glViewport(0, 0, window.getWidth(), window.getHeight());
        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        window.updateProjectionMatrix();

        sceneShaderProgram.bind();

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        sceneShaderProgram.setUniform("viewMatrix", viewMatrix);
        sceneShaderProgram.setUniform("projectionMatrix", projectionMatrix);
        sceneShaderProgram.setUniform("texture_sampler", 0);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, world.getTextureId());
       
        world.render();

        sceneShaderProgram.unbind();
    }

    @Override
    public void cleanupRendering() {
        if (sceneShaderProgram != null) {
            sceneShaderProgram.cleanup();
        }
    }

    @Override
    public void input(MouseInput mouseInput) {
    }

    @Override
    public void cleanupApplication() {
        world.cleanUp();
    }
    
    public static void main(String[] args) {
        try {
            new GameEngine("GAME", new BasicRenderer()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
