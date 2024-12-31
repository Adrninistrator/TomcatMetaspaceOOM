package com.test.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author adrninistrator
 * @date 2024/12/11
 * @description:
 */
public class ThreadFactory4TPE implements ThreadFactory {

    private static final AtomicInteger ai = new AtomicInteger(0);

    private final String threadNamePrefix;

    public ThreadFactory4TPE(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(threadNamePrefix + "-" + ai.addAndGet(1));
        return thread;
    }
}