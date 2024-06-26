package org.lwjgl.util;

import java.io.Serializable;
import java.nio.ByteBuffer;

public final class Color implements ReadableColor, Serializable, WritableColor {

    static final long serialVersionUID = 1L;

    private byte red, green, blue, alpha;

    public Color() {
		this(0, 0, 0, 255);
	}

    public Color(int r, int g, int b) {
		this(r, g, b, 255);
	}

    public Color(byte r, byte g, byte b) {
		this(r, g, b, (byte) 255);
	}

    public Color(int r, int g, int b, int a) {
		set(r, g, b, a);
	}

    public Color(byte r, byte g, byte b, byte a) {
		set(r, g, b, a);
	}

    public Color(ReadableColor c) {
		setColor(c);
	}

    public void set(int r, int g, int b, int a) {
		red = (byte) r;
		green = (byte) g;
		blue = (byte) b;
		alpha = (byte) a;
	}

    public void set(byte r, byte g, byte b, byte a) {
		this.red = r;
		this.green = g;
		this.blue = b;
		this.alpha = a;
	}

    public void set(int r, int g, int b) {
		set(r, g, b, 255);
	}

    public void set(byte r, byte g, byte b) {
		set(r, g, b, (byte) 255);
	}

    public int getRed() {
		return red & 0xFF;
	}

    public int getGreen() {
		return green & 0xFF;
	}

    public int getBlue() {
		return blue & 0xFF;
	}

    public int getAlpha() {
		return alpha & 0xFF;
	}

    public void setRed(int red) {
		this.red = (byte) red;
	}

    public void setGreen(int green) {
		this.green = (byte) green;
	}

    public void setBlue(int blue) {
		this.blue = (byte) blue;
	}

    public void setAlpha(int alpha) {
		this.alpha = (byte) alpha;
	}

    public void setRed(byte red) {
		this.red = red;
	}

    public void setGreen(byte green) {
		this.green = green;
	}

	public void setBlue(byte blue) {
		this.blue = blue;
	}

	public void setAlpha(byte alpha) {
		this.alpha = alpha;
	}

	public String toString() {
		return "Color [" + getRed() + ", " + getGreen() + ", " + getBlue() + ", " + getAlpha() + "]";
	}

    public boolean equals(Object o) {
		return (o != null) && (o instanceof ReadableColor) && (((ReadableColor) o).getRed() == this.getRed()) && (((ReadableColor) o).getGreen() == this.getGreen()) && (((ReadableColor) o).getBlue() == this.getBlue()) && (((ReadableColor) o).getAlpha() == this.getAlpha());
	}

    public int hashCode() {
		return (red << 24) | (green << 16) | (blue << 8) | alpha;
	}

    public byte getAlphaByte() {
		return alpha;
	}

    public byte getBlueByte() {
		return blue;
	}

    public byte getGreenByte() {
		return green;
	}

	public byte getRedByte() {
		return red;
	}

	public void writeRGBA(ByteBuffer dest) {
		dest.put(red);
		dest.put(green);
		dest.put(blue);
		dest.put(alpha);
	}

	public void writeRGB(ByteBuffer dest) {
		dest.put(red);
		dest.put(green);
		dest.put(blue);
	}

	public void writeABGR(ByteBuffer dest) {
		dest.put(alpha);
		dest.put(blue);
		dest.put(green);
		dest.put(red);
	}

	public void writeARGB(ByteBuffer dest) {
		dest.put(alpha);
		dest.put(red);
		dest.put(green);
		dest.put(blue);
	}
	
	public void writeBGR(ByteBuffer dest) {
		dest.put(blue);
		dest.put(green);
		dest.put(red);
	}
	
	public void writeBGRA(ByteBuffer dest) {
		dest.put(blue);
		dest.put(green);
		dest.put(red);
		dest.put(alpha);
	}
	
	public void readRGBA(ByteBuffer src) {
		red = src.get();
		green = src.get();
		blue = src.get();
		alpha = src.get();
	}

