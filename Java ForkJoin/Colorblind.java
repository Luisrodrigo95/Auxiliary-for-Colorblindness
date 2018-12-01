import java.awt.image.BufferedImage;
import java.util.concurrent.RecursiveAction;
import java.lang.Math;

public class Colorblind extends RecursiveAction{
    private static final long MIN = 681L;
    private BufferedImage img;
    private int start, width, start2, height;
    public Colorblind(BufferedImage img, int start1, int width, int start2, int height) {
        this.img = img;
        this.start = start1;
        this.width = width;
        this.start2 = start2;
        this.height = height;
    }

    private void ChangeColorScale() {
        for (int i = start; i < width; i++) {
            for (int j = start2; j < height; j++) {
                int pixel = img.getRGB(i, j);

                int alfa = (pixel >> 24) & 0xff;
                float r = ((pixel >> 16) & 0xff) / 255.0f;
                float g = ((pixel >> 8) & 0xff) / 255.0f;
                float b = ((pixel & 0xff) / 255.0f );
                float h,s,v;
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
    
                    h = (h+140.0f);
                    s = s ;
                    v = v;
                } else {
                    h = h;
                    s = s;
                    v = v;
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

                // Remplaza los valores por los valores en escala de grises
                pixel = (alfa << 24) | ((Math.round(r)*255) << 16) | ((Math.round(g)*255) << 8) | ((Math.round(b)*255));
    
                img.setRGB(i, j, pixel);
            }
        }
    }

    protected void compute() {
        if ((this.width - this.start <= Colorblind.MIN)) {
            ChangeColorScale();
        } else {
            int mid = (width + start) / 2;
            invokeAll(new Colorblind(img, start, mid, start2, height), new Colorblind(img, mid, width, start2, height));
        }
    }
}

