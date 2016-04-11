/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gifview.aven.gifviewdemo;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Gif线程池，用于网络任务
 */
public class GifThreadPoolExecutor {
    public static final String TAG = GifThreadPoolExecutor.class.getSimpleName();
    /** 调用核心数 */
    private final static int CORE_POOL_SIZE = 2;
    /** 最大线程数 */
    private final static int MAXINUM_POOL_SIZE = 2;
    /** 线程队列 */
    private final static int QUEUE_NUM = 80;
    private final Object mLock = new Object();

    private final Queue<Runnable> mTasks;
    private boolean mIsShutdown;
    private final ThreadPoolExecutor mThreadPoolExecutor;

    // The task which is running now.
    private Runnable mActive;

    private static GifThreadPoolExecutor sInstance;

    public GifThreadPoolExecutor() {
        mTasks = new ConcurrentLinkedQueue<Runnable>();
        mIsShutdown = false;
        mThreadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE , MAXINUM_POOL_SIZE,
                0 /* keepAliveTime */, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(QUEUE_NUM));
    }

    public GifThreadPoolExecutor(int coreSize, int poolSize, int queueSize) {
        mTasks = new ConcurrentLinkedQueue<Runnable>();
        mIsShutdown = false;
        mThreadPoolExecutor = new ThreadPoolExecutor(coreSize , poolSize,
                0 /* keepAliveTime */, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(queueSize));
    }

    public static GifThreadPoolExecutor getInstance() {
        synchronized (GifThreadPoolExecutor.class) {
            if (sInstance == null) {
                sInstance = new GifThreadPoolExecutor();
            }
        }
        return sInstance;
    }

    public static GifThreadPoolExecutor getInstance(int coreSize, int poolSize, int queueSize) {
        synchronized (GifThreadPoolExecutor.class) {
            if (sInstance == null) {
                sInstance = new GifThreadPoolExecutor(coreSize,poolSize,queueSize);
            }
        }
        return sInstance;
    }
    
    /**
     * Enqueues the given task into the task queue.
     * @param r the enqueued task
     */
    public void execute(final Runnable r) {
        synchronized(mLock) {
            if (!mIsShutdown) {
                mTasks.offer(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            r.run();
                        } finally {
                            scheduleNext();
                        }
                    }
                });
                if (mActive == null) {
                    scheduleNext();
                }
            }
        }
    }


    private boolean fetchNextTasksLocked() {
        mActive = mTasks.poll();
        return mActive != null;
    }

    private void scheduleNext() {
        synchronized(mLock) {
            if (fetchNextTasksLocked()) {
                mThreadPoolExecutor.execute(mActive);
            }
        }
    }

    public boolean isTerminated() {
        synchronized(mLock) {
            if (!mIsShutdown) {
                return false;
            }
            return mTasks.isEmpty() && mActive == null;
        }
    }
}
