package org.mcrendering.common;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_X;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Z;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class GameEngine implements Runnable {

    public static final int TARGET_FPS = 75;

    public static final int TARGET_UPS = 30;
    
    private static final float MOUSE_SENSITIVITY = 0.2f;
    
    private static final float CAMERA_POS_STEP = 0.10f;

    private final Vector3f cameraInc;

    private final Camera camera;

    private final Window window;

    private final Thread gameLoopThread;

    private final Timer timer;

    private final IRenderer renderer;

    private final MouseInput mouseInput;

    private double lastFps;
    
    private int fps;
    
    private String windowTitle;
    
    public GameEngine(String windowTitle, IRenderer renderer) throws Exception {
        this(windowTitle, 0, 0, renderer);
    }

    public GameEngine(String windowTitle, int width, int height, IRenderer renderer) throws Exception {
        camera = new Camera();
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        this.windowTitle = windowTitle;
        gameLoopThread = new Thread(this, "GAME_LOOP_THREAD");
        window = new Window(windowTitle, width, height);
        mouseInput = new MouseInput();
        this.renderer = renderer;
        timer = new Timer();
    }

    public void start() {
        String osName = System.getProperty("os.name");
        if ( osName.contains("Mac") ) {
            gameLoopThread.run();
        } else {
            gameLoopThread.start();
        }
    }

    @Override
    public void run() {
        try {
            init();
            gameLoop();
        } catch (Exception excp) {
            excp.printStackTrace();
        } finally {
            cleanup();
        }
    }

    protected void init() throws Exception {
        window.init();
        timer.init();
        mouseInput.init(window);
        renderer.initApplication(window, camera);
        renderer.initRendering();
        lastFps = timer.getTime();
        fps = 0;
    }

    protected void gameLoop() {
        float elapsedTime;
        float accumulator = 0f;
        float interval = 1f / TARGET_UPS;

        boolean running = true;
        while (running && !window.windowShouldClose()) {
            elapsedTime = timer.getElapsedTime();
            accumulator += elapsedTime;

            input();

            while (accumulator >= interval) {
                update(interval);
                accumulator -= interval;
            }

            render();
        }
    }

    protected void cleanup() {
        renderer.cleanupRendering();
        renderer.cleanupApplication();
    }

    protected void input() {
        mouseInput.input(window);
        cameraInc.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = -1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_Z)) {
            cameraInc.y = -1;
        } else if (window.isKeyPressed(GLFW_KEY_X)) {
            cameraInc.y = 1;
        }

        renderer.input(mouseInput);
    }

    protected void update(float interval) {
        if (mouseInput.isRightButtonPressed()) {
            // Update camera based on mouse            
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
        }

        // Update camera position
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);

        // Update view matrix
        camera.updateViewMatrix();
    }

    protected void render() {
        if ( timer.getLastLoopTime() - lastFps > 1 ) {
            lastFps = timer.getLastLoopTime();
            window.setWindowTitle(windowTitle + " - " + fps + " FPS");
            fps = 0;
        }
        fps++;
        renderer.render();
        window.update();
    }

}
