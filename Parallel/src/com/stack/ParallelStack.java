package com.stack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ParallelStack<T>{
	List<T> base;
	private final ReentrantReadWriteLock rwl =
			  new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	public ParallelStack() {
		base = new ArrayList<T>();
	}

	public void push(T item){
		w.lock();
		base.add(item);
		w.unlock();
	}

	public T peek() {
		T item = null;
		r.lock();
		int size = base.size();
		r.unlock();
		if(size>0){
			r.lock();
			item = base.get(base.size()-1);
			r.unlock();
		}
		return item;
	}

	public T pop(){
		T item = null;
		r.lock();
		int size = base.size();
		r.unlock();
		if(size>0){
			r.lock();
			item = base.get(base.size()-1);
			r.unlock();
			w.lock();
			base.remove(base.size()-1);
			w.unlock();
		}
		return item;
	}

	public void clear() {
		base.clear();
	}


}
