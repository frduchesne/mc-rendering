package org.mcrendering.game;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_X;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Z;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.mcrendering.engine.IGameLogic;
import org.mcrendering.engine.MouseInput;
import org.mcrendering.engine.Scene;
import org.mcrendering.engine.SceneLight;
import org.mcrendering.engine.Utils;
import org.mcrendering.engine.Window;
import org.mcrendering.engine.graph.Camera;
import org.mcrendering.engine.graph.Material;
import org.mcrendering.engine.graph.Mesh;
import org.mcrendering.engine.graph.Renderer;
import org.mcrendering.engine.graph.Texture;
import org.mcrendering.engine.graph.lights.DirectionalLight;
import org.mcrendering.engine.graph.lights.PointLight;
import org.mcrendering.engine.graph.lights.PointLight.Attenuation;
import org.mcrendering.engine.items.GameItem;
import org.mcrendering.engine.loaders.obj.OBJLoader;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.stream.NBTInputStream;

public class DummyGame implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;

    private final Vector3f cameraInc;

    private final Renderer renderer;

    private final Camera camera;

    private Scene scene;

    private Hud hud;

    private static final float CAMERA_POS_STEP = 0.10f;

    private float angleInc;

    private float lightAngle;

    private MouseBoxSelectionDetector selectDetector;

    private boolean leftButtonPressed;

    private boolean firstTime;

    private boolean sceneChanged;

    private GameItem[] gameItems;

    public DummyGame() {
        renderer = new Renderer();
        hud = new Hud();
        camera = new Camera();
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        angleInc = 0;
        lightAngle = 90;
        firstTime = true;
    }

    @Override
    public void init(Window window) throws Exception {
        hud.init(window);
        renderer.init(window);

        leftButtonPressed = false;

        scene = new Scene();

        float reflectance = 1f;

        float blockScale = 1f;

        float startx = -10;
        float startz = -10;
        float starty = 0;
        float inc = blockScale * 2;

        selectDetector = new MouseBoxSelectionDetector();

        InputStream fis = Class.forName(Utils.class.getName()).getResourceAsStream("/models/caverne.schematic");
        NBTInputStream nbt = new NBTInputStream(fis);
        CompoundTag backuptag = (CompoundTag) nbt.readTag();
        CompoundMap tagCollection = backuptag.getValue();

        short width = (Short) tagCollection.get("Width").getValue();
        short height = (Short) tagCollection.get("Height").getValue();
        short length = (Short) tagCollection.get( "Length").getValue();

        byte[] blocks = (byte[]) tagCollection.get("Blocks").getValue();
        nbt.close();
        fis.close();
        
        int instances = 0;
        for (int i = 0; i < blocks.length; i++) {
        	if (blocks[i] == 1) {
        		instances++;
        	}
        }
        
        Mesh mesh = OBJLoader.loadMesh("/models/cube.obj", instances);
        mesh.setBoundingRadius(1);
        Texture texture = new Texture("/textures/terrain_textures.png", 2, 1);
        Material material = new Material(texture, reflectance);
        mesh.setMaterial(material);
        gameItems = new GameItem[instances];
        
        List<Vector3f> pointLightPositions = new ArrayList<>();
        int numBlock = 0;
        for (int h = 0; h < height; h++) {
        	for (int l = 0; l < length; l++) {
        		for (int w = 0; w < width; w++) {
        			int index = (h * length + l) * width + w;
        			byte block = blocks[index];
        			float x = startx + w * inc;
        			float y = starty + h * inc;
        			float z = startz + l * inc;
        			if (block == 1) {
        				
                        GameItem gameItem = new GameItem(mesh);
                        gameItem.setScale(blockScale);
                        gameItem.setPosition(x, y, z);
                        gameItems[numBlock] = gameItem;
                        numBlock++;
            			if (block == 1) {
            				gameItem.setTextPos(0);
            			} 
        			} else if (block == 50) {
        				pointLightPositions.add(new Vector3f(x, y, z));
        			}
         			
        		}
        	}
        }
		pointLightPositions.add(new Vector3f(4.0f, 8.0f, 6.0f));
        
        scene.setGameItems(gameItems);

        // Setup Lights
        setupLights(pointLightPositions);

        camera.getPosition().x = 0.25f;
        camera.getPosition().y = 6.5f;
        camera.getPosition().z = 6.5f;
        camera.getRotation().x = 25;
        camera.getRotation().y = -1;
    }


    private void setupLights(List<Vector3f> pointLightPositions) {
        SceneLight sceneLight = new SceneLight();
        scene.setSceneLight(sceneLight);

        // Ambient Light
        sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
        sceneLight.setSkyBoxLight(new Vector3f(1.0f, 1.0f, 1.0f));

        // Directional Light
        float lightIntensity = 0.001f;
        Vector3f lightDirection = new Vector3f(0, 1, 1);
        DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
        sceneLight.setDirectionalLight(directionalLight);
        
        if (pointLightPositions != null && !pointLightPositions.isEmpty()) {
            PointLight[] pointLights = new PointLight[pointLightPositions.size()];
            for (int i = 0; i < pointLightPositions.size(); i++) {
            	pointLights[i] = new PointLight(
            			new Vector3f(210f/255f, 190f/255f, 156f/255f), 
//            			new Vector3f(255f/255f, 120f/255f, 0f/255f), 
//            			new Vector3f(1.0f, 0.9f, 0.8f), 
            			pointLightPositions.get(i), 
            			20.0f,
            			new Attenuation(1.0f, 0.35f, 0.44f));
            }
            sceneLight.setPointLightList(pointLights);
        }
    }

    @Override
    public void input(Window window, MouseInput mouseInput) {
        sceneChanged = false;
        cameraInc.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) {
            sceneChanged = true;
            cameraInc.z = -1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            sceneChanged = true;
            cameraInc.z = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            sceneChanged = true;
            cameraInc.x = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            sceneChanged = true;
            cameraInc.x = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_Z)) {
            sceneChanged = true;
            cameraInc.y = -1;
        } else if (window.isKeyPressed(GLFW_KEY_X)) {
            sceneChanged = true;
            cameraInc.y = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_LEFT)) {
            sceneChanged = true;
            angleInc -= 0.05f;
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            sceneChanged = true;
            angleInc += 0.05f;
        } else {
            angleInc = 0;
        }
    }

    @Override
    public void update(float interval, MouseInput mouseInput, Window window) {
        if (mouseInput.isRightButtonPressed()) {
            // Update camera based on mouse            
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
            sceneChanged = true;
        }

        // Update camera position
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);

        lightAngle += angleInc;
        if (lightAngle < 0) {
            lightAngle = 0;
        } else if (lightAngle > 180) {
            lightAngle = 180;
        }
        float zValue = (float) Math.cos(Math.toRadians(lightAngle));
        float yValue = (float) Math.sin(Math.toRadians(lightAngle));
        Vector3f lightDirection = this.scene.getSceneLight().getDirectionalLight().getDirection();
        lightDirection.x = 0;
        lightDirection.y = yValue;
        lightDirection.z = zValue;
        lightDirection.normalize();

        // Update view matrix
        camera.updateViewMatrix();

        boolean aux = mouseInput.isLeftButtonPressed();
        if (aux && !this.leftButtonPressed && this.selectDetector.selectGameItem(gameItems, window, mouseInput.getCurrentPos(), camera)) {
            this.hud.incCounter();
        }
        this.leftButtonPressed = aux;
    }

    @Override
    public void render(Window window) {
        if (firstTime) {
            sceneChanged = true;
            firstTime = false;
        }
        renderer.render(window, camera, scene, sceneChanged);
        hud.render(window);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();

        scene.cleanup();
        if (hud != null) {
            hud.cleanup();
        }
    }
}
