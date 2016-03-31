package com.mergeSort;

import java.util.Date;

public class MergeSort {
	public static final int n = 2000000;

	public int binsearchSmall(int val, int[] values, int length) {
		int start = 0;
		int mid = 0;
		int end = length - 1;
		int ret = 0;
		while (start < end) {
			mid = (start + end) / 2;
			if (val <= values[mid]) {
				end = mid - 1;
			} else if (val > values[mid]) {
				start = mid + 1;
			}
		}

		if (val <= values[start]) {
			ret = start;
		} else if (val > values[start]) {
			ret = start + 1;
		}

		return ret;
	}

	public int binsearchBig(int val, int[] values, int length) {
		int start = 0;
		int mid = 0;
		int end = length - 1;
		int ret = 0;
		while (start < end) {
			mid = (start + end) / 2;
			if (val < values[mid]) {
				end = mid - 1;
			} else if (val >= values[mid]) {
				start = mid + 1;
			}
		}

		if (val < values[start]) {
			ret = start;
		} else if (val >= values[start]) {
			ret = start + 1;
		}
		return ret;
	}

	public static void main(String[] args) {
		int i;
		final int[] X = new int[n];
		final int[] Y = new int[n];
		final int[] Z = new int[2 * n];
		MergeSort ms = new MergeSort();

		int value1, value2;
		int indexInOther1, indexInOther2;
		/* input sorted lists X and Y */
		for (i = 0; i < n; i++)
			X[i] = i * 2;
		for (i = 0; i < n; i++)
			Y[i] = 1 + i * 2;
		Date t = new Date();
		long seq;

		for (i = 0; i < n; i++) {
			value1 = X[i];
			value2 = Y[i];
			indexInOther1 = ms.binsearchSmall(value1, Y, n);
			indexInOther2 = ms.binsearchBig(value2, X, n);
			Z[indexInOther1 + i] = value1;
			Z[indexInOther2 + i] = value2;
		}
		Date s = new Date();
		seq = s.getTime() - t.getTime();
		System.out.println("Sequential Time: "+seq);

		/* Parallel Version */
		/* input sorted lists X and Y */
		for (i = 0; i < n; i++)
			X[i] = i * 2;
		for (i = 0; i < n; i++)
			Y[i] = 1 + i * 2;
		t = new Date();
		// insert your code to create
		Thread th1 = new Thread(new Runnable() {

			@Override
			public void run() {
				int value1,i,indexInOther1;
				for (i = 0; i < n; i++) {
					value1 = X[i];
					indexInOther1 = ms.binsearchSmall(value1, Y, n);
					Z[indexInOther1 + i] = value1;
				}
			}
		});

		Thread th2 = new Thread(new Runnable() {

			@Override
			public void run() {
				int value2,i,indexInOther2;
				for (i = 0; i < n; i++) {
					value2 = Y[i];
					indexInOther2 = ms.binsearchBig(value2, X, n);
					Z[indexInOther2 + i] = value2;
				}
			}
		});

		// and start the threads here
		th1.start();
		th2.start();

		try{
			th1.join();
			th2.join();
		}
		catch(InterruptedException e){}

		s = new Date();
		System.out.print("Parallel Time: ");
		System.out.println(s.getTime() - t.getTime());
		System.out.println("Speedup is: "+(float)seq/(s.getTime() - t.getTime()));

		for(i=0;i<40;i++){
			System.out.print(X[i]+" ");
			if (i%10 == 9) System.out.println();
		}
		for(i=0;i<40;i++){
			System.out.print(Y[i]+" ");
			if (i%10 == 9) System.out.println();
		}
		for(i=0;i<80;i++){
			System.out.print(Z[i]+" ");
			if (i%10 == 9) System.out.println();
		}

	}

}
