package edu.hawaii.its.api.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class AsyncConfigTest {

    private final AsyncConfig asyncConfig = new AsyncConfig(10, 100);

    @Test
    public void construction() {
        assertNotNull(asyncConfig);
    }

    @Test
    public void getAsyncExecutorTest() {
        assertNotNull(asyncConfig.getAsyncExecutor());
    }

    @Test
    public void usesTheConfiguredSizes() {
        ThreadPoolTaskExecutor threadPool = new AsyncConfig(3, 7).threadPool();
        try {
            assertEquals(3, threadPool.getCorePoolSize());
            assertEquals(3, threadPool.getMaxPoolSize());
            assertEquals(7, threadPool.getThreadPoolExecutor().getQueue().remainingCapacity());
        } finally {
            threadPool.shutdown();
        }
    }

    @Test
    public void runsTenJobsAtOnceBeforeQueueingAny() throws InterruptedException {
        // An import holds its thread for the whole import, so a job must not queue while threads could be started.
        ThreadPoolTaskExecutor threadPool = asyncConfig.threadPool();
        CountDownLatch started = new CountDownLatch(10);
        CountDownLatch finish = new CountDownLatch(1);
        try {
            for (int i = 0; i < 10; i++) {
                threadPool.execute(() -> {
                    started.countDown();
                    try {
                        finish.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            assertTrue(started.await(5, TimeUnit.SECONDS), "Jobs still waiting to start: " + started.getCount());
            assertEquals(0, threadPool.getQueueSize());
            // Idle threads end, rather than staying for the life of the API.
            assertTrue(threadPool.getThreadPoolExecutor().allowsCoreThreadTimeOut());
        } finally {
            finish.countDown();
            threadPool.shutdown();
        }
    }

    @Test
    public void rejectsAJobWhenTheThreadsAreBusyAndTheQueueIsFull() throws InterruptedException {
        // A rejected job fails its request; it is not run on the request's own thread.
        ThreadPoolTaskExecutor threadPool = new AsyncConfig(1, 1).threadPool();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch finish = new CountDownLatch(1);
        Runnable blocked = () -> {
            started.countDown();
            try {
                finish.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
        try {
            threadPool.execute(blocked);
            assertTrue(started.await(5, TimeUnit.SECONDS));
            threadPool.execute(() -> {
            });
            assertEquals(1, threadPool.getQueueSize());

            assertThrows(TaskRejectedException.class, () -> threadPool.execute(() -> {
            }));
        } finally {
            finish.countDown();
            threadPool.shutdown();
        }
    }
}
