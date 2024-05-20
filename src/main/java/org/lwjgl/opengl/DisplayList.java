package org.lwjgl.opengl;

import main.Main;

public class DisplayList {
	
	public static boolean isCompiling = false;
	public final int id;
	public Main.BufferArrayGL array;
	public Main.BufferGL buffer;
	public int mode;
	public int length;
	
	public DisplayList(int id) {
		this.id = id;
		array = null;
		buffer = null;
		mode = -1;
		length = 0;
	}
}