	public void readRGB(ByteBuffer src) {
		red = src.get();
		green = src.get();
		blue = src.get();
	}

	public void readARGB(ByteBuffer src) {
		alpha = src.get();
		red = src.get();
		green = src.get();
		blue = src.get();
	}

	public void readBGRA(ByteBuffer src) {
		blue = src.get();
		green = src.get();
		red = src.get();
		alpha = src.get();
	}

	public void readBGR(ByteBuffer src) {
		blue = src.get();
		green = src.get();
		red = src.get();
	}

	public void readABGR(ByteBuffer src) {
		alpha = src.get();
		blue = src.get();
		green = src.get();
		red = src.get();
	}

	public void setColor(ReadableColor src) {
		red = src.getRedByte();
		green = src.getGreenByte();
		blue = src.getBlueByte();
		alpha = src.getAlphaByte();
	}
	
    public void fromHSB(float hue, float saturation, float brightness) {
		if (saturation == 0.0F) {
			red = green = blue = (byte) (brightness * 255F + 0.5F);
		} else {
			float f3 = (hue - (float) Math.floor(hue)) * 6F;
			float f4 = f3 - (float) Math.floor(f3);
			float f5 = brightness * (1.0F - saturation);
			float f6 = brightness * (1.0F - saturation * f4);
			float f7 = brightness * (1.0F - saturation * (1.0F - f4));
			switch ((int) f3) {
				case 0 :
					red = (byte) (brightness * 255F + 0.5F);
					green = (byte) (f7 * 255F + 0.5F);
					blue = (byte) (f5 * 255F + 0.5F);
					break;
				case 1 :
					red = (byte) (f6 * 255F + 0.5F);
					green = (byte) (brightness * 255F + 0.5F);
					blue = (byte) (f5 * 255F + 0.5F);
					break;
				case 2 :
					red = (byte) (f5 * 255F + 0.5F);
					green = (byte) (brightness * 255F + 0.5F);
					blue = (byte) (f7 * 255F + 0.5F);
					break;
				case 3 :
					red = (byte) (f5 * 255F + 0.5F);
					green = (byte) (f6 * 255F + 0.5F);
					blue = (byte) (brightness * 255F + 0.5F);
					break;
				case 4 :
					red = (byte) (f7 * 255F + 0.5F);
					green = (byte) (f5 * 255F + 0.5F);
					blue = (byte) (brightness * 255F + 0.5F);
					break;
				case 5 :
					red = (byte) (brightness * 255F + 0.5F);
					green = (byte) (f5 * 255F + 0.5F);
					blue = (byte) (f6 * 255F + 0.5F);
					break;
			}
		}
	}

	public float[] toHSB(float dest[]) {
		int r = getRed();
		int g = getGreen();
		int b = getBlue();
		if (dest == null) {
			dest = new float[3];
        }
		int l = r <= g ? g : r;
		if (b > l) {
			l = b;
        }
		int i1 = r >= g ? g : r;
		if (b < i1) {
			i1 = b;
        }
		float brightness = l / 255F;
		float saturation;
		if (l != 0) {
			saturation = (float) (l - i1) / (float) l;
		} else {
			saturation = 0.0F;
        }
		float hue;
		if (saturation == 0.0F) {
			hue = 0.0F;
		} else {
			float f3 = (float) (l - r) / (float) (l - i1);
			float f4 = (float) (l - g) / (float) (l - i1);
			float f5 = (float) (l - b) / (float) (l - i1);
			if (r == l) {
				hue = f5 - f4;
			} else if (g == l) {
				hue = (2.0F + f3) - f5;
			} else {
				hue = (4F + f4) - f3;
            }
			hue /= 6F;
			if (hue < 0.0F) {
				hue++;
            }
		}
		dest[0] = hue;
		dest[1] = saturation;
		dest[2] = brightness;
		return dest;
	}
}