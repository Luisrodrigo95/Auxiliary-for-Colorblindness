import java.awt.image.BufferedImage;

import java.io.File;

import javax.imageio.ImageIO;

import java.lang.Math;

public class Threads extends Thread {

    private int src[], dest[], width, height, start, end;

    public Threads(int src[], int dest[], int width, int height, int start, int end) {

		super();

		this.src = src;

		this.dest = dest;

		this.width = width;

		this.height = height;

		this.start = start;

		this.end = end;

	}

    private void compute_pixel(int ren, int col) {

        int pixel, dpixel;
        float h, s, v;
        float r, b, g;
        pixel = src[(ren * width) + col];
        r = ((float) ((pixel & 0x00ff0000) >> 16) / 255.0f);
        g = ((float) ((pixel & 0x0000ff00) >> 8) / 255.0f);
        b = ((float) ((pixel & 0x000000ff) >> 0 ) / 255.0f);

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float diff = max - min;
        v = max;
        
        if(v == 0.0f) { 		// black
            h = s = 0.0f;
        } else {
            s = diff / v;
            if(diff < 0.001f) { // grey
                h = 0.0f;
            } else { 			// color
                if(max == r) {
                    h = 60.0f * (g - b)/diff;
                    if(h < 0.0f) { h += 360.0f; }
                    } else if(max == g) {
                        h = 60.0f * (2 + (b - r) / diff);
                    } else {
                        h = 60.0f * (4 + (r - g) / diff);
                    }
            }		
        }
            // if to check the color blindness line, if the pixel is in this line i change the color to other color base shifting the h
        float minh=40.0f;
        float maxh=120.0f;

        if (h > minh && h < maxh ){

            h = (h + 140.0f);
        }

        float f = h/60.0f;
        float hi = (float) Math.floor(f);
        f = f - hi;
        float p = v * (1 - s);
        float q = v * (1 - s * f);
        float t = v * (1 - s * (1 - f));
        
        if(hi == 0.0f || hi == 6.0f) {
            r = v;
            g = t;
            b = p;
        } else if(hi == 1.0f) {
            r = q;
            g = v;
            b = p;
        } else if(hi == 2.0f) {
            r = p;
            g = v;
            b = t;
        } else if(hi == 3.0f) {
            r = p;
            g = q;
            b = v;
        } else if(hi == 4.0f) {
            r = t;
            g = p;
            b = v;
        } else {
            r = v;
            g = p;
            b = q;
        }

        dpixel = (0xff000000)

                | (((int) (r*255.0f )) << 16)

                | (((int) (g*255.0f )) << 8)

                | (((int) (b*255.0f )) << 0);

        dest[(ren * width) + col] = dpixel;

    }

    public void run() {

        int index, size;

        int ren, col;

        size = width * height;

        for (index = start; index < end; index++) {

            ren = index / width;

            col = index % width;

            compute_pixel(ren, col);

        }

    }

}