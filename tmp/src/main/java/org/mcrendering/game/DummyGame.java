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
import org.mcrendering.engine.Utils;
import org.mcrendering.engine.Window;
import org.mcrendering.engine.graph.Camera;
import org.mcrendering.engine.graph.Material;
import org.mcrendering.engine.graph.Mesh;
import org.mcrendering.engine.graph.Renderer;
import org.mcrendering.engine.graph.Texture;
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

    private static final float CAMERA_POS_STEP = 0.10f;

    private boolean firstTime;

    private boolean sceneChanged;

    private GameItem[] gameItems;

    public DummyGame() {
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        firstTime = true;
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);

        scene = new Scene();

        float reflectance = 1f;

        float blockScale = 1f;

        float startx = -10;
        float startz = -10;
        float starty = 0;
        float inc = blockScale * 2;

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
        Texture texture = new Texture("/textures/terrain_textures.png");
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

        camera.getPosition().x = 0.25f;
        camera.getPosition().y = 6.5f;
        camera.getPosition().z = 6.5f;
        camera.getRotation().x = 25;
        camera.getRotation().y = -1;
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
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            sceneChanged = true;
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

        // Update view matrix
        camera.updateViewMatrix();
    }

    @Override
    public void render(Window window) {
        if (firstTime) {
            sceneChanged = true;
            firstTime = false;
        }
        renderer.render(window, camera, scene, sceneChanged);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();

        scene.cleanup();
    }
}
