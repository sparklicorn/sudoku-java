package com.mycompany.app.util;

import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool {
	
	//private int numThreads;
	
	//private boolean dynamicThreadCreation;
	private Vector<Thread> threads;
	//private Thread[] threads;
	private LinkedBlockingQueue<Runnable> workQueue;
	private AtomicBoolean isActive;
	
	public ThreadPool() {
		//this.numThreads = numThreads;
		this.workQueue = new LinkedBlockingQueue<>();
		this.isActive = new AtomicBoolean(true);
		this.threads = new Vector<>();
		//dynamicThreadCreation = true;
	}
	
	public ThreadPool(int numThreads) {
		//this.numThreads = numThreads;
		this.workQueue = new LinkedBlockingQueue<>();
		this.isActive = new AtomicBoolean(true);
		this.threads = new Vector<>();
		//dynamicThreadCreation = false;
		for (int n = 0; n < numThreads; n++) {
			startThread();
		}
	}
	
	/**
	 * Prevents the work queue from accepting more work items, allowing
	 * the worker threads to terminate at some point in the future.
	 * Once the pool has been shut down, it cannot be reopened.
	 */
	public void shutdown() {
		isActive.set(false);
	}
	
	/**
	 * Immediately shuts down this thread pool by interrupting all
	 * worker threads and clearing the work queue.
	 */
	public void shutdownNow() {
		shutdown();
		workQueue.clear();
		for (Thread t : threads) {
			t.interrupt();
		}
	}
	
	/**
	 * Returns the work queue backing this thread pool.
	 */
	public Queue<Runnable> getQueue() {
		return this.workQueue;
	}
	
	public void addThreads(int numThreads) {
		for (int n = 0; n < numThreads; n++) {
			startThread();
		}
	}
	
	private void startThread() {
		Thread newThread = new Thread();
		
		Runnable r = () -> {
			while (isActive.get()) {
				try {
					Runnable workItem = workQueue.poll(1, TimeUnit.SECONDS);
					if (workItem != null) {
						workItem.run();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
	
		newThread = new Thread(r);
		newThread.start();
		threads.addElement(newThread);
	}
	
	//return null if queue is not accepting more work
	public synchronized <T> Future<T> submit(Callable<T> task) {
		FutureTask<T> result = null;
		
		if (isActive.get()) {
			result = new FutureTask<>(task);
			workQueue.offer(result);
		}
		
		return result;
	}
	
	public synchronized void submit(Runnable task) {
		if (isActive.get()) {
			workQueue.offer(task);
		}
	}
	
	
	public static void doBatch(List<Runnable> work, int numThreads, Runnable callback) {
		
		int numJobs = work.size();
		ThreadPool pool = new ThreadPool(numThreads);
		AtomicInteger workFinishedCounter = new AtomicInteger(0);
		
		for (Runnable r : work) {
			pool.submit(() -> {
				r.run();
				workFinishedCounter.incrementAndGet();
			});
		}
		
		Thread workWatcher = new Thread(() -> {
			while (workFinishedCounter.get() < numJobs) {
				try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			callback.run();
			pool.shutdown();
		});
		workWatcher.start();
		
	}
	
	public static void repeatWork(Runnable work, int numTimes, int numThreads, Runnable callback) {
		ThreadPool pool = new ThreadPool(numThreads);
		AtomicInteger workFinishedCounter = new AtomicInteger(0);
		
		for (int i = 0; i < numTimes; i++) {
			pool.submit(() -> {
				work.run();
				workFinishedCounter.incrementAndGet();
			});
		}
		
		Thread workWatcher = new Thread(() -> {
			while (workFinishedCounter.get() < numTimes) {
				try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			callback.run();
			pool.shutdown();
		});
		workWatcher.start();
	}
	
}
