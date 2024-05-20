package org.lwjgl.opengl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.Float32Array;
import org.teavm.jso.typedarrays.Int32Array;
import org.teavm.jso.typedarrays.Uint8Array;
import org.teavm.jso.webgl.WebGLUniformLocation;

import main.Main;
import main.Main.BufferGL;
import main.Main.WebGL2RenderingContext;

public class GL11 extends Main.GLEnums {
	
	static WebGL2RenderingContext webgl;
	
	private static float alphaValue;
	private static boolean alpha;
	
	private static int currentWebGLProgram;
	
	//Uniform functions
	private static Float32Array mat2;
	private static Float32Array mat3;
	private static Float32Array mat4;
	
	private static final HashMap<Integer, Main.TextureGL> textures;
	private static int textureIndex;
	
	//Tessellator
	private static Int32Array tessIntBuffer;
	private static Float32Array tessFloatBuffer;
	private static boolean isTessDrawing;
	private static int tessVertexCount;
	private static boolean tessHasTexture;
	private static boolean tessHasColor;
	private static boolean tessHasNormals;
	
	private static boolean textureArray;
	private static boolean colorArray;
	private static boolean normalArray;
	
	private static DisplayList currentList;
	private static final HashMap<Integer, DisplayList> lists;
	private static final HashMap<Integer, DisplayList> initLists;
	private static BufferGL quadsToTrianglesBuffer;
	private static Main.BufferArrayGL currentArray;
	private static int bytesUploaded;
	private static int vertexDrawn;
	private static int triangleDrawn;
	
	public static final void glAlphaFunc(int func, float ref) {
		//only GL_GREATER is supported so the first param is ignored
		alphaValue = ref;
	}
	
	public static final void glClearColor(float red, float green, float blue, float alpha) {
		webgl.clearColor(red, green, blue, alpha);
	}
	
	public static final void glClear(int mask) {
		webgl.clear(mask);
	}
	
	public static final void glScissor(int x, int y, int width, int height) {
		if(webgl.isEnabled(GL_SCISSOR_TEST)) {
			webgl.scissor(x, y, width, height);
		}
	}
	
	public static final void glFlush() {
		webgl.flush();
	}
	
	public static final void glCallLists(int size, int type, Buffer listsBuffer) {
		if(size < 0) {
			throw new IllegalArgumentException("negative buffer size");
		}
		if(listsBuffer == null) {
			throw new NullPointerException("null buffer parameter passed");
		}
		if(!(listsBuffer instanceof IntBuffer)) {
			throw new IllegalArgumentException("invalid buffer type: " + listsBuffer.getClass());
		}
		
		IntBuffer listsIntBuffer = (IntBuffer)listsBuffer;
		
		int[] lists = new int[size];
		listsIntBuffer.get(lists);
		
		for(int i = 0; i < size; i++) {
			glCallList(lists[i]);
		}
	}
	
