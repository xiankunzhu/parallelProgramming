package com.shortestPath;

import java.util.Date;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class WorkPool {
	static final int numworkers = 4000;
	int workpool;
	Queue<Integer> pool = new LinkedBlockingQueue<Integer>();
	int count; /* Work Pool counter */
	Lock M;

	WorkPool() {
		M = new ReentrantLock();
		count = 0;
		Putwork(0);
	}

	/* field declarations here */
	public int Getwork() {
		int item;
		/* first read and decrement Work Pool counter */
		if (count == 0) /* Terminate Workers */
			item = -1;
		else {
			M.lock();
			item = (int) pool.remove();
			count--;
			M.unlock();
		}
		return item;
	}

	public void Putwork(int item) {
		M.lock();
		count++; /* Increment Work Pool counter */
		pool.add(new Integer(item));
		M.unlock();
	}
}

class Worker implements Runnable {
	public static final int n = 4000;
	public static final int inf = 32000;// infinity

	private WorkPool workpool;
	private int weight[][];
	private int mindist[];
	private int inflag[];
	private Lock L[];

	public Worker(WorkPool p,int weight[][],int mindist[],int inflag[],Lock L[]){
		this.workpool = p;
		this.weight = weight;
		this.mindist = mindist;
		this.inflag = inflag;
		this.L = L;
	}

	public void run() {
		int vertex;
		int w, newdist;
		vertex = workpool.Getwork(); /* Get vertex no. to analyze */
		while (vertex != -1) {
			inflag[vertex] = 0; /* Vertex removed from Worker Processes */
			if (vertex < 0 || vertex >= n)
				System.out.println(vertex);
			for (w = 0; w < n; w++) { /* Consider all outgoing edges of Vertex*/
				if (weight[vertex][w]<inf) {/*See if this is shorter path to w*/
					newdist = mindist[vertex] + weight[vertex][w];
					L[w].lock(); /* mutual exclusion on mindist[w] */
					if (newdist < mindist[w]) {
						mindist[w] = newdist; /* Update dist to w */
						L[w].unlock();
						if (inflag[w] == 0) { /* If w not in Work Pool */
							inflag[w] = 1;
							workpool.Putwork(w); /* Put w into Work Pool */
						}
					} else
						L[w].unlock();
				}
			}
			vertex = workpool.Getwork(); /* Get new vertex number */
		}
	}
}

// with if condition
class Worker2 implements Runnable {
	public static final int n = 4000;
	public static final int inf = 32000;// infinity

	private WorkPool workpool;
	private int weight[][];
	private int mindist[];
	private int inflag[];
	private Lock L[];

	public Worker2(WorkPool p,int weight[][],int mindist[],int inflag[],Lock L[]){
		this.workpool = p;
		this.weight = weight;
		this.mindist = mindist;
		this.inflag = inflag;
		this.L = L;
	}

	public void run() {
		int vertex;
		int w, newdist;
		vertex = workpool.Getwork(); /* Get vertex no. to analyze */
		while (vertex != -1) {
			inflag[vertex] = 0; /* Vertex removed from Worker Processes */
			for (w = 0; w < n; w++) { /*Consider all outgoing edges of Vertex*/
				if (weight[vertex][w]<inf){/*See if this is shorter path to w*/
					newdist = mindist[vertex] + weight[vertex][w];
					if (newdist < mindist[w]) {
						L[w].lock(); /* mutual exclusion on mindist[w] */
						if (newdist < mindist[w]) {
							mindist[w] = newdist; /* Update dist to w */
							L[w].unlock();
							if (inflag[w] == 0) { /* If w not in Work Pool */
								inflag[w] = 1;
								workpool.Putwork(w); /* Put w into Work Pool*/
							}
						} else
							L[w].unlock();
					}
				}
			}
			vertex = workpool.Getwork(); /* Get new vertex number */
		}
	}
}

public class ShortestPath {

	public static final int numworkers = 2;
	public static final int n = 4000;
	// public static final int n = 5;
	public static final int inf = 32000;// infinity
	private static WorkPool workpool = new WorkPool();
	private static int weight[][] = new int[n][n];
	private static int mindist[] = new int[n];

	private static int mindist1[] = new int[n];
	private static int inflag1[] = new int[n];
	private static Lock L[] = new ReentrantLock[n];

