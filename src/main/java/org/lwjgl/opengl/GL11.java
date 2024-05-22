package org.lwjgl.opengl;

import org.lwjgl.util.vector.*;

import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import org.teavm.jso.JSBody;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.Float32Array;
import org.teavm.jso.typedarrays.Int32Array;
import org.teavm.jso.typedarrays.Uint8Array;
import org.teavm.jso.webgl.WebGLBuffer;
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
	
	//Fog
	private static float fogColorR;
	private static float fogColorG;
	private static float fogColorB;
	private static float fogColorA;
	private static int fogMode;
	private static boolean fogEnabled;
	private static boolean fogPremultiply;
	private static float fogStart;
	private static float fogEnd;
	private static float fogDensity;
	
	private static boolean texture2D;
	private static boolean lighting;
	private static boolean colorMaterial;
	
	private static float normalX;
	private static float normalY;
	private static float normalZ;
	private static float tex0X;
	private static float tex0Y;
	
	private static float colorR;
	private static float colorG;
	private static float colorB;
	private static float colorA;
	
	//Matrix variables
	private static int matrixMode;
	static Matrix4f[] matModelV;
	static int matModelPointer;
	static Matrix4f[] matProjV;
	static int matProjPointer;
	static Matrix4f[] matTexV;
	static int matTexPointer;
	
	private static final Vector3f matrixVector;
	
	private static Matrix4f unprojA;
	private static Matrix4f unprojB;
	private static Vector4f unprojC;
	
	private static Vector4f lightPos1vec;
	private static Vector4f lightPos0vec;
	
	private static WebGLBuffer vertexBuffer;
	private static WebGLBuffer texCoordBuffer;
	 
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
		if(glIsEnabled(GL_SCISSOR_TEST)) {
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
	
	public static final void glMultiTexCoord2f(int p1, float p2, float p3) {
		tex0X = p2;
		tex0Y = p3;
	}
	
	public static final void glBlendFunc(int sFactor, int dFactor) {
		fogPremultiply = (sFactor == GL_ONE && dFactor == GL_ONE_MINUS_SRC_ALPHA);
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
	
	public static final void glBegin(int mode) {
		if(ImmediateModeData.mode != -1) {
			glEnd();
		}
		ImmediateModeData.mode = mode;
		ImmediateModeData.vertexPosition = 0;
		ImmediateModeData.textureCoordPosition = 0;
	}
	
	public static final void glTexCoord1f(float s) {
		int currentPos = ImmediateModeData.textureCoordPosition;
		if(currentPos > ImmediateModeData.textureCoordBuffer.getLength()) {
			throw new BufferOverflowException();
		}
		ImmediateModeData.textureCoordBuffer.set(currentPos, s);
		ImmediateModeData.textureCoordPosition = currentPos + 1;
	}
	
	public static final void glTexCoord1d(double s) {
		glTexCoord1f((float)s);
	}
	
	public static final void glTexCoord1i(int s) {
		glTexCoord1f(s);
	}
	
	public static final void glTexCoord2f(float s, float t) {
		int currentPos = ImmediateModeData.textureCoordPosition;
		if(currentPos > ImmediateModeData.textureCoordBuffer.getLength()) {
			throw new BufferOverflowException();
		}
		ImmediateModeData.textureCoordBuffer.set(currentPos, s); //x
		ImmediateModeData.textureCoordBuffer.set(currentPos + 1, t); //y
		ImmediateModeData.textureCoordPosition = currentPos + 2;
	}
	
	public static final void glTexCoord2d(double s, double t) {
		glTexCoord2f((float)s, (float)t);
	}
	
	public static final void glTexCoord2i(int s, int t) {
		glTexCoord2f(s, t);
	}
	
	public static final void glTexCoord3f(float s, float t, float r) {
		int currentPos = ImmediateModeData.textureCoordPosition;
		if(currentPos > ImmediateModeData.textureCoordBuffer.getLength()) {
			throw new BufferOverflowException();
		}
		ImmediateModeData.textureCoordBuffer.set(currentPos, s);
		ImmediateModeData.textureCoordBuffer.set(currentPos + 1, t);
		ImmediateModeData.textureCoordBuffer.set(currentPos + 2, r);
		ImmediateModeData.textureCoordPosition = currentPos + 3;
	}
	
	public static final void glTexCoord3d(double s, double t, double r) {
		glTexCoord3f((float)s, (float)t, (float)r);
	}
	
	public static final void glTexCoord3i(int s, int t, int r) {
		glTexCoord3f(s, t, r);
	}
	
	public static final void glTexCoord4f(float s, float t, float r, float q) {
		int currentPos = ImmediateModeData.textureCoordPosition;
		if(currentPos > ImmediateModeData.textureCoordBuffer.getLength()) {
			throw new BufferOverflowException();
		}
		ImmediateModeData.textureCoordBuffer.set(currentPos, s);
		ImmediateModeData.textureCoordBuffer.set(currentPos + 1, t);
		ImmediateModeData.textureCoordBuffer.set(currentPos + 2, r);
		ImmediateModeData.textureCoordBuffer.set(currentPos + 3, q);
		ImmediateModeData.textureCoordPosition = currentPos + 4;
	}
	
	public static final void glTexCoord4d(double s, double t, double r, double q) {
		glTexCoord4f((float)s, (float)t, (float)r, (float)q);
	}
	
	public static final void glTexCoord4i(int s, int t, int r, int q) {
		glTexCoord4f(s, t, r, q);
	}
	
	public static final void glVertex2f(float x, float y) {
		int currentPos = ImmediateModeData.vertexPosition;
		if(currentPos > ImmediateModeData.vertexBuffer.getLength()) {
			throw new BufferOverflowException();
		}
		ImmediateModeData.vertexBuffer.set(currentPos, x);
		ImmediateModeData.vertexBuffer.set(currentPos + 1, y);
		ImmediateModeData.vertexPosition = currentPos + 2;
	}
	
	public static final void glVertex2d(double x, double y) {
		glVertex2f((float)x, (float)y);
	}
	
	public static final void glVertex2i(int x, int y) {
		glVertex2f(x, y);
	}
	
	public static final void glVertex3f(float x, float y, float z) {
		int currentPos = ImmediateModeData.vertexPosition;
		if(currentPos > ImmediateModeData.vertexBuffer.getLength()) {
			throw new BufferOverflowException();
		}
		ImmediateModeData.vertexBuffer.set(currentPos, x);
		ImmediateModeData.vertexBuffer.set(currentPos + 1, y);
		ImmediateModeData.vertexBuffer.set(currentPos + 2, z);
		ImmediateModeData.vertexPosition = currentPos + 3;
	}
	
	public static final void glVertex3d(double x, double y, double z) {
		glVertex3f((float)x, (float)y, (float)z);
	}
	
	public static final void glVertex3i(int x, int y, int z) {
		glVertex3f(x, y, z);
	}
	
	public static final void glVertex4f(float x, float y, float z, float w) {
		int currentPos = ImmediateModeData.vertexPosition;
		if(currentPos > ImmediateModeData.vertexBuffer.getLength()) {
			throw new BufferOverflowException();
		}
		ImmediateModeData.vertexBuffer.set(currentPos, x);
		ImmediateModeData.vertexBuffer.set(currentPos + 1, y);
		ImmediateModeData.vertexBuffer.set(currentPos + 2, z);
		ImmediateModeData.vertexBuffer.set(currentPos + 3, w);
		ImmediateModeData.vertexPosition = currentPos + 4;
	}
	
	public static final void glVertexdf(double x, double y, double z, double w) {
		glVertex4f((float)x, (float)y, (float)z, (float)w);
	}
	
	public static final void glVertexdi(int x, int y, int z, int w) {
		glVertex4f(x, y, z, w);
	}
	
	public static final void glEnd() {
		if(ImmediateModeData.mode == -1) {
			return;
		}
		
		//vertex data
		glBindBuffer(GL_ARRAY_BUFFER, new Main.BufferGL(vertexBuffer));
		glBufferData(GL_ARRAY_BUFFER, subArray(ImmediateModeData.vertexBuffer, 0, ImmediateModeData.vertexPosition), GL_STATIC_DRAW);
		glVertexAttribPointer(WebGLShader.a_position, 3, GL_FLOAT, false, 3*4, 0);
		glEnableVertexAttribArray(WebGLShader.a_position);
		
		//texCoord data
		glBindBuffer(GL_ARRAY_BUFFER, new Main.BufferGL(texCoordBuffer));
		glBufferData(GL_ARRAY_BUFFER, subArray(ImmediateModeData.textureCoordBuffer, 0, ImmediateModeData.textureCoordPosition), GL_STATIC_DRAW);
		glVertexAttribPointer(WebGLShader.a_texture0, 2, GL_FLOAT, false, 2*4, 0);
		glEnableVertexAttribArray(WebGLShader.a_texture0);
		
		glDrawArrays(ImmediateModeData.mode, 0, ImmediateModeData.vertexPosition);
		
		ImmediateModeData.mode = -1;
	}
	
	public static final void glLineWidth(float width) {
		webgl.lineWidth(width);
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
		if(mode == GL_QUADS && (count % 4) == 0) {
			for(int i=0; i < count; i += 4) {
				webgl.drawArrays(GL_TRIANGLE_FAN, i, 4);
			}
		} else {
			webgl.drawArrays(mode, first, count);
		}
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
	
	public static final void glEnable(int cap) {
		switch(cap) {
			case GL_RESCALE_NORMAL:
				break;
			case GL_TEXTURE_2D:
				texture2D = true;
				break;
			case GL_LIGHTING:
				lighting = true;
				break;
			case GL_ALPHA_TEST:
				alpha = true;
				break;
			case GL_FOG:
				fogEnabled = true;
				break;
			case GL_COLOR_MATERIAL:
				colorMaterial = true;
				break;
			default:
				webgl.enable(cap);
		}
	}
	
	public static final void glDisable(int cap) {
		switch(cap) {
			case GL_RESCALE_NORMAL:
				break;
			case GL_TEXTURE_2D:
				texture2D = false;
				break;
			case GL_LIGHTING:
				lighting = false;
				break;
			case GL_ALPHA_TEST:
				alpha = false;
				break;
			case GL_FOG:
				fogEnabled = false;
				break;
			case GL_COLOR_MATERIAL:
				colorMaterial = false;
				break;
			default:
				webgl.disable(cap);
		}
	}
	
	public static final boolean glIsEnabled(int cap) {
		switch(cap) {
			case GL_RESCALE_NORMAL:
				return false;
			case GL_TEXTURE_2D:
				return texture2D;
			case GL_LIGHTING:
				return lighting;
			case GL_ALPHA_TEST:
				return alpha;
			case GL_FOG:
				return fogEnabled;
			case GL_COLOR_MATERIAL:
				return colorMaterial;
			default:
				return webgl.isEnabled(cap);
		}
	}
	
	public static final void glNormal3b(byte nx, byte ny, byte nz) {
		float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
		normalX = nx / len;
		normalY = ny / len;
		normalZ = nz / len;
	}
	
	public static final void glNormal3f(float nx, float ny, float nz) {
		float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
		normalX = nx / len;
		normalY = ny / len;
		normalZ = nz / len;
	}
	
	public static final void glNormal3d(double nx, double ny, double nz) {
		float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
		normalX = (float) (nx / len);
		normalY = (float) (ny / len);
		normalZ = (float) (nz / len);
	}
	
	public static final void glNormal3i(int nx, int ny, int nz) {
		float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
		normalX = nx / len;
		normalY = ny / len;
		normalZ = nz / len;
	}
	
	public static final void glPixelStoref(int pname, float param) {
		webgl.pixelStorei(pname, pname);
	}

	public static final void glPixelStorei(int pname, int param) {
		webgl.pixelStorei(pname, pname);
	}
	
	public static final void glColor3b(float red, float green, float blue) {
		colorR = red;
		colorG = green;
		colorB = blue;
		colorA = 1.0f;
	}
	
	public static final void glColor3f(float red, float green, float blue) {
		colorR = red;
		colorG = green;
		colorB = blue;
		colorA = 1.0f;
	}
	
	public static final void glColor3d(double red, double green, double blue) {
		colorR = (float) red;
		colorG = (float) green;
		colorB = (float) blue;
		colorA = 1.0f;
	}
	
	public static final void glColor4b(byte red, byte green, byte blue, byte alpha) {
		colorR = red;
		colorG = green;
		colorB = blue;
		colorA = alpha;
	}
	
	public static final void glColor4f(float red, float green, float blue, float alpha) {
		colorR = red;
		colorG = green;
		colorB = blue;
		colorA = alpha;
	}
	
	public static final void glColor4d(double red, double green, double blue, double alpha) {
		colorR = (float) red;
		colorG = (float) green;
		colorB = (float) blue;
		colorA = (float) alpha;
	}
	
	public static final void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
		webgl.colorMask(red, green, blue, alpha);
	}
	
	public static final void glTexParameteri(int target, int pname, int param) {
		webgl.texParameteri(target, pname, param);
	}
	
	public static final void glMatrixMode(int mode) {
		matrixMode = mode;
	}

	private static final Matrix4f getMatrix() {
		switch (matrixMode) {
		case GL_MODELVIEW:
		default:
			return matModelV[matModelPointer];
		case GL_PROJECTION:
			return matProjV[matProjPointer];
		case GL_TEXTURE:
			return matTexV[matTexPointer];
		}
	}
	
	public static final void glLoadIdentity() {
		getMatrix().setIdentity();
	}
	
	public static final void glOrtho(float left, float right, float bottom, float top, float zNear, float zFar) {
		Matrix4f res = getMatrix();
		res.m00 = 2.0f / (right - left);
		res.m01 = 0.0f;
		res.m02 = 0.0f;
		res.m03 = 0.0f;
		res.m10 = 0.0f;
		res.m11 = 2.0f / (top - bottom);
		res.m12 = 0.0f;
		res.m13 = 0.0f;
		res.m20 = 0.0f;
		res.m21 = 0.0f;
		res.m22 = 2.0f / (zFar - zNear);
		res.m23 = 0.0f;
		res.m30 = -(right + left) / (right - left);
		res.m31 = -(top + bottom) / (top - bottom);
		res.m32 = (zFar + zNear) / (zFar - zNear);
		res.m33 = 1.0f;
	}
	
	public static final void glTranslatef(float x, float y, float z) {
		matrixVector.set(x, y, z);
		getMatrix().translate(matrixVector);
		if (DisplayList.isCompiling) {
			throw new IllegalArgumentException("matrix not supported in display list");
		}
	}
	
	public static final void glTranslated(double x, double y, double z) {
		matrixVector.set((float)x, (float)y, (float)z);
		getMatrix().translate(matrixVector);
		if (DisplayList.isCompiling) {
			throw new IllegalArgumentException("matrix not supported in display list");
		}
	}
	
	private static final float rad = 0.0174532925f;

	public static final void glRotatef(float angle, float x, float y, float z) {
		matrixVector.set(x, y, z);
		getMatrix().rotate(angle * rad, matrixVector);
		if (DisplayList.isCompiling) {
			throw new IllegalArgumentException("matrix not supported in display list");
		}
	}
	
	public static final void glRotated(double angle, double x, double y, double z) {
		matrixVector.set((float)x, (float)y, (float)z);
		getMatrix().rotate((float)angle * rad, matrixVector);
		if (DisplayList.isCompiling) {
			throw new IllegalArgumentException("matrix not supported in display list");
		}
	}
	
	public static final void glScalef(float x, float y, float z) {
		matrixVector.set(x, y, z);
		getMatrix().scale(matrixVector);
		if (DisplayList.isCompiling) {
			throw new IllegalArgumentException("matrix not supported in display list");
		}
	}
	
	public static final void glScaled(double x, double y, double z) {
		matrixVector.set((float)x, (float)y, (float)x);
		getMatrix().scale(matrixVector);
		if (DisplayList.isCompiling) {
			throw new IllegalArgumentException("matrix not supported in display list");
		}
	}
	
	public static final void glPushMatrix() {
		switch (matrixMode) {
		case GL_MODELVIEW:
		default:
			if (matModelPointer < matModelV.length - 1) {
				++matModelPointer;
				matModelV[matModelPointer].load(matModelV[matModelPointer - 1]);
			} else {
				System.err.println("modelview matrix stack overflow");
			}
			break;
		case GL_PROJECTION:
			if (matProjPointer < matProjV.length - 1) {
				++matProjPointer;
				matProjV[matProjPointer].load(matProjV[matProjPointer - 1]);
			} else {
				System.err.println("projection matrix stack overflow");
			}
			break;
		case GL_TEXTURE:
			if (matTexPointer < matTexV.length - 1) {
				++matTexPointer;
				matTexV[matTexPointer].load(matTexV[matTexPointer - 1]);
			} else {
				System.err.println("texture matrix stack overflow");
			}
			break;
		}
	}
	
	public static final void glPopMatrix() {
		switch (matrixMode) {
		case GL_MODELVIEW:
		default:
			if (matModelPointer > 0) {
				--matModelPointer;
			} else {
				System.err.println("modelview matrix stack underflow");
			}
			break;
		case GL_PROJECTION:
			if (matProjPointer > 0) {
				--matProjPointer;
			} else {
				System.err.println("projection matrix stack underflow");
			}
			break;
		case GL_TEXTURE:
			if (matTexPointer > 0) {
				--matTexPointer;
			} else {
				System.err.println("texture matrix stack underflow");
			}
			break;
		}
	}
	
	public static final void glGetFloat(int pname, FloatBuffer param) {
		switch (pname) {
		case GL_MODELVIEW_MATRIX:
		default:
			matModelV[matModelPointer].store(param);
			break;
		case GL_PROJECTION_MATRIX:
			matProjV[matProjPointer].store(param);
			break;
		}
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
	
	public static final void glBufferData(int target, Float32Array data, int usage) {
		webgl.bufferData(target, data, usage);
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
		mode = (mode | ((colorMaterial && lighting) ? Main.WebGLShader.LIGHTING : 0));
		mode = (mode | (fogEnabled ? Main.WebGLShader.FOG : 0));
		mode = (mode | (alpha ? Main.WebGLShader.ALPHATEST : 0));
		mode = (mode | (texture2D ? Main.WebGLShader.UNIT0 : 0));
		return mode;
	}
	
	private static int getShaderMode() {
		int mode = 0;
		mode = (mode | (colorArray ? Main.WebGLShader.COLOR : 0));
		mode = (mode | (normalArray ? Main.WebGLShader.NORMAL : 0));
		mode = (mode | (textureArray ? Main.WebGLShader.TEXTURE0 : 0));
		mode = (mode | ((colorMaterial && lighting) ? Main.WebGLShader.LIGHTING : 0));
		mode = (mode | (fogEnabled ? Main.WebGLShader.FOG : 0));
		mode = (mode | (alpha ? Main.WebGLShader.ALPHATEST : 0));
		mode = (mode | (texture2D ? Main.WebGLShader.UNIT0 : 0));
		return mode;
	}
	
	public static final void glBindShaders() {
		glBindShaders(getShaderMode());
	}
	
	public static final void glBindShaders(int i) {
		Main.WebGLShader s = WebGLShader = Main.WebGLShader.instance(i);
		s.use();
		if (alpha) {
			s.alphaTest(alphaValue);
		}
		s.color(colorR, colorG, colorB, colorA);
		if (fogEnabled) {
			s.fogMode((fogPremultiply ? 2 : 0) + fogMode);
			s.fogColor(fogColorR, fogColorG, fogColorB, fogColorA);
			s.fogDensity(fogDensity);
			s.fogStartEnd(fogStart, fogEnd);
		}
		s.modelMatrix(matModelV[matModelPointer]);
		s.projectionMatrix(matProjV[matProjPointer]);
		s.textureMatrix(matTexV[matTexPointer]);
		if (colorMaterial && lighting) {
			s.normal(normalX, normalY, normalZ);
			s.lightPositions(lightPos0vec, lightPos1vec);
		}
		s.tex0Coords(tex0X, tex0Y);
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
	
	public static final void glFogi(int pname, int param) {
		if (pname == GL_FOG_MODE) {
			switch (param) {
				default:
				case GL_LINEAR:
					fogMode = 1;
					break;
				case GL_EXP:
					fogMode = 2;
					break;
			}
		}
	}

	public static final void glFogf(int pname, float param) {
		switch (pname) {
			case GL_FOG_START:
				fogStart = param;
				break;
			case GL_FOG_END:
				fogEnd = param;
				break;
			case GL_FOG_DENSITY:
				fogDensity = param;
				break;
			default:
				break;
		}
	}

	public static final void glFog(int pname, FloatBuffer param) {
		if (pname == GL_FOG_COLOR) {
			fogColorR = param.get();
			fogColorG = param.get();
			fogColorB = param.get();
			fogColorA = param.get();
		}
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

	public static final void gluUnProject(float p1, float p2, float p3, FloatBuffer p4, FloatBuffer p5, int[] p6, FloatBuffer p7) {
		unprojA.load(p4);
		unprojB.load(p5);
		Matrix4f.mul(unprojA, unprojB, unprojB);
		unprojB.invert();
		unprojC.set(((p1 - (float) p6[0]) / (float) p6[2]) * 2f - 1f, ((p2 - (float) p6[1]) / (float) p6[3]) * 2f - 1f,
				p3, 1.0f);
		Matrix4f.transform(unprojB, unprojC, unprojC);
		p7.put(unprojC.x / unprojC.w);
		p7.put(unprojC.y / unprojC.w);
		p7.put(unprojC.z / unprojC.w);
	}

	public static final void gluPerspective(float fovy, float aspect, float zNear, float zFar) {
		Matrix4f res = getMatrix();
		float cotangent = (float) Math.cos(fovy * rad * 0.5f) / (float) Math.sin(fovy * rad * 0.5f);
		res.m00 = cotangent / aspect;
		res.m01 = 0.0f;
		res.m02 = 0.0f;
		res.m03 = 0.0f;
		res.m10 = 0.0f;
		res.m11 = cotangent;
		res.m12 = 0.0f;
		res.m13 = 0.0f;
		res.m20 = 0.0f;
		res.m21 = 0.0f;
		res.m22 = (zFar + zNear) / (zFar - zNear);
		res.m23 = -1.0f;
		res.m30 = 0.0f;
		res.m31 = 0.0f;
		res.m32 = 2.0f * zFar * zNear / (zFar - zNear);
		res.m33 = 0.0f;
	}

	public static final void gluPerspectiveFlat(float fovy, float aspect, float zNear, float zFar) {
		Matrix4f res = getMatrix();
		float cotangent = (float) Math.cos(fovy * rad * 0.5f) / (float) Math.sin(fovy * rad * 0.5f);
		res.m00 = cotangent / aspect;
		res.m01 = 0.0f;
		res.m02 = 0.0f;
		res.m03 = 0.0f;
		res.m10 = 0.0f;
		res.m11 = cotangent;
		res.m12 = 0.0f;
		res.m13 = 0.0f;
		res.m20 = 0.0f;
		res.m21 = 0.0f;
		res.m22 = ((zFar + zNear) / (zFar - zNear)) * 0.001f;
		res.m23 = -1.0f;
		res.m30 = 0.0f;
		res.m31 = 0.0f;
		res.m32 = 2.0f * zFar * zNear / (zFar - zNear);
		res.m33 = 0.0f;
	}
	
	public static final String gluErrorString(int p1) {
		switch (p1) {
		case GL_INVALID_ENUM:
			return "GL_INVALID_ENUM";
		case GL_INVALID_VALUE:
			return "GL_INVALID_VALUE";
		case GL_INVALID_OPERATION:
			return "GL_INVALID_OPERATION";
		case GL_OUT_OF_MEMORY:
			return "GL_OUT_OF_MEMORY";
		case GL_CONTEXT_LOST_WEBGL:
			return "CONTEXT_LOST_WEBGL";
		default:
			return "Unknown Error";
		}
	}
	
	public static final int glGetError() {
		int err = webgl.getError();
		return err;
	}
	
	public static final int glGetVertexes() {
		int ret = vertexDrawn;
		vertexDrawn = 0;
		return ret;
	}

	public static final int glGetTriangles() {
		int ret = triangleDrawn;
		triangleDrawn = 0;
		return ret;
	}
	
	@JSBody(params = {"buf", "i", "i2"}, script = "return buf.subarray(i, i2);")
	private static native Float32Array subArray(Float32Array buf, int i, int i2);
	
	private static class ImmediateModeData {
		public static int mode = -1;
		public static Float32Array vertexBuffer = Float32Array.create(32);
		public static int vertexPosition = 0;
		public static Float32Array textureCoordBuffer = Float32Array.create(32);
		public static int textureCoordPosition = 0;
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
		
		fogColorR = 1.0f;
		fogColorG = 1.0f;
		fogColorB = 1.0f;
		fogColorA = 1.0f;
		fogMode = 1;
		fogEnabled = false;
		fogPremultiply = false;
		fogStart = 1.0f;
		fogEnd = 1.0f;
		fogDensity = 1.0f;
		
		texture2D = false;
		lighting = false;
		colorMaterial = false;
		
		normalX = 1.0f;
		normalY = 0.0f;
		normalZ = 0.0f;
		tex0X = 0;
		tex0Y = 0;
		
		colorR = 1.0f;
		colorG = 1.0f;
		colorB = 1.0f;
		colorA = 1.0f;
		
		matrixMode = GL_MODELVIEW;
		matModelV = new Matrix4f[32];
		matModelPointer = 0;
		matProjV = new Matrix4f[6];
		matProjPointer = 0;
		matTexV = new Matrix4f[16];
		matTexPointer = 0;
		
		for (int i = 0; i < matModelV.length; ++i) {
			matModelV[i] = new Matrix4f();
		}
		
		for (int i = 0; i < matProjV.length; ++i) {
			matProjV[i] = new Matrix4f();
		}
		
		for (int i = 0; i < matTexV.length; ++i) {
			matTexV[i] = new Matrix4f();
		}
		
		matrixVector = new Vector3f();
		
		unprojA = new Matrix4f();
		unprojB = new Matrix4f();
		unprojC = new Vector4f();
		
		lightPos1vec = new Vector4f();
		lightPos0vec = new Vector4f();
		
		vertexBuffer = webgl.createBuffer();
		texCoordBuffer = webgl.createBuffer();
	}
	
	private static Main.WebGLShader WebGLShader = null;
	
}