	private static final void glDrawQuadArrays(int p2, int p3) {
		if (quadsToTrianglesBuffer == null) {
			IntBuffer upload = IntBuffer.wrap(new int[98400 / 2]);
			for (int i = 0; i < 16384; ++i) {
				int v1 = i * 4;
				int v2 = i * 4 + 1;
				int v3 = i * 4 + 2;
				int v4 = i * 4 + 3;
				upload.put(v1 | (v2 << 16));
				upload.put(v4 | (v2 << 16));
				upload.put(v3 | (v4 << 16));
			}
			upload.flip();
			quadsToTrianglesBuffer = glCreateBuffer();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, quadsToTrianglesBuffer);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, upload, GL_STATIC_DRAW);
		}
		if (!currentArray.isQuadBufferBound) {
			currentArray.isQuadBufferBound = true;
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, quadsToTrianglesBuffer);
		}
		glDrawElements(GL_TRIANGLES, p3 * 6 / 4, GL_UNSIGNED_SHORT, p2 * 6 / 4);
	}
	
	public static final void glBindVertexArray0(Main.BufferArrayGL p1) {
		currentArray = p1;
		if(p1 == null) {
			webgl.bindVertexArray(null);
			return;
		}
		webgl.bindVertexArray(p1.obj);
	}
	
	private static int displayListId = 0;
	public static final int glGenLists(int range) {
		int base = displayListId + 1;
		for (int i = 0; i < range; i++) {
			int id = ++displayListId;
			lists.put(id, new DisplayList(id));
		}
		return base;
	}
	
	public static final void glNewList(int list, int mode) {
		if(!DisplayList.isCompiling) {
			currentList = lists.get(list);
			if(currentList != null) {
				currentList.mode = -1;
				currentList.length = 0;
				DisplayList.isCompiling = true;
			}
		}
	}
	
	public static final void glCallList(int list) {
		if(!DisplayList.isCompiling) {
			DisplayList displayList = initLists.get(list);
			if(displayList != null && displayList.length > 0) {
				glBindShaders(displayList.mode | glGetShaderMode1());
				glBindVertexArray0(displayList.array);
				glDrawQuadArrays(0, displayList.length);
				WebGLShader.unuse();
				vertexDrawn += displayList.length * 6 / 4;
				triangleDrawn += displayList.length / 2;
			}
		}
	}
	
	public static final void glEndList() {
		if(DisplayList.isCompiling) {
			DisplayList.isCompiling = false;
			Object upload = glGetLowLevelBuffersAppended();
			int l = glArrayByteLength(upload);
			if(l > 0) {
				if(currentList.buffer == null) {
					initLists.put(currentList.id, currentList);
					currentList.array = glCreateVertexArray();
					currentList.buffer = glCreateBuffer();
					Main.WebGLShader s = Main.WebGLShader.instance(currentList.mode);
					glBindVertexArray0(currentList.array);
					glBindBuffer(GL_ARRAY_BUFFER, currentList.buffer);
					s.setupArrayForProgram();
				}
				glBindBuffer(GL_ARRAY_BUFFER, currentList.buffer);
				glBufferData(GL_ARRAY_BUFFER, upload, GL_STATIC_DRAW);
				bytesUploaded += l;
			}
		}
	}
	
	public static final void glDeleteLists(int list, int range) {
		for (int i = 0; i < range; i++) {
			DisplayList d = initLists.remove(list + i);
			if (d != null) {
				glDeleteVertexArray(d.array);
				glDeleteBuffer(d.buffer);
			}
			lists.remove(list + i);
		}
	}
	
	public static final void glBlendFunc(int sFactor, int dFactor) {
		webgl.blendFunc(sFactor, dFactor);
	}
	
	public static final void glBindTexture(int target, int texture) {
		Main.TextureGL t = textures.get(texture);
		if(t == null) {
			webgl.bindTexture(target, null);
			return;
		}
 		webgl.bindTexture(target, t.obj);
	}
	
	//Only GL_QUADS are supported????
	public static final void glBegin(int mode) {
		if(isTessDrawing) {
			glEnd();
		}
		isTessDrawing = true;
		tessVertexCount = 0;
		tessHasTexture = false;
		tessHasColor = false;
		tessHasNormals = false;
	}
	
	public static final void glEnd() {
		if(!isTessDrawing) {
			throw new IllegalStateException("Not drawing!");
		}
		isTessDrawing = false;
		if(tessVertexCount > 0) {
			if(tessHasTexture) {
				glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			}
			
			if(tessHasColor) {
				glEnableClientState(GL_COLOR_ARRAY);
			}
			
			if(tessHasNormals) {
				glEnableClientState(GL_NORMAL_ARRAY);
			}
			
			glDrawArrays(GL_QUADS, GL_POINTS, tessVertexCount, Int32Array.create(tessIntBuffer.getBuffer(), 0, tessVertexCount * 7));
			
			if(tessHasTexture) {
				glDisableClientState(GL_TEXTURE_COORD_ARRAY);
			}

			if(tessHasColor) {
				glDisableClientState(GL_COLOR_ARRAY);
			}

			if(tessHasNormals) {
				glDisableClientState(GL_NORMAL_ARRAY);
			}
		}
	}
	
	private static void addVertex(double d1, double d2, double d3) {
		
	}
	
	public static final void glClearDepth(double depth) {
		webgl.clearDepth((float)depth);
	}
	
	public static final void glClearDepth(float depth) {
		webgl.clearDepth(depth);
	}
	
	public static final void glDrawElements(int mode, int type, int count, int offset) {
		webgl.drawElements(mode, type, count, offset);
	}
	
	//Code pulled from eagler b1.3's EaglerAdapter
	private static Object blankUploadArray = Int32Array.create(525000);
	public static final void glDrawArrays(int mode, int first, int count, Object buffer) {
		if(DisplayList.isCompiling) {
			if(mode == GL_QUADS) {
				if(currentList.mode == -1) {
					currentList.mode = glGetShaderMode0();
				} else {
					if(currentList.mode != glGetShaderMode0()) {
						throw new IllegalArgumentException("inconsistent vertex format");
					}
					currentList.length += count;
					glAppendLowLevelBuffer(buffer);
				}
			} else {
				throw new IllegalArgumentException("Only GL_QUADS are supported in display lists!");
			}
		} else {
			int bl = glArrayByteLength(buffer);
			bytesUploaded += bl;
			vertexDrawn += count;
			
			glBindShaders();
			
			glBindVertexArray0(WebGLShader.genericArray);
			glBindBuffer(GL_ARRAY_BUFFER, WebGLShader.genericBuffer);
			if(!WebGLShader.bufferIsInitialized) {
				WebGLShader.bufferIsInitialized = true;
				glBufferData(GL_ARRAY_BUFFER, blankUploadArray, GL_DYNAMIC_DRAW);
			}
			glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
			
			if(mode == GL_QUADS) {
				glDrawQuadArrays(first, count);
				triangleDrawn += count / 2;
			} else {
				int drawMode = 0;
				switch (mode) {
				default:
				case GL_TRIANGLES:
					drawMode = GL_TRIANGLES;
					triangleDrawn += count / 3;
					break;
				case GL_TRIANGLE_STRIP:
					drawMode = GL_TRIANGLE_STRIP;
					triangleDrawn += count - 2;
					break;
				case GL_TRIANGLE_FAN:
					drawMode = GL_TRIANGLE_FAN;
					triangleDrawn += count - 2;
					break;
				case GL_LINE_STRIP:
					drawMode = GL_LINE_STRIP;
					triangleDrawn += count - 1;
					break;
				case GL_LINES:
					drawMode = GL_LINES;
					triangleDrawn += count / 2;
					break;
				}
				webgl.drawArrays(drawMode, first, count);
			}
			
			WebGLShader.unuse();
			
		}
	}

	public static final void glDrawArrays(int mode, int first, int count) {
		webgl.drawArrays(mode, first, count);
	}
	
	public static final void glEnableClientState(int cap) {
		switch(cap) {
			case GL_TEXTURE_COORD_ARRAY:
				textureArray = true;
				break;
				
			case GL_COLOR_ARRAY:
				colorArray = true;
				break;
				
			case GL_NORMAL_ARRAY:
				normalArray = true;
				break;
				
			default:
				break;
		}
	}
	
	public static final void glDisableClientState(int cap) {
		switch(cap) {
			case GL_TEXTURE_COORD_ARRAY:
				textureArray = false;
				break;
				
			case GL_COLOR_ARRAY:
				colorArray = false;
				break;
				
			case GL_NORMAL_ARRAY:
				normalArray = false;
				break;
				
			default:
				break;
		}
	}
	
	public static final void glTexParameteri(int target, int pname, int param) {
		webgl.texParameteri(target, pname, param);
	}
	
	private static Uint8Array bufferUpload = Uint8Array.create(ArrayBuffer.create(4 * 1024 * 1024));
	public static final void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels) {
		if(pixels == null) {
			webgl.texImage2D(target, level, internalformat, width, height, border, format, type, null);
			return;
		}
		bytesUploaded += pixels.remaining() * 4;
		if(pixels instanceof ByteBuffer) {
			int bufferLength = pixels.remaining();
			Uint8Array array = bufferUpload;
			for(int i = 0; i < bufferLength; ++i) {
				array.set(i, (short) ((int)((ByteBuffer)pixels).get() & 0xff));
			}
			Uint8Array bufferData = Uint8Array.create(bufferUpload.getBuffer(), 0, bufferLength);
			webgl.texImage2D(target, level, internalformat, width, height, border, format, type, bufferData);
		} else if(pixels instanceof IntBuffer) {
			int bufferLength = pixels.remaining();
			Int32Array array = Int32Array.create(bufferUpload.getBuffer());
			for(int i = 0; i < bufferLength; ++i) {
				array.set(i, ((IntBuffer)pixels).get());
			}
			Uint8Array bufferData = Uint8Array.create(bufferUpload.getBuffer(), 0, bufferLength*4);
			webgl.texImage2D(target, level, internalformat, width, height, border, format, type, bufferData);
		} else {
			System.err.println("glTexImage2D: Unsupported Buffer type!");
		}
	}
	
	public static final void glBufferData(int target, Buffer data, int usage) {
		IntBuffer buffer = (IntBuffer)data;
		int length = buffer.remaining();
		Int32Array array = Int32Array.create(bufferUpload.getBuffer());
		for(int i = 0; i < length; ++i) {
			array.set(i, buffer.get());
		}
		Uint8Array data1 = Uint8Array.create(bufferUpload.getBuffer(), 0, length*4);
		webgl.bufferData(target, data1, usage);
	}
	
	public static final void glBufferData(int target, Object data, int usage) {
		webgl.bufferData(target, (Int32Array)data, usage);
	}
	
	private static final int glGetShaderMode0() {
		int mode = 0;
		mode = (mode | (colorArray ? Main.WebGLShader.COLOR : 0));
		mode = (mode | (normalArray ? Main.WebGLShader.NORMAL : 0));
		mode = (mode | (textureArray ? Main.WebGLShader.TEXTURE0 : 0));
		return mode;
	}

	private static final int glGetShaderMode1() {
		int mode = 0;
		mode = (mode | (alpha ? Main.WebGLShader.ALPHATEST : 0));
		return mode;
	}
	
	private static int getShaderMode() {
		int mode = 0;
		mode = (mode | (colorArray ? Main.WebGLShader.COLOR : 0));
		mode = (mode | (normalArray ? Main.WebGLShader.NORMAL : 0));
		mode = (mode | (textureArray ? Main.WebGLShader.TEXTURE0 : 0));
		mode = (mode | (alpha ? Main.WebGLShader.ALPHATEST : 0));
		return mode;
	}
	
	public static final void glBindShaders() {
		Main.WebGLShader shader = WebGLShader = Main.WebGLShader.instance(getShaderMode());
		shader.use();
		
		if (alpha) {
			shader.alphaTest(alphaValue);
		}
	}
	
	public static final void glBindShaders(int i) {
		
	}
	
	public static final Main.ShaderGL glCreateShader(int p1) {
		return new Main.ShaderGL(webgl.createShader(p1));
	}
	
	public static final Main.ProgramGL glCreateProgram() {
		return new Main.ProgramGL(webgl.createProgram());
	}
	
	public static final void glDetachShader(Main.ProgramGL p1, Main.ShaderGL p2) {
		webgl.detachShader(p1.obj, p2.obj);
	}
	
	public static final void glDeleteShader(Main.ShaderGL p1) {
		webgl.deleteShader(p1.obj);
	}
	
	public static final void glCompileShader(Main.ShaderGL p1) {
		webgl.compileShader(p1.obj);
	}
	
	public static final void glAttachShader(Main.ProgramGL p1, Main.ShaderGL p2) {
		webgl.attachShader(p1.obj, p2.obj);
	}
	
	public static final void glLinkProgram(Main.ProgramGL p1) {
		webgl.linkProgram(p1.obj);
	}
	
	public static final void glShaderSource(Main.ShaderGL p1, String p2) {
		webgl.shaderSource(p1.obj, p2);
	}
	
	public static final void glDeleteProgram(Main.ProgramGL program) {
		webgl.deleteProgram(program.obj);
	}
	
	public static final String glGetShaderHeader() {
		return "#version 300 es";
	}
	
	public static final String glGetShaderInfoLog(Main.ShaderGL p1) {
		return webgl.getShaderInfoLog(p1.obj);
	}
	
	public static final String glGetProgramInfoLog(Main.ProgramGL p1) {
		return webgl.getProgramInfoLog(p1.obj);
	}
	
	public static final boolean glGetProgramLinked(Main.ProgramGL p1) {
		return webgl.getProgramParameteri(p1.obj, GL_LINK_STATUS) == 1;
	}
	
	public static final boolean glGetShaderCompiled(Main.ShaderGL p1) {
		return webgl.getShaderParameteri(p1.obj, GL_COMPILE_STATUS) == 1;
	}
	
	public static final void glBindAttributeLocation(Main.ProgramGL p1, int p2, String p3) {
		webgl.bindAttribLocation(p1.obj, p2, p3);
	}
	
	public static final Main.UniformGL glGetUniformLocation(Main.ProgramGL p1, String p2) {
		WebGLUniformLocation u = webgl.getUniformLocation(p1.obj, p2);
		return u == null ? null : new Main.UniformGL(u);
	}
	
	public static final void glUseProgram(Main.ProgramGL p1) {
		if(p1 != null && currentWebGLProgram != p1.hashcode) {
			currentWebGLProgram = p1.hashcode;
			webgl.useProgram(p1.obj);
		}
	}
	
	public static final void glUniform1f(Main.UniformGL p1, float p2) {
		if(p1 != null) webgl.uniform1f(p1.obj, p2);
	}
	
	public static final void glUniform2f(Main.UniformGL p1, float p2, float p3) {
		if(p1 != null) webgl.uniform2f(p1.obj, p2, p3);
	}
	
	public static final void glUniform3f(Main.UniformGL p1, float p2, float p3, float p4) {
		if(p1 != null) webgl.uniform3f(p1.obj, p2, p3, p4);
	}
	
	public static final void glUniform4f(Main.UniformGL p1, float p2, float p3, float p4, float p5) {
		if(p1 != null) webgl.uniform4f(p1.obj, p2, p3, p4, p5);
	}
	
	public static final void glUniform1i(Main.UniformGL p1, int p2) {
		if(p1 != null) webgl.uniform1i(p1.obj, p2);
	}
	
	public static final void glUniform2i(Main.UniformGL p1, int p2, int p3) {
		if(p1 != null) webgl.uniform2i(p1.obj, p2, p3);
	}
	
	public static final void glUniform3i(Main.UniformGL p1, int p2, int p3, int p4) {
		if(p1 != null) webgl.uniform3i(p1.obj, p2, p3, p4);
	}
	
	public static final void glUniform4i(Main.UniformGL p1, int p2, int p3, int p4, int p5) {
		if(p1 != null) webgl.uniform4i(p1.obj, p2, p3, p4, p5);
	}
	
	public static final void glUniformMat2fv(Main.UniformGL p1, float[] mat) {
		mat2.set(mat);
		if(p1 != null) webgl.uniformMatrix2fv(p1.obj, false, mat2);
	}
	
	public static final void glUniformMat3fv(Main.UniformGL p1, float[] mat) {
		mat3.set(mat);
		if(p1 != null) webgl.uniformMatrix3fv(p1.obj, false, mat3);
	}
	
	public static final void glUniformMat4fv(Main.UniformGL p1, float[] mat) {
		mat4.set(mat);
		if(p1 != null) webgl.uniformMatrix4fv(p1.obj, false, mat4);
	}
	
	public static final void glEnableVertexAttribArray(int p1) {
		webgl.enableVertexAttribArray(p1);
	}
	
	public static final void glVertexAttribPointer(int p1, int p2, int p3, boolean p4, int p5, int p6) {
		webgl.vertexAttribPointer(p1, p2, p3, p4, p5, p6);
	}
	
	public static final Main.BufferArrayGL glCreateVertexArray() {
		return new Main.BufferArrayGL(webgl.createVertexArray());
	}
	
	public static final Main.BufferGL glCreateBuffer() {
		return new Main.BufferGL(webgl.createBuffer());
	}
	
	public static final void glBindVertexArray(Main.BufferArrayGL p1) {
		webgl.bindVertexArray(p1 == null ? null : p1.obj);
	}
	
	public static final void glBindBuffer(int p1, BufferGL p2) {
		webgl.bindBuffer(p1, p2 == null ? null : p2.obj);
	}
	
	public static final int glGenTextures() {
		textureIndex++;
		if(textureIndex >= 256) {
			//Reset texture index instead of clearing textures completely
			textureIndex = 1;
		}
		
		textures.put(textureIndex, new Main.TextureGL(webgl.createTexture()));
		return textureIndex;
	}
	
	private static int appendbufferindex = 0;
	private static Int32Array appendbuffer = Int32Array.create(ArrayBuffer.create(525000*4));

	public static final void glAppendLowLevelBuffer(Object arr) {
		Int32Array a = ((Int32Array)arr);
		if(appendbufferindex + a.getLength() < appendbuffer.getLength()) {
			appendbuffer.set(a, appendbufferindex);
			appendbufferindex += a.getLength();
		}
	}
	
	public static final Object glGetLowLevelBuffersAppended() {
		Int32Array ret = Int32Array.create(appendbuffer.getBuffer(), 0, appendbufferindex);
		appendbufferindex = 0;
		return ret;
	}
	
	public static final int glArrayByteLength(Object obj) {
		return ((Int32Array)obj).getByteLength();
	}
	
	public static final void glBufferSubData(int p1, int p2, Object p3) {
		webgl.bufferSubData(p1, p2, (Int32Array)p3);
	}
	
	public static final void glDeleteVertexArray(Main.BufferArrayGL p1) {
		webgl.deleteVertexArray(p1.obj);
	}
	
	public static final void glDeleteBuffer(Main.BufferGL p1) {
		webgl.deleteBuffer(p1.obj);
	}
	
	static {
		webgl = Main.webgl;
		
		
		alphaValue = 0.1f;
		alpha = false;
		
		currentWebGLProgram = -1;
		
		//Uniform
		mat2 = Float32Array.create(4);
		mat3 = Float32Array.create(9);
		mat4 = Float32Array.create(16);
		
		textures = new HashMap<Integer, Main.TextureGL>(256);
		textureIndex = 0;
		
		//(Emulates Minecraft's Tessellator)
		ArrayBuffer a = ArrayBuffer.create(524288 * 4);
		tessIntBuffer = Int32Array.create(a);
		tessFloatBuffer = Float32Array.create(a);
		isTessDrawing = false;
		tessVertexCount = 0;
		tessHasTexture = false;
		tessHasColor = false;
		tessHasNormals = false;
		
		textureArray = false;
		colorArray = false;
		normalArray = false;
		
		currentList = null;
		lists = new HashMap<Integer, DisplayList>();
		initLists = new HashMap<Integer, DisplayList>();
		quadsToTrianglesBuffer = null;
		currentArray = null;
		bytesUploaded = 0;
		vertexDrawn = 0;
		triangleDrawn = 0;
	}
	
	private static Main.WebGLShader WebGLShader = null;
	
}
