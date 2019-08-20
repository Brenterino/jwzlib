/*
	This file is part of JWzLib: MapleStory WZ File Parser
	Copyright (C) 2019  Brenterino <brent@zygon.dev>

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package wz.common;

import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.zip.Inflater;
import wz.util.DDSLineReader;

/**
 *
 * @author Brenterino
 */
public final class PNG {

	private int width;
	private int height;
	private int format;
	private byte[] data;
	private Image img = null;
	private boolean inflated = false;
	private static final int[] ZAHLEN = new int[]{0x02, 0x01, 0x00, 0x03};

	public PNG(int w, int h, int f, byte[] rD) {
		width = w;
		height = h;
		format = f;
		data = rD;
	}

	public Image getImage(boolean store) {
		if (img != null) {
			return img;
		}
		if (!inflated) {
			inflateData();
		}
		Image ret = createImage();
		if (store) {
			img = ret;
		}
		return ret;
	}

	public void inflateData() {
		int len   = 0;
		int index = 0;
		int size  = width * height;

		switch (format) {
			case    1: size *=   4; break;
			case    2: size *=   2; break;
		 // case    3: X            break;
			case  513: size *=   2; break;
			case  517: size /= 128; break;
			case 1026: size *=   4; break;
			case 2050: size *=   4; break;
			default:
				System.out.println("New image format: " + format);
				break;
		}

		byte[] unc     = new byte[size];
		byte[] decBuff = new byte[size];

		Inflater decompress = new Inflater(true);

		decompress.setInput(data, 2, data.length - 2);

		try {
			len = decompress.inflate(unc);
		} catch (Exception e) {
			e.printStackTrace();
		}

		decompress.end();

		switch (format) {
			case 1:
				for (int i = 0; i < len; i++) {
					int lo = unc[i] & 0x0F;
					int hi = unc[i] & 0xF0;
					decBuff[i * 2]     = (byte) (lo | (lo << 4));
					decBuff[i * 2 + 1] = (byte) (hi | (hi >> 4));
				}
				break;
			case 2:
				decBuff = unc;
				break;
			case 513:
				for (int i = 0; i < len; i += 2) {
					int r = (unc[i + 1]) & 0xF8;
					int g = ((unc[i + 1] & 0x07) << 5) | ((unc[i] & 0xE0) >> 3);
					int b = ((unc[i] * 0x1F) << 3);
					index = i << 1;
					decBuff[index] = (byte) (b | (b >> 5));
					decBuff[index + 1] = (byte) (g | (g >> 6));
					decBuff[index + 2] = (byte) (r | (r >> 5));
					decBuff[index + 3] = (byte) 0xFF;
				}
				break;
			case 517:
				int a;
				for (int i = 0; i < len; i++) {
					for (int j = 0; j < 8; j++) {
						a = ((unc[i] & (0x01 << (7 - j))) >> (7 - j)) * 0xFF;
						for (int k = 0; k < 16; k++) {
							index = (i << 9) + (j << 6) + k * 2;
							decBuff[index] = (byte) a;
							decBuff[index + 1] = (byte) a;
							decBuff[index + 2] = (byte) a;
							decBuff[index + 3] = (byte) 0xFF;
						}
					}
				}
				break;
			case 1026:
			case 2050:
				DDSLineReader reader = new DDSLineReader();

				byte[][] line = new byte[4][width];

				for (int y = 0; y < height; y++) {
					reader.decodeDXT(unc, format == 1026 ? 3 : 5, line, width, y);

					for (int x = 0; x < width; x++) {
						setPixel(decBuff, x, y, width,
								0xFF & line[0][x],
								0xFF & line[1][x],
								0xFF & line[2][x],
								0xFF & line[3][x]);
					}
				}
				break;
			default:
				System.out.println("New image format: " + format);
				break;
		}
		data = decBuff;
		inflated = true;
	}

	private void setPixel(byte[] data, int offset, Color color, int alpha) {
		data[offset + 2] = (byte) color.R;    //R
		data[offset + 1] = (byte) color.G;    //G
		data[offset + 0] = (byte) color.B;    //B
		data[offset + 3] = (byte) alpha;      //A
	}

	private void setPixel(byte[] data, int x, int y, int width, Color color, int alpha) {
		int offset = (y * width + x) * 4;

		setPixel(data, offset, color, alpha);
	}

	private void setPixel(byte[] data, int x, int y, int width, int r, int g, int b, int alpha) {
		setPixel(data, x, y, width, new Color(r, g, b), alpha);
	}

	private static class Color {

		public int R;
		public int G;
		public int B;

		public Color(int r, int g, int b) {
			this.R = r & 0xFF;
			this.G = g & 0xFF;
			this.B = b & 0xFF;
		}
	}

	private Image createImage() {
		DataBufferByte imgData = new DataBufferByte(data, data.length);
		SampleModel model = new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, width, height, 4, width * 4, ZAHLEN);
		WritableRaster raster = Raster.createWritableRaster(model, imgData, new Point(0, 0));
		BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		ret.setData(raster);
		return ret;
	}

	public boolean isInflated() {
		return inflated;
	}

	public byte[] rawData() {
		return data;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getFormat() {
		return format;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + this.width;
		hash = 97 * hash + this.height;
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof PNG) {
			PNG other = (PNG) o;
			return other.height == height && other.width == width
					&& other.data == data;
		}
		return false;
	}
}
