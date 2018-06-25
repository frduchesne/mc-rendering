package org.mcrendering.common;

public interface IRenderer {

	void initApplication(Window window, Camera camera) throws Exception;
	void initRendering() throws Exception;
	void render();
	void input(MouseInput mouseInput);
	void cleanupRendering();
	void cleanupApplication();
}
