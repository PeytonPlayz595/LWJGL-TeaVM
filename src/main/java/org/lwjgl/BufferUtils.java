package org.lwjgl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

public class BufferUtils {

    /**
     * It's not possible to directly allocate and then convert the buffers in TeaVM
     * So to fix this, I'm wrapping the values to the buffer instead
     */

    public static ByteBuffer createByteBuffer(int size) {
        return ByteBuffer.wrap(new byte[size]).order(ByteOrder.nativeOrder());
    }

    public static ShortBuffer createShortBuffer(int size) {
        return createByteBuffer(size << 1).asShortBuffer();
    }

    public static CharBuffer createCharBuffer(int size) {
		return createByteBuffer(size << 1).asCharBuffer();
	}

    public static IntBuffer createIntBuffer(int size) {
		return createByteBuffer(size << 2).asIntBuffer();
	}

    public static LongBuffer createLongBuffer(int size) {
		return createByteBuffer(size << 3).asLongBuffer();
	}

    public static FloatBuffer createFloatBuffer(int size) {
		return createByteBuffer(size << 2).asFloatBuffer();
	}

    public static DoubleBuffer createDoubleBuffer(int size) {
		return createByteBuffer(size << 3).asDoubleBuffer();
	}

    public static PointerBuffer createPointerBuffer(int size) {
        //Doesn't actually directly allocate
        //I rewrote the PointerBuffer class to use buffer.wrap
		return PointerBuffer.allocateDirect(size);
	}

    public static int getElementSizeExponent(Buffer buf) {
		if (buf instanceof ByteBuffer) {
			return 0;
        } else if (buf instanceof ShortBuffer || buf instanceof CharBuffer) {
			return 1;
        } else if (buf instanceof FloatBuffer || buf instanceof IntBuffer) {
			return 2;
        } else if (buf instanceof LongBuffer || buf instanceof DoubleBuffer) {
			return 3;
        } else {
			throw new IllegalStateException("Unsupported buffer type: " + buf);
        }
	}

    public static int getOffset(Buffer buffer) {
		return buffer.position() << getElementSizeExponent(buffer);
	}

    public static void zeroBuffer(ByteBuffer b) {
	    zeroBuffer0(b, b.position(), b.remaining());
	}

    public static void zeroBuffer(ShortBuffer b) {
	    zeroBuffer0(b, b.position()*2L, b.remaining()*2L);
	}

    public static void zeroBuffer(CharBuffer b) {
	    zeroBuffer0(b, b.position()*2L, b.remaining()*2L);
	}

    public static void zeroBuffer(IntBuffer b) {
	    zeroBuffer0(b, b.position()*4L, b.remaining()*4L);
	}

    public static void zeroBuffer(FloatBuffer b) {
	    zeroBuffer0(b, b.position()*4L, b.remaining()*4L);
	}

    public static void zeroBuffer(LongBuffer b) {
	    zeroBuffer0(b, b.position()*8L, b.remaining()*8L);
	}

    public static void zeroBuffer(DoubleBuffer b) {
	    zeroBuffer0(b, b.position()*8L, b.remaining()*8L);
	}

    //Wrote my own implementation since JNI isn't supported in TeaVM
    //I'm not sure if this even works as intended
    private static void zeroBuffer0(Buffer b, long offset, long length) {
        if (b instanceof ByteBuffer) {
            ((ByteBuffer) b).position((int) offset);
            ((ByteBuffer) b).limit((int) (offset + length));
            ((ByteBuffer) b).put(new byte[(int) length]);
        } else {
            for (long i = offset; i < offset + length; i++) {
                ((ByteBuffer) b).put((int) i, (byte) 0); //Probably very unsafe
            }
        }
    }

    //Not in BufferUtils but thought I would add it anyways
    public static int getBufferSizeInBytes(Buffer buffer) {
    	return buffer.capacity() * getElementSizeExponent(buffer);
    }
}