	private static int mindist2[] = new int[n];
	private static int inflag2[] = new int[n];
	private static Lock L2[] = new ReentrantLock[n];

	/*
	 * private static int weight[][] = { {inf, 4, 8, inf, inf}, {inf, inf, 3, 1,
	 * inf}, {inf, inf, inf, inf, 5}, {inf, inf, 2, inf, 10}, {inf, inf, inf,
	 * inf, inf}};
	 */
	public static void main(String[] args) {

		int point[][] = new int[n][2];
		int i, j, temp = 0, dist = 0;
		Random rand = new Random(500);
		for (i = 0; i < n; i++) {
			temp = rand.nextInt(1000);
			point[i][0] = temp;
			temp = rand.nextInt(1000);
			point[i][1] = temp;
		}

		for (i = 0; i < n; i++)
			for (j = 0; j <= i; j++) {
				if (i == j)
					weight[i][j] = inf;
				else {
					temp = point[i][0] - point[j][0];
					dist = temp * temp;
					temp = point[i][1] - point[j][1];
					dist = dist + temp * temp;
					weight[i][j] = dist;
					weight[j][i] = dist;
				}
			}

		Date s, t;
		long seqtime, paratime1, paratime2;
		t = new Date();
		for (i = 0; i < n; i++) {
			mindist[i] = inf;
		}
		Queue<Integer> que = new LinkedBlockingQueue<Integer>();
		que.add(new Integer(0));
		mindist[0] = 0;
		int x;
		int newdist = 0, w;
		while (!que.isEmpty()) {
			x = (int) que.remove();
			for (w = 0; w < n; w++) {
				newdist = mindist[x] + weight[x][w];
				if (newdist < mindist[w]) {
					mindist[w] = newdist;
					if (!que.contains(new Integer(w))) {
						que.add(new Integer(w));
					}
				}
			}
		}

		s = new Date();
		seqtime = s.getTime() - t.getTime();
		System.out.print("Sequential Time: ");
		System.out.println(seqtime);

		System.out.println("Number of workers: "+numworkers);
		// parallel 1
		t = new Date();
		for (i = 0; i < n; i++) {
			/* Initialize mindist and inflag */
			mindist1[i] = inf;
			inflag1[i] = 0;
			L[i] = new ReentrantLock();
		}
		mindist1[0] = 0;
		inflag1[0] = 1;
		WorkPool p = new WorkPool();
		/* Create Replicated Workers */
		Thread th1 = new Thread(new Worker(p, weight, mindist1, inflag1, L));
		Thread th2 = new Thread(new Worker(p, weight, mindist1, inflag1, L));

		th1.start();
		th2.start();
		try {
			th1.join();
			th2.join();
		} catch (InterruptedException e) {}
		s = new Date();
		paratime1 = s.getTime() - t.getTime();
		System.out.println("Parallel Time Of \"no if\": " + paratime1);
		System.out.println("Speedup is " + (float) seqtime / paratime1);

		// parallel 2
		t = new Date();
		for (i = 0; i < n; i++) {
			/* Initialize mindist and inflag */
			mindist2[i] = inf;
			inflag2[i] = 0;
			L2[i] = new ReentrantLock();
		}
		mindist2[0] = 0;
		inflag2[0] = 1;
		WorkPool p2 = new WorkPool();
		/* Create Replicated Workers */
		Thread th3 = new Thread(new Worker2(p2, weight, mindist2, inflag2, L2));
		Thread th4 = new Thread(new Worker2(p2, weight, mindist2, inflag2, L2));

		th3.start();
		th4.start();
		try {
			th3.join();
			th4.join();
		} catch (InterruptedException e) {}
		s = new Date();
		paratime2 = s.getTime() - t.getTime();
		System.out.println("Parallel Time Of \"has if\": " + paratime2);
		System.out.println("Speedup is " + (float) seqtime / paratime2);

		for (i = 0; i < n; i++) {
			if(mindist[i] != mindist1[i]
					|| mindist[i] != mindist2[i]
					|| mindist2[i] != mindist1[i])
			System.out.println("difference in i : "+mindist[i] + " "
					+ mindist1[i] + " "+ mindist2[i] + " ");
		}
		System.out.println("end");
	}
}
