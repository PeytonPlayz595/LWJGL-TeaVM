package main;

import org.lwjgl.opengl.GL11;

public class StreamBuffer {
		public final int initialSize;
		public final int initialCount;
		public final int maxCount;

		protected StreamBufferInstance[] buffers;

		protected int currentBufferId = 0;
		protected int overflowCounter = 0;

		protected final IStreamBufferInitializer initializer;

		public static class StreamBufferInstance {

			public BufferArrayGL vertexArray = null;
			public BufferGL vertexBuffer = null;
			protected int vertexBufferSize = 0;

			public boolean bindQuad16 = false;
			public boolean bindQuad32 = false;

			public BufferArrayGL getVertexArray() {
				return vertexArray;
			}

			public BufferGL getVertexBuffer() {
				return vertexBuffer;
			}

		}

		public static interface IStreamBufferInitializer {
			void initialize(BufferArrayGL vertexArray, BufferGL vertexBuffer);
		}

		public StreamBuffer(int initialSize, int initialCount, int maxCount, IStreamBufferInitializer initializer) {
			this.buffers = new StreamBufferInstance[initialCount];
			for(int i = 0; i < this.buffers.length; ++i) {
				this.buffers[i] = new StreamBufferInstance();
			}
			this.initialSize = initialSize;
			this.initialCount = initialCount;
			this.maxCount = maxCount;
			this.initializer = initializer;
		}

		public StreamBufferInstance getBuffer(int requiredMemory) {
			StreamBufferInstance next = buffers[(currentBufferId++) % buffers.length];
			if(next.vertexBuffer == null) {
				next.vertexBuffer = GL11.glCreateBuffer();
			}
			if(next.vertexArray == null) {
				next.vertexArray = GL11.glCreateVertexArray();
				initializer.initialize(next.vertexArray, next.vertexBuffer);
			}
			if(next.vertexBufferSize < requiredMemory) {
				int newSize = (requiredMemory & 0xFFFFF000) + 0x2000;
				GL11.glBindBuffer(WebGL2RenderingContext.ARRAY_BUFFER, next.vertexBuffer);
				WebGL.webgl.bufferData(WebGL2RenderingContext.ARRAY_BUFFER, (int)newSize, WebGL2RenderingContext.STREAM_DRAW);
				next.vertexBufferSize = newSize;
			}
			return next;
		}

		public void optimize() {
			overflowCounter += currentBufferId - buffers.length;
			if(overflowCounter < -25) {
				int newCount = buffers.length - 1 + ((overflowCounter + 25) / 5);
				if(newCount < initialCount) {
					newCount = initialCount;
				}
				if(newCount < buffers.length) {
					StreamBufferInstance[] newArray = new StreamBufferInstance[newCount];
					for(int i = 0; i < buffers.length; ++i) {
						if(i < newArray.length) {
							newArray[i] = buffers[i];
						}else {
							if(buffers[i].vertexArray != null) {
								WebGL.webgl.deleteVertexArray(buffers[i].vertexArray.obj);
							}
							if(buffers[i].vertexBuffer != null) {
								WebGL.webgl.deleteBuffer(buffers[i].vertexBuffer.obj);
							}
						}
					}
					buffers = newArray;
				}
				overflowCounter = 0;
			}else if(overflowCounter > 15) {
				int newCount = buffers.length + 1 + ((overflowCounter - 15) / 5);
				if(newCount > maxCount) {
					newCount = maxCount;
				}
				if(newCount > buffers.length) {
					StreamBufferInstance[] newArray = new StreamBufferInstance[newCount];
					for(int i = 0; i < newArray.length; ++i) {
						if(i < buffers.length) {
							newArray[i] = buffers[i];
						}else {
							newArray[i] = new StreamBufferInstance();
						}
					}
					buffers = newArray;
				}
				overflowCounter = 0;
			}
			currentBufferId = 0;
		}

		public void destroy() {
			for(int i = 0; i < buffers.length; ++i) {
				StreamBufferInstance next = buffers[i];
				if(next.vertexArray != null) {
					WebGL.webgl.deleteVertexArray(next.vertexArray.obj);
				}
				if(next.vertexBuffer != null) {
					WebGL.webgl.deleteBuffer(next.vertexBuffer.obj);
				}
			}
			buffers = new StreamBufferInstance[initialCount];
			for(int i = 0; i < buffers.length; ++i) {
				buffers[i] = new StreamBufferInstance();
			}
		}
	}