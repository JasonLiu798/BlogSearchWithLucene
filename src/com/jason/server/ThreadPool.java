package com.jason.server;

import java.util.LinkedList;

/**
 * class thread pool
 * Created by liujianlong on 15/1/21.
 */
public class ThreadPool {
    static final long IDLE_TIMEOUT = 60000L;//超时

    private String name;
    private int minsize;
    private int maxsize;
    private int nextWorkerId = 0;
    private LinkedList pool = new LinkedList();

    public ThreadPool() {
        this("Thread Pool Started");
    }

    public ThreadPool(String name) {
        this(name, 0, 20);
    }

    public ThreadPool(String name, int minsize, int maxsize) {
        this.name = name;
        this.minsize = minsize;
        this.maxsize = maxsize;
    }

    public synchronized void run(Runnable runner) {
        Worker worker;

        if (runner == null) {
            throw new NullPointerException();
        }

        // get a worker from free list...
        if (!pool.isEmpty()) {
            worker = (Worker) pool.removeFirst();
        } else {
        	// no free worker available, create new one...
            worker = new Worker(name + "-" + ++nextWorkerId);
            worker.start();
        }
        // and wake up worker to service incoming runner
        worker.wakeup(runner);
    }

    // Notified when a worker has idled timeout
    // @return true if worker should die, false otherwise
    synchronized boolean notifyTimeout(Worker worker) {
        if (worker.runner != null) {
            return false;
        }
        if (pool.size() > minsize) {// Remove from free list
            pool.remove(worker);
            return true; // die
        }
        return false; // continue
    }

    // Notified when a worker has finished his work and
// free to service next runner
// @return true if worker should die, false otherwise
    synchronized boolean notifyFree(Worker worker) {
        if (pool.size() < maxsize) {
// Add to free list
            pool.addLast(worker);
            return false; // continue
        }
        return true; // die
    }

    
    /**
     * class Worker 
     */
    class Worker extends Thread {
        Runnable runner = null;
        static final long IDLE_TIMEOUT = 60000L;

        public Worker(String name) {
            super(name);
        }

        synchronized void wakeup(Runnable runner) {
            this.runner = runner;
            notify();
        }

        public void run() {
            for (;;) {
                synchronized (this) {
                    if (runner == null) {
                        try {
                            wait(IDLE_TIMEOUT);
                        } catch (InterruptedException e) {}
                    }
                }

                // idle timed out, die or put into free list
                if (runner == null) {
                    if (notifyTimeout(this))
                        return;
                    else
                        continue;
                }

                try {
                    runner.run();
                } finally {
                    runner = null;
                    if (notifyFree(this))
                        return;
                }
            }
        }
    }

}