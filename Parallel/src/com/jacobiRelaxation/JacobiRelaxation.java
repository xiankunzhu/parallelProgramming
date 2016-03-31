package com.jacobiRelaxation;

import java.util.Date;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.IntStream;

public class JacobiRelaxation {
	static final int n = 32;
//	static final int n = 10000;
	static final double tolerance = 0.1;
	static boolean done = true;
	static int idone = 0;
	static double A[][] = new double[n + 2][n + 2];
	static double B[][] = new double[n + 2][n + 2];

	private void doSequential(double A[][],double B[][]) {
		double maxchange, change;
		int i, j;
		/* compute new values until tolerance is reached */
		do {
			maxchange = 0;
			for (i = 1; i <= n; i++){
				for (j = 1; j <= n; j++) {
					/* compute new value and change over old value */
					B[i][j] = (A[i - 1][j] + A[i + 1][j] + A[i][j - 1] + A[i][j + 1]) / 4;
					change = Math.abs(B[i][j] - A[i][j]);
					if (change > maxchange)
						maxchange = change;
				}
			}
//			double temp[][] = A;
//			A = B;
//			B = temp;

			for (i = 1; i <= n; i++)
				for (j = 1; j <= n; j++)
					A[i][j]=B[i][j];

		} while (maxchange > tolerance);
	}

	private void doSeqStream() {
		do{
			done = true;
			IntStream.range(1, n+1).forEach(i->{
				IntStream.range(1, n+1).forEach(j-> {
					B[i][j] = (A[i - 1][j] + A[i + 1][j] + A[i][j - 1] + A[i][j + 1]) / 4;
					if (Math.abs(B[i][j] - A[i][j]) > tolerance)
						done = false;
				});
			});

//			double temp[][] = A;
//			A = B;
//			B = temp;

			for (int i = 1; i <= n; i++)
				for (int j = 1; j <= n; j++)
					A[i][j]=B[i][j];
		}while(!done);
	}


	private void doParaStream(double A[][],double B[][]) {
		do{
			done = true;
			IntStream.range(1, n+1).parallel().forEach(i->{
				IntStream.range(1, n+1).forEach(j-> {
					B[i][j] = (A[i - 1][j] + A[i + 1][j] + A[i][j - 1] + A[i][j + 1]) / 4;
					if (Math.abs(B[i][j] - A[i][j]) > tolerance)
						done = false;
				});
			});

			IntStream.range(1, n+1).parallel().forEach(i->{
				IntStream.range(1, n+1).forEach(j-> {
					A[i][j]=B[i][j];
				});
			});
		}while(!done);
	}

	private void doParaStream2(double A[][],double B[][]) {
		int p = Runtime.getRuntime().availableProcessors();
		CyclicBarrier barrier = new CyclicBarrier(p);
		System.out.println(p);

		//done = true;
		IntStream.range(1, n+1).parallel().forEach(i->{
			
			do {
				done = true;
				IntStream.range(1, n + 1).forEach(j -> {
					B[i][j] = (A[i - 1][j] + A[i + 1][j] + A[i][j - 1] + A[i][j + 1]) / 4;
					if (Math.abs(B[i][j] - A[i][j]) > tolerance)
						done = false;
				});
				try {
					barrier.await();
					for (int j = 1; j <= n; j++)
						A[i][j] = B[i][j];
					barrier.await();
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println(done);
			} while (!done);
		});
	}
	
	private void doParaStream3(double A[][],double B[][]) {
		int total;
		do{
			idone = 1;
			total = IntStream.range(1, n+1).parallel().map(i->{
				IntStream.range(1, n+1).forEach(j-> {
					B[i][j] = (A[i - 1][j] + A[i + 1][j] + A[i][j - 1] + A[i][j + 1]) / 4;
					if (Math.abs(B[i][j] - A[i][j]) > tolerance)
						idone = 0;
				});
				return idone;
			}).sum();

			IntStream.range(1, n+1).parallel().forEach(i->{
				IntStream.range(1, n+1).forEach(j-> {
					A[i][j]=B[i][j];
				});
			});
		}while(total<n);
	}
	
	private void doParaStream4(double A[][],double B[][]) {
		long total;
		do{
			done = true;
			total = IntStream.range(1, n+1).parallel().filter(i->{
				IntStream.range(1, n+1).forEach(j-> {
					B[i][j] = (A[i - 1][j] + A[i + 1][j] + A[i][j - 1] + A[i][j + 1]) / 4;
					if (Math.abs(B[i][j] - A[i][j]) > tolerance)
						done = false;
				});
				return done;
			}).count();

			IntStream.range(1, n+1).parallel().forEach(this::g);
		}while(total<n);
	}
	
	private void g(int i){
		IntStream.range(1, n+1).forEach(j-> {
			A[i][j]=B[i][j];
		});
	}

	private void print(double A[][]){
		int i,j;
		for (i = 0; i < n+2; i++){
			for (j = 0; j < n+2; j++)
				System.out.format("%.2f ",A[i][j]);
			System.out.println();
		}
	}

	private void initalArray(double A[][]){
		int i,j;
		for (i = 1; i <= n; i++)
			for (j = 1; j <= n; j++)
				A[i][j] = 0;
		for (i = 0; i < n + 2; i++) {
			A[i][0] = 10;
			A[i][n + 1] = 10;
			A[0][i] = 10;
			A[n + 1][i] = 10;
		}
	}

	public static void main(String[] args) {
		Date s,e;
		long seqTime,paraTime;
		JacobiRelaxation jac = new JacobiRelaxation();
/*
		jac.initalArray(A);
//		System.out.println("before");
//		jac.print(A);
		s = new Date();
		jac.doSequential(A,	B);
		e = new Date();
		seqTime = e.getTime() - s.getTime();
		System.out.println("#####   elapsed time of normal sequential verison is "+seqTime);
//		System.out.println("result:");
//		jac.print(A);

		jac.initalArray(A);
		s = new Date();
		jac.doSeqStream();
		e = new Date();
		seqTime = e.getTime() - s.getTime();
//		System.out.println("after");
//		jac.print(A);
		System.out.println();
		System.out.println("#####   elapsed time of stream sequential verison is "+seqTime);

		jac.initalArray(A);
		s = new Date();
		jac.doParaStream(A,B);
		e = new Date();
		paraTime = e.getTime() - s.getTime();
//		System.out.println("after");
//		jac.print(A);
		System.out.println();
		System.out.println("#####   elapsed time of stream parallel verison with foreach is "+paraTime);
		System.out.println("#####   speedup is "+(float)seqTime/paraTime);
*/
		jac.initalArray(A);
		s = new Date();
		jac.doParaStream3(A,B);
		e = new Date();
		paraTime = e.getTime() - s.getTime();
		System.out.println("after");
		jac.print(A);
		System.out.println();
		System.out.println("#####   elapsed time of stream parallel verison with map is "+paraTime);
		
		jac.initalArray(A);
		s = new Date();
		jac.doParaStream4(A,B);
		e = new Date();
		paraTime = e.getTime() - s.getTime();
		System.out.println("after");
		jac.print(A);
		System.out.println();
		System.out.println("#####   elapsed time of stream parallel verison with map is "+paraTime);

	}

}
