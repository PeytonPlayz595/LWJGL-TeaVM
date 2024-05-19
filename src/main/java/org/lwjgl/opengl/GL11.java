package org.lwjgl.opengl;

import org.teavm.jso.typedarrays.Float32Array;
import org.teavm.jso.webgl.WebGLUniformLocation;

import main.Main;
import main.Main.BufferGL;
import main.Main.WebGL2RenderingContext;

public class GL11 extends Main.GLEnums {
	
	static WebGL2RenderingContext webgl = null;
	
	private static float alphaValue;
	private static boolean alpha;
	
	private static int currentWebGLProgram;
	
	//Uniform functions
	private static Float32Array mat2;
	private static Float32Array mat3;
	private static Float32Array mat4;
	
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
	
	private static int getShaderMode() {
		int mode = 0;
		mode = (mode | (alpha ? Main.WebGLShader.ALPHATEST : 0));
		return mode;
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
	
	static {
		webgl = Main.webgl;
		
		
		alphaValue = 0.1f;
		alpha = false;
		
		currentWebGLProgram = -1;
		
		//Uniform
		mat2 = Float32Array.create(4);
		mat3 = Float32Array.create(9);
		mat4 = Float32Array.create(16);
	}
	
	private static Main.WebGLShader WebGLShader = null;
	
}
