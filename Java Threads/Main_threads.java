import java.awt.image.BufferedImage;

import java.io.File;

import javax.imageio.ImageIO;

import java.io.IOException;

import java.lang.Math;

public class Main_threads {

    private static final int MAXTHREADS = Runtime.getRuntime().availableProcessors();

    public static void main(String args[]) throws Exception {

        Threads threads[];

        File file;

        int block;

        long startTime, stopTime;

        double acum = 0;

        if (args.length != 1) {

            System.out.println("usage: java Threads image_file");

            System.exit(-1);

        }

        final String fileName = args[0];

        File srcFile = new File(fileName);

        final BufferedImage source = ImageIO.read(srcFile);

        int w = source.getWidth();

        int h = source.getHeight();

        int src[] = source.getRGB(0, 0, w, h, null, 0, w);

        int dest[] = new int[src.length];

        block = src.length / MAXTHREADS;

        threads = new Threads[MAXTHREADS];

        acum = 0;

        for (int j = 1; j <= Utils.N; j++) {

            for (int i = 0; i < threads.length; i++) {

                if (i != threads.length - 1) {

                    threads[i] = new Threads(src, dest, w, h, (i * block), ((i + 1) * block));

                } else {

                    threads[i] = new Threads(src, dest, w, h, (i * block), src.length);

                }

            }

            startTime = System.currentTimeMillis();

            for (int i = 0; i < threads.length; i++) {

                threads[i].start();

            }

            for (int i = 0; i < threads.length; i++) {

                try {

                    threads[i].join();

                } catch (InterruptedException e) {

                    e.printStackTrace();

                }

            }

            stopTime = System.currentTimeMillis();

            acum += (stopTime - startTime);

        }

        System.out.printf("avg time = %.5f\n", (acum / Utils.N));

        final BufferedImage destination = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        destination.setRGB(0, 0, w, h, dest, 0, w);

        try {
            file = new File("Output-Threads.jpg");
            ImageIO.write(destination, "jpg", file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}