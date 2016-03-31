package com.histogram;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class Histogram {
	static final int n = 20000; /* dimension of the image */
	static final int max = 10; /* maximum pixel intensity */
	static final int maxBig = 100; /* maximum pixel intensity */
	static int image[][] = new int[n + 1][n + 1];
	static int hist[] = new int[max + 1];
	static int histBig[] = new int[maxBig + 1];
	static AtomicInteger hist1[] = new AtomicInteger[max + 1];
	static AtomicInteger hist2[] = new AtomicInteger[maxBig + 1];
	static int hist3[] = new int[maxBig + 1];
	static int hist4[] = new int[maxBig + 1];

	private void doSequential(int image[][],int hist[]){
		int i, j, intensity;

		for (i = 1; i <= n; i++) {
			for (j = 1; j <= n; j++) {
				intensity = image[i][j];
				hist[intensity] = hist[intensity] + 1;
			}
		}
	}

	class AtomicComputeHistogram implements Runnable {
		int start;
		int end;
		int image[][];
		AtomicInteger hist[];

		public AtomicComputeHistogram(int image[][],AtomicInteger hist[],int start,int end) {
			this.start = start;
			this.end = end;
			this.image = image;
			this.hist = hist;
		}

		@Override
		public void run() {
			int i, j, intensity;
			for (i = start; i <= end; i++) {
				for (j = 1; j <= n; j++) {
					intensity = image[i][j];
					hist[intensity].addAndGet(1);
				}
			}
		}
	}

	private void doParallelAtomic(int numsOfThreads,int image[][], AtomicInteger hist[]){
		int i;
		Thread thlist[] = new Thread[numsOfThreads];

		for (i = 0; i < numsOfThreads; i++){
			if(i < numsOfThreads - 1){
				thlist[i] = new Thread(new AtomicComputeHistogram(image, hist, i*(n/numsOfThreads)+1, (i+1)*(n/numsOfThreads)));
			}else{
				thlist[i] = new Thread(new AtomicComputeHistogram(image, hist, i*(n/numsOfThreads)+1, n));
			}
			thlist[i].start();
		}
		try{
			for (i = 0; i < numsOfThreads; i++){
				thlist[i].join();
			}
		}catch(InterruptedException e){}
	}

	class ComputeHistogram implements Runnable {
		int start;
		int end;
		int image[][];
		int hist[];

		public ComputeHistogram(int image[][],int hist[],int start,int end) {
			this.start = start;
			this.end = end;
			this.image = image;
			this.hist = hist;
		}

		@Override
		public void run() {
			int i, j, intensity;
			for (i = start; i <= end; i++) {
				for (j = 1; j <= n; j++) {
					intensity = image[i][j];
					hist[intensity]++;
				}
			}
		}
	}

	private void doParallel(int numsOfThreads,int image[][], int resultHist[], int max){
		int i,j;
		Thread thlist[] = new Thread[numsOfThreads];
		int hist[][] = new int[numsOfThreads][max];
		for (i = 0; i < numsOfThreads; i++){
			if(i < numsOfThreads - 1){
				thlist[i] = new Thread(new ComputeHistogram(image, hist[i], i*(n/numsOfThreads)+1, (i+1)*(n/numsOfThreads)));
			}else{
				thlist[i] = new Thread(new ComputeHistogram(image, hist[i], i*(n/numsOfThreads)+1, n));
			}
			thlist[i].start();
		}
		try{
			for (i = 0; i < numsOfThreads; i++){
				thlist[i].join();
			}
		}catch(InterruptedException e){}
		for (i = 0; i < max; i++){
			for (j = 0; j < numsOfThreads; j++){
				resultHist[i] = resultHist[i] + hist[j][i];
			}
		}
	}

	private void doParallel(int numsOfThreads,int image[][], int hist[]){
		int i;
		Thread thlist[] = new Thread[numsOfThreads];

		for (i = 0; i < numsOfThreads; i++){
			if(i < numsOfThreads - 1){
				thlist[i] = new Thread(new ComputeHistogram(image, hist, i*(n/numsOfThreads)+1, (i+1)*(n/numsOfThreads)));
			}else{
				thlist[i] = new Thread(new ComputeHistogram(image, hist, i*(n/numsOfThreads)+1, n));
			}
			thlist[i].start();
		}
		try{
			for (i = 0; i < numsOfThreads; i++){
				thlist[i].join();
			}
		}catch(InterruptedException e){}
	}

	public static void main(String[] args) {
		Date s,e;
		Long seqtime,seqtimeBig,paratime;
		int i, j;
		Histogram his = new Histogram();
		final int p = Runtime.getRuntime().availableProcessors();

		/* initialize the Image array */
		for (i = 0; i < n; i++) {
			for (j = 0; j < n; j++)
				image[i][j] = (i + j) % max;
		}

		/* Initialize histogram */
		for (i = 0; i <= max; i++){
			hist[i] = 0;
			hist1[i] = new AtomicInteger(0);
		}
		for (i = 0; i <= maxBig; i++){
			hist2[i] = new AtomicInteger(0);
			hist3[i] = 0;
			hist4[i] = 0;
		}

		System.out.println("#####   number of cores is "+p+ "   #####");
		s = new Date();
		his.doSequential(image,hist);
		e = new Date();
		seqtime = e.getTime() - s.getTime();
		System.out.println();
		System.out.println("####### sequential Max = 10  #######");
		System.out.println("        elapsed time is "+seqtime + "");

		s = new Date();
		his.doParallelAtomic(p,image,hist1);
		e = new Date();
		paratime = e.getTime() - s.getTime();
		System.out.println();
		System.out.println("####### parallel version 1 Max = 10 #######");
		System.out.println("        elapsed time is "+paratime);
		System.out.println("        speedup is "+(float)seqtime/paratime);
		System.out.println("        ultility is "+(float)seqtime/paratime/p);

		/* initialize the Image array */
		for (i = 0; i < n; i++) {
			for (j = 0; j < n; j++)
				image[i][j] = (i + j) % maxBig;
		}

		s = new Date();
		his.doSequential(image,histBig);
		e = new Date();
		seqtimeBig = e.getTime() - s.getTime();
		System.out.println();
		System.out.println("####### sequential Max = 100 #######");
		System.out.println("        elapsed time is "+seqtimeBig);

		s = new Date();
		his.doParallelAtomic(p,image,hist2);
		e = new Date();
		paratime = e.getTime() - s.getTime();
		System.out.println();
		System.out.println("####### parallel version 2   #######");
		System.out.println("        elapsed time is "+paratime);
		System.out.println("        speedup is "+(float)seqtimeBig/paratime);
		System.out.println("        ultility is "+(float)seqtimeBig/paratime/p);

		s = new Date();
		his.doParallel(p,image,hist3);
		e = new Date();
		paratime = e.getTime() - s.getTime();
		System.out.println();
		System.out.println("####### parallel version 3   #######");
		System.out.println("        elapsed time is "+paratime);
		System.out.println("        speedup is "+(float)seqtimeBig/paratime);
		System.out.println("        ultility is "+(float)seqtimeBig/paratime/p);

		s = new Date();
		his.doParallel(p,image,hist4,maxBig);
		e = new Date();
		paratime = e.getTime() - s.getTime();
		System.out.println();
		System.out.println("####### parallel version 4   #######");
		System.out.println("        elapsed time is "+paratime);
		System.out.println("        speedup is "+(float)seqtimeBig/paratime);
		System.out.println("        ultility is "+(float)seqtimeBig/paratime/p);

		for (i = 0; i < max; i++){
			System.out.print(hist[i] + " ");
		}
		System.out.println();
		for (i = 0; i < max; i++){
			System.out.print(hist1[i].get() + " ");
		}
		System.out.println();
		for (i = 0; i < maxBig; i++){
			System.out.print(histBig[i] + " ");
		}
		System.out.println();
		for (i = 0; i < maxBig; i++){
			System.out.print(hist2[i].get() + " ");
		}
		System.out.println();
		for (i = 0; i < maxBig; i++){
			System.out.print(hist3[i] + " ");
		}
		System.out.println();
		for (i = 0; i < maxBig; i++){
			System.out.print(hist4[i] + " ");
		}
		System.out.println();
		
	}

}
