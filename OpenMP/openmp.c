/* This code will generate a fractal image. Uses OpenCV, to compile:
   gcc openmp.c `pkg-config --cflags --libs opencv` -lm */
#include <stdio.h>
#include <stdlib.h>
#include <opencv/highgui.h>
#include "utils/cheader.h"


typedef enum color {BLUE, GREEN, RED} Color;

void compute_pixel(IplImage *src, IplImage *dest , int ren, int col) {
	int step;
	float r, g, b;
	float h, s, v;
	
	step = src->widthStep / sizeof(uchar);
	
	r = (float) (src->imageData[(ren * step) + (col * src->nChannels) + RED] / 255.0f);
	g = (float) (src->imageData[(ren * step) + (col * src->nChannels) + GREEN] / 255.0f);
	b = (float) (src->imageData[(ren * step) + (col * src->nChannels) + BLUE] / 255.0f);
	
	float max = fmax(r, fmax(g, b));
	float min = fmin(r, fmin(g, b));
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
	float maxh=160.0f;
/*
	float minis = 0.0f;
	float maxs = 100.0f;
	float miniv = 0.0f;
	float maxv = 100.0f;
	*/	

	if (h > minh && h < maxh ){
		
		h = (h+140.0f);
		s = s ;
		v = v;
	} else { // this keep the pixel if it is out of the color blindnessline
		h = h;
		s = s;
		v = v;		
	}
	
	float f = h/60.0f;
	float hi = floorf(f);
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
	dest->imageData[(ren * step) + (col * dest->nChannels) + RED] =  (unsigned char) (r * 255.0f );
	dest->imageData[(ren * step) + (col * dest->nChannels) + GREEN] = (unsigned char) (g * 255.0f );
	dest->imageData[(ren * step) + (col * dest->nChannels) + BLUE] = (unsigned char) (b * 255.0f);
}
	
void compute_image(IplImage *src, IplImage *dest) {
	int index, size, step;
    int ren, col;
    
    size = src->width * src->height;
	#pragma omp parallel for shared(size, src, dest) private(ren, col)
    for (index = 0; index < size; index++) {
    	ren = index / src->width;
    	col = index % src->width;
    	compute_pixel(src, dest, ren, col);
    }
}
void compute_image_seq(IplImage *src, IplImage *dest) {
	int index, size, step;
    int ren, col;
    
    size = src->width * src->height;
    for (index = 0; index < size; index++) {
    	ren = index / src->width;
    	col = index % src->width;
    	compute_pixel(src, dest, ren, col);
    }
}

int main(int argc, char* argv[]) {
	int i;
	double acum, acum2; 	
	
	if (argc != 2) {
		printf("usage: %s source_file\n", argv[0]);
		return -1;
	}
	
	IplImage *src = cvLoadImage(argv[1], CV_LOAD_IMAGE_COLOR);
	IplImage *dest = cvCreateImage(cvSize(src->width, src->height), IPL_DEPTH_8U, 3);
	if (!src) {
		printf("Could not load image file: %s\n", argv[1]);
		return -1;
	}
	
	printf("Starting MultiThreading...\n");	
	acum = 0;
	for (i = 0; i < N; i++) {
		start_timer();
		compute_image(src, dest);
		acum += stop_timer();
	}
	printf("avg time Multithreading= %.5lf ms\n", (acum / N));	

	printf("Starting Sequential...\n");
	acum2 = 0;
	for (i = 0; i < N; i++) {
		start_timer();
		compute_image_seq(src, dest);
		acum2 += stop_timer();
	}
	
	
	printf("avg time Sequential = %.5lf ms\n", (acum2 / N));
	
	cvShowImage("Image (Original)", src);
	cvShowImage("Image (Compute)", dest);
	cvWaitKey(0);
	cvDestroyWindow("Image (Original)");
	cvDestroyWindow("Image (Compute)");

	return 0;
}
