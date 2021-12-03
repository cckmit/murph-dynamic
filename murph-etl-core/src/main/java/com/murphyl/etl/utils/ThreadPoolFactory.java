package com.murphyl.etl.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * -
 *
 * @date: 2021/11/12 12:39
 * @author: murph
 */
public final class ThreadPoolFactory {

    private static final String THREAD_NAME_TEMPLATE = "%s-thread-%d";

    public static synchronized ThreadPoolExecutor create(int corePoolSize, int maxPoolSize, int keepAliveMinutes, int queueSize, String key) {
        AtomicInteger idGenerator = new AtomicInteger(0);
        ThreadGroup threadGroup = new ThreadGroup(key);
        BlockingQueue queue = new LinkedBlockingDeque(queueSize);
        ThreadFactory taskFactory = runnable -> {
            String threadName = String.format(THREAD_NAME_TEMPLATE, key, idGenerator.incrementAndGet());
            return new Thread(threadGroup, runnable, threadName);
        };
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveMinutes, TimeUnit.MINUTES, queue, taskFactory);
    }

}
