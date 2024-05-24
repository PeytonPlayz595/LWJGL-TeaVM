package org.lwjgl.opengl;

import main.BufferArrayGL;
import main.BufferGL;

public class DisplayList {
	
	public final int id;
	public BufferArrayGL array;
	public BufferGL buffer;
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
