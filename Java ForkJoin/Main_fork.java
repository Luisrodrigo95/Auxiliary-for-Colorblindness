import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ForkJoinPool;
import java.io.IOException;
import java.lang.Math;


public class Main_fork {
    private static final int MAXTHREADS = Runtime.getRuntime().availableProcessors();
    public static void main(String args[]) {
        double start, stop;
        double time = 0;
        File file;
        BufferedImage img = null;
        String path = "untitled.png";

        // Leer la imagen
        try {
            file = new File(path);
            img = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Obtener el tamaño de la imagen, ancho y altura
        int height = img.getHeight();
        int width = img.getWidth();


        for (int i = 1; i <= Utils.N; i++) {
            // inicializa el contador de tiempo
            start = System.currentTimeMillis();
            // Convertir a escalala de HSV y de regreso
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    int pixel = img.getRGB(col, row);
                    float h, s, v;
                    int alfa = ((pixel >> 24) & 0xff);
                    float b = ((pixel >> 16) & 0xff) / 255.0f;
                    float g = ((pixel >> 8) & 0xff) / 255.0f;
                    float r = ((pixel & 0xff) / 255.0f );
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
                    float maxh=200.0f;

                    if (h > minh && h < maxh ){
		
                        h = (h + 140.0f);
                        s = s ;
                        v = v;
                    } else {
                        h = h;
                        s = s;
                        v = v;
                    }

                    float f = h / 60.0f;
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
                    pixel = (alfa << 24) | ((Math.round(b)*255) << 16) | ((Math.round(g)*255) << 8) | ((Math.round(r)*255));
                    /*
                       float red = ((pixel >> 16) & 0xff) / 255.0f;
                    float green = ((pixel >> 8) & 0xff) / 255.0f;
                    float blue = (pixel & 0xff);*/
                    img.setRGB(col, row, pixel);
                }
            }
            // Finaliza el contador de tiempo
            stop = System.currentTimeMillis();
            time += (stop - start);
        }

        // Imprime los resultados para SINGLE THREAD
        System.out.println("\nSINGLE THREAD");
        System.out.println("Tiempo en ms: " + (time/Utils.N) + "ms");
        // Crea la imagen resultante
        try {
            file = new File("Output.jpg");
            ImageIO.write(img, "jpg", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Fork-join
        double start_join, stop_join, time_join = 0;

        ForkJoinPool pool;

        for (int j = 0; j < Utils.N; j++) {
            // inicializa el contador de tiempo
            start_join = System.currentTimeMillis();
            //inicia el pool para fork-join
            pool = new ForkJoinPool(MAXTHREADS);
            //invoca la clase
            pool.invoke(new Colorblind(img, 0, width, 0, height));
            //detiene el timer
            stop_join = System.currentTimeMillis();
            //suma el tiempo
            time_join += (stop_join - start_join);
        }
        
        try {
            file = new File("Output-Fork.jpg");
            ImageIO.write(img, "jpg", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Imprime los resultados para Fork-Join
        System.out.println("\nThreads: " + MAXTHREADS);
        System.out.println("\nFork-Join");
        System.out.println("Tiempo en ms: " + (time_join/Utils.N) + "ms");

    }
}