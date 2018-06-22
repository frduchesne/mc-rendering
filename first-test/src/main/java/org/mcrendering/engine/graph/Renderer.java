package org.mcrendering.engine.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.mcrendering.engine.Scene;
import org.mcrendering.engine.SceneLight;
import org.mcrendering.engine.Utils;
import org.mcrendering.engine.Window;
import org.mcrendering.engine.graph.lights.DirectionalLight;
import org.mcrendering.engine.graph.lights.PointLight;
import org.mcrendering.engine.items.GameItem;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    private static final int MAX_POINT_LIGHTS = 5;

    private ShadowMap shadowMap;

    private ShaderProgram depthShaderProgram;

    private ShaderProgram sceneShaderProgram;

    private ShaderProgram particlesShaderProgram;

    private final float specularPower;

    private final List<GameItem> filteredItems;

    public Renderer() {
        specularPower = 10f;
        filteredItems = new ArrayList<>();
    }

    public void init(Window window) throws Exception {
        shadowMap = new ShadowMap();

        setupDepthShader();
        setupSceneShader();
    }

    public void render(Window window, Camera camera, Scene scene, boolean sceneChanged) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    	
        // Render depth map before view ports has been set up
        renderDepthMap(window, scene);
 
        glViewport(0, 0, window.getWidth(), window.getHeight());

        // Update projection matrix once per render cycle
        window.updateProjectionMatrix();

        renderScene(window, camera, scene);
    }

    private void setupDepthShader() throws Exception {
        depthShaderProgram = new ShaderProgram();
        depthShaderProgram.createVertexShader(Utils.loadResource("/shaders/depth_vertex.vs"));
        depthShaderProgram.createGeometryShader(Utils.loadResource("/shaders/depth_geometry.gs"));
        depthShaderProgram.createFragmentShader(Utils.loadResource("/shaders/depth_fragment.fs"));
        depthShaderProgram.link();  
        
       depthShaderProgram.createPointLightShadowUniform();
      
        glEnable(GL_CLIP_PLANE0);
        glEnable(GL_CLIP_PLANE1);
        glEnable(GL_CLIP_PLANE2);
        
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
        sceneShaderProgram.createUniform("normalMap");
        // Create uniform for material
        sceneShaderProgram.createMaterialUniform("material");
        // Create lighting related uniforms
        sceneShaderProgram.createUniform("specularPower");
        sceneShaderProgram.createUniform("ambientLight");
        sceneShaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
        sceneShaderProgram.createDirectionalLightUniform("directionalLight");

        sceneShaderProgram.createUniform("numCols");
        sceneShaderProgram.createUniform("numRows");
        
        // shadow
        sceneShaderProgram.createUniform("shadowMap");
    }

    private void renderDepthMap(Window window, Scene scene) {
        glBindFramebuffer(GL_FRAMEBUFFER, shadowMap.getDepthMapFBO());
        glViewport(0, 0, ShadowMap.SHADOW_LENGTH, ShadowMap.SHADOW_LENGTH);
        glEnable(GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(3.4f, 0.0f);

        glClear(GL_DEPTH_BUFFER_BIT);

        depthShaderProgram.bind();
        
        TileMap tileMap = new TileMap();
        for (PointLight pointLight : scene.getSceneLight().getPointLightList()) {
        	pointLight.updateTile(tileMap);
            depthShaderProgram.setUniform(pointLight.getMPosition(), pointLight.getShadowMatrices());
            Map<InstancedMesh, List<GameItem>> mapMeshes = scene.getGameInstancedMeshes();
            for (InstancedMesh mesh : mapMeshes.keySet()) {

                filteredItems.clear();
                for (GameItem gameItem : mapMeshes.get(mesh)) {
                	filteredItems.add(gameItem);            }

                mesh.renderListInstanced(filteredItems);
            }
        }
        
        // Unbind
        depthShaderProgram.unbind();
        glDisable(GL_POLYGON_OFFSET_FILL);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    
    private void renderScene(Window window, Camera camera, Scene scene) {
        sceneShaderProgram.bind();

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        sceneShaderProgram.setUniform("viewMatrix", viewMatrix);
        sceneShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        SceneLight sceneLight = scene.getSceneLight();
        renderLights(viewMatrix, sceneLight);

        sceneShaderProgram.setUniform("texture_sampler", 0);
        sceneShaderProgram.setUniform("shadowMap", 2);
        
    	// Activate second texture bank
        glActiveTexture(GL_TEXTURE2);
        // Bind the texture
        glBindTexture(GL_TEXTURE_2D, shadowMap.getDepthMapTexture());
        renderInstancedMeshes(scene);

        sceneShaderProgram.unbind();
    }

    private void renderInstancedMeshes(Scene scene) {

        // Render each mesh with the associated game Items
        Map<InstancedMesh, List<GameItem>> mapMeshes = scene.getGameInstancedMeshes();
        for (InstancedMesh mesh : mapMeshes.keySet()) {
            Texture text = mesh.getMaterial().getTexture();
            if (text != null) {
                sceneShaderProgram.setUniform("numCols", text.getNumCols());
                sceneShaderProgram.setUniform("numRows", text.getNumRows());
            }

            sceneShaderProgram.setUniform("material", mesh.getMaterial());

            filteredItems.clear();
            for (GameItem gameItem : mapMeshes.get(mesh)) {
            	filteredItems.add(gameItem);            }

            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, shadowMap.getDepthMapTexture());
            
            mesh.renderListInstanced(filteredItems);
        }
    }

    private void renderLights(Matrix4f viewMatrix, SceneLight sceneLight) {

        sceneShaderProgram.setUniform("ambientLight", sceneLight.getAmbientLight());
        sceneShaderProgram.setUniform("specularPower", specularPower);

        // Process Point Lights
        PointLight[] pointLightList = sceneLight.getPointLightList();
        int numLights = pointLightList != null ? pointLightList.length : 0;
        for (int i = 0; i < numLights; i++) {
            PointLight currPointLight = pointLightList[i];
            currPointLight.updateViewMatrix(viewMatrix);
            sceneShaderProgram.setUniform("pointLights", currPointLight, i);
        }

        // Get a copy of the directional light object and transform its position to view coordinates
        DirectionalLight currDirLight = new DirectionalLight(sceneLight.getDirectionalLight());
        Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        sceneShaderProgram.setUniform("directionalLight", currDirLight);
    }

    public void cleanup() {
        if (sceneShaderProgram != null) {
            sceneShaderProgram.cleanup();
        }
        if (particlesShaderProgram != null) {
            particlesShaderProgram.cleanup();
        }
        if (depthShaderProgram != null) {
            depthShaderProgram.cleanup();
        }
    }
}
