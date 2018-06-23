package org.mcrendering.engine.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.joml.Matrix4f;
import org.mcrendering.engine.Scene;
import org.mcrendering.engine.Utils;
import org.mcrendering.engine.Window;
import org.mcrendering.engine.items.GameItem;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    private ShaderProgram sceneShaderProgram;

    private final List<GameItem> filteredItems;

    public Renderer() {
        filteredItems = new ArrayList<>();
    }

    public void init(Window window) throws Exception {
        setupSceneShader();
    }

    public void render(Window window, Camera camera, Scene scene, boolean sceneChanged) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 
        glViewport(0, 0, window.getWidth(), window.getHeight());

        // Update projection matrix once per render cycle
        window.updateProjectionMatrix();

        renderScene(window, camera, scene);
    }

    private void setupSceneShader() throws Exception {
        // Create shader
        sceneShaderProgram = new ShaderProgram();
        sceneShaderProgram.createVertexShader(Utils.loadResource("/shaders/scene_vertex.vs"));
        sceneShaderProgram.createFragmentShader(Utils.loadResource("/shaders/scene_fragment.fs"));
        sceneShaderProgram.link();

        // Create uniforms for view and projection matrices
        sceneShaderProgram.createUniform("viewMatrix");
        sceneShaderProgram.createUniform("projectionMatrix");
        sceneShaderProgram.createUniform("texture_sampler");
        // Create uniform for material
        sceneShaderProgram.createMaterialUniform("material");
    }
    
    private void renderScene(Window window, Camera camera, Scene scene) {
        sceneShaderProgram.bind();

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        sceneShaderProgram.setUniform("viewMatrix", viewMatrix);
        sceneShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        sceneShaderProgram.setUniform("texture_sampler", 0);
        
        renderInstancedMeshes(scene);

        sceneShaderProgram.unbind();
    }

    private void renderInstancedMeshes(Scene scene) {

        // Render each mesh with the associated game Items
        Map<InstancedMesh, List<GameItem>> mapMeshes = scene.getGameInstancedMeshes();
        for (InstancedMesh mesh : mapMeshes.keySet()) {

            sceneShaderProgram.setUniform("material", mesh.getMaterial());

            filteredItems.clear();
            for (GameItem gameItem : mapMeshes.get(mesh)) {
            	filteredItems.add(gameItem);            }
            
            mesh.renderListInstanced(filteredItems);
        }
    }

    public void cleanup() {
        if (sceneShaderProgram != null) {
            sceneShaderProgram.cleanup();
        }
    }
}
