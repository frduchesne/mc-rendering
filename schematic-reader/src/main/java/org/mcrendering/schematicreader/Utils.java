package org.mcrendering.schematicreader;

import java.io.File;
import java.net.URL;

public class Utils {
	public static File getResource(String path) {
		
		URL res = Utils.class.getClassLoader().getResource(path);
		
		if (res == null) {
			return null;
		}
		
		File file = new File(res.getFile());
		
		if (!file.exists()) {
			return null;
		}
		return file;
	}
}
