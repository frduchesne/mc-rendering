package org.mcrendering.pointshadows;

import static org.lwjgl.opengl.GL11.GL_CLIP_PLANE0;
import static org.lwjgl.opengl.GL11.GL_CLIP_PLANE1;
import static org.lwjgl.opengl.GL11.GL_CLIP_PLANE2;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_POLYGON_OFFSET_FILL;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPolygonOffset;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.mcrendering.common.Camera;
import org.mcrendering.common.IRenderer;
import org.mcrendering.common.MouseInput;
import org.mcrendering.common.ShaderProgram;
import org.mcrendering.common.Utils;
import org.mcrendering.common.Window;
import org.mcrendering.schematicreader.Block;
import org.mcrendering.schematicreader.SchematicReader;
import org.mcrendering.schematicreader.World;

/*
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.opengl.GL41.*;
import static org.lwjgl.opengl.GL42.*;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.opengl.GL44.*;
import static org.lwjgl.opengl.GL45.*;
 */

public class PointShadowsRenderer implements IRenderer {

    private static final int MAX_POINT_LIGHTS = 5;

    private World world;

    private ShadowMap shadowMap;

    private ShaderProgram depthShaderProgram;

    private ShaderProgram sceneShaderProgram;

    private Window window;
    private Camera camera;
    
    PointLight[] pointLights;

    @Override
    public void initApplication(Window window, Camera camera) throws Exception {
        
    	this.window = window;
    	this.camera = camera;
    	
    	shadowMap = new ShadowMap();
    	
        try (   InputStream fis = PointShadowsRenderer.class.getResourceAsStream("/models/model.schematic");) {
        	this.world = new SchematicReader().read(fis);
        }

        // find torches
        List<Vector3f> pointLightPositions = new ArrayList<>();
        for (Block block : world.getBlocks()) {
        	if ("normal_torch".equals(block.getType().getModel()) || "normal_torch_wall".equals(block.getType().getModel())) {
        		pointLightPositions.add(new Vector3f(block.getPosition().x, block.getPosition().y, block.getPosition().z));
        	}
        }
        pointLights = new PointLight[pointLightPositions.size()];
        for (int i = 0; i < pointLightPositions.size(); i++) {
        	pointLights[i] = new PointLight(
        			new Vector3f(210f/255f, 190f/255f, 156f/255f), 
        			pointLightPositions.get(i), 
        			20.0f,
        			new PointLight.Attenuation(1.0f, 0.35f, 0.44f));
        }

        
        
        this.world.init();
        
        camera.getPosition().x = 5f;
        camera.getPosition().y = 5f;
        camera.getPosition().z = 9f;
    }

    @Override
    public void initRendering() throws Exception {
        setupDepthShader();
        setupSceneShader();
    }

    private void setupDepthShader() throws Exception {
        depthShaderProgram = new ShaderProgram();
        depthShaderProgram.createVertexShader(Utils.loadResource("/shaders/depth_vertex.vs"));
        depthShaderProgram.createGeometryShader(Utils.loadResource("/shaders/depth_geometry.gs"));
        depthShaderProgram.createFragmentShader(Utils.loadResource("/shaders/depth_fragment.fs"));
        depthShaderProgram.link();  
        
       createPointLightShadowUniform(depthShaderProgram);
      
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
        // Create lighting related uniforms
        sceneShaderProgram.createUniform("specularPower");
        sceneShaderProgram.createUniform("ambientLight");
        createPointLightListUniform(sceneShaderProgram, "pointLights", MAX_POINT_LIGHTS);
        
        // shadow
        sceneShaderProgram.createUniform("shadowMap");
    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    	
        // Render depth map before view ports has been set up
        renderDepthMap();
 
        glViewport(0, 0, window.getWidth(), window.getHeight());

        // Update projection matrix once per render cycle
        window.updateProjectionMatrix();

        renderScene();
    }

