package org.lwjgl.opengl;

import main.Main;
import main.Main.WebGL2RenderingContext;

public class GL11 extends Main.GLEnums {

    static WebGL2RenderingContext webgl = null;
	
	public static final void glFlush() {
		webgl.flush();
	}
	
	static {
		webgl = Main.webgl;
	}
}
