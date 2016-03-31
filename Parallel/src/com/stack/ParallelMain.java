package com.stack;

public class ParallelMain {

	public static void main(String[] args) {
		ParallelStack<Integer> stack = new ParallelStack<>();
		Thread th1 = new Thread(new Runnable() {

			@Override
			public void run() {
				for(int i=1; i<=10; i++)
					stack.push(new Integer(i));
			}
		});

		Thread th2 = new Thread(new Runnable() {

			@Override
			public void run() {
				for(int i=1; i<=10; i++)
					System.out.println("pop " + stack.pop());
			}
		});

		Thread th3 = new Thread(new Runnable() {

			@Override
			public void run() {
				for(int i=1; i<=10; i++)
					System.out.println("peek " + stack.peek());
			}
		});



		th1.start();  th2.start(); th3.start();
	    try {th2.join(); th1.join(); th3.join();}
	    catch (InterruptedException e) {};
	}

}
