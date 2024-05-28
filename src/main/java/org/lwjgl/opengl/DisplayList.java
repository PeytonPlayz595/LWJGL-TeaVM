package org.lwjgl.opengl;

import main.BufferArrayGL;
import main.BufferGL;

public class DisplayList {
	
	public final int id;
	public BufferArrayGL array;
	public BufferGL buffer;
	public int mode;
	public int length;
	public int drawMode = GL11.GL_QUADS;
	public Object currentBuffer = null;
	public int count = 0;
	public int first = 0;
	
	public DisplayList(int id) {
		this.id = id;
		array = null;
		buffer = null;
		mode = -1;
		length = 0;
	}
}