    private void renderDepthMap() {
        glBindFramebuffer(GL_FRAMEBUFFER, shadowMap.getDepthMapFBO());
        glViewport(0, 0, ShadowMap.SHADOW_LENGTH, ShadowMap.SHADOW_LENGTH);
        glEnable(GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(3.4f, 0.0f);

        glClear(GL_DEPTH_BUFFER_BIT);

        depthShaderProgram.bind();
        
        TileMap tileMap = new TileMap();
        for (PointLight pointLight : pointLights) {
        	pointLight.updateTile(tileMap);
            setUniform(depthShaderProgram, pointLight.getMPosition(), pointLight.getShadowMatrices());
            world.render(camera.getPosition());
        }
        
        // Unbind
        depthShaderProgram.unbind();
        glDisable(GL_POLYGON_OFFSET_FILL);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    
    private void renderScene() {
        sceneShaderProgram.bind();

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        sceneShaderProgram.setUniform("viewMatrix", viewMatrix);
        sceneShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        sceneShaderProgram.setUniform("ambientLight", new Vector3f(0.3f, 0.3f, 0.3f));
        sceneShaderProgram.setUniform("specularPower", 1f);

        // Process Point Lights
        for (int i = 0; i < this.pointLights.length; i++) {
            PointLight currPointLight = this.pointLights[i];
            currPointLight.updateViewMatrix(viewMatrix);
            setUniform(sceneShaderProgram, "pointLights", currPointLight, i);
        }

        sceneShaderProgram.setUniform("texture_sampler", 0);
        sceneShaderProgram.setUniform("shadowMap", 1);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, world.getTextureId());

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, shadowMap.getDepthMapTexture());
        
        world.render(camera.getPosition());

        sceneShaderProgram.unbind();
    }
    
    @Override
    public void cleanupRendering() {
        if (sceneShaderProgram != null) {
            sceneShaderProgram.cleanup();
        }
        if (depthShaderProgram != null) {
            depthShaderProgram.cleanup();
        }
    }

    @Override
    public void input(MouseInput mouseInput) {
    }

    @Override
    public void cleanupApplication() {
        world.cleanup();
        shadowMap.cleanup();
    }
    
    private void setUniform(ShaderProgram shaderProgram, String uniformName, PointLight pointLight, int pos) {
        setUniform(shaderProgram, uniformName + "[" + pos + "]", pointLight);
    }

    private void setUniform(ShaderProgram shaderProgram, String uniformName, PointLight pointLight) {
    	shaderProgram.setUniform(uniformName + ".colour", pointLight.getColor());
    	shaderProgram.setUniform(uniformName + ".mvPosition", pointLight.getMvPosition());
    	shaderProgram.setUniform(uniformName + ".mPosition", pointLight.getMPosition());
    	shaderProgram.setUniform(uniformName + ".intensity", pointLight.getIntensity());
        PointLight.Attenuation att = pointLight.getAttenuation();
        shaderProgram.setUniform(uniformName + ".att.constant", att.getConstant());
        shaderProgram.setUniform(uniformName + ".att.linear", att.getLinear());
        shaderProgram.setUniform(uniformName + ".att.exponent", att.getExponent());
        shaderProgram.setUniform(uniformName + ".shadowViewProjTexMatrices", pointLight.getShadowMatrices());
    }
    
    private void createPointLightListUniform(ShaderProgram shaderProgram, String uniformName, int size) throws Exception {
        for (int i = 0; i < size; i++) {
            createPointLightUniform(shaderProgram, uniformName + "[" + i + "]");
        }
    }

    public void createPointLightUniform(ShaderProgram shaderProgram, String uniformName) throws Exception {
    	shaderProgram.createUniform(uniformName + ".colour");
    	shaderProgram.createUniform(uniformName + ".mvPosition");
    	shaderProgram.createUniform(uniformName + ".mPosition");
    	shaderProgram.createUniform(uniformName + ".intensity");
    	shaderProgram.createUniform(uniformName + ".att.constant");
    	shaderProgram.createUniform(uniformName + ".att.linear");
    	shaderProgram.createUniform(uniformName + ".att.exponent");
    	shaderProgram.createUniform(uniformName + ".shadowViewProjTexMatrices");
    }


    private void createPointLightShadowUniform(ShaderProgram shaderProgram) throws Exception {
    	shaderProgram.createUniform("lightPosition");
    	shaderProgram.createUniform("shadowViewProjTexMatrices");
    }

    public void setUniform(ShaderProgram shaderProgram, Vector3f position, Matrix4f[] matrices) {
    	shaderProgram.setUniform("lightPosition", position);
    	shaderProgram.setUniform("shadowViewProjTexMatrices", matrices);
    }

}
