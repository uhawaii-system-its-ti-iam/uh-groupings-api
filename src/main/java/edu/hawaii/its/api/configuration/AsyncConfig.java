package edu.hawaii.its.api.configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private final int threads;
    private final int queueCapacity;

    public AsyncConfig(
            @Value("${groupings.api.async.threads}") int threads,
            @Value("${groupings.api.async.queue-capacity}") int queueCapacity) {
        this.threads = threads;
        this.queueCapacity = queueCapacity;
    }

    @Override
    public Executor getAsyncExecutor() {
        return new DelegatingSecurityContextAsyncTaskExecutor(threadPool());
    }

    /**
     * The threads the @Async jobs run on. An import holds its thread for the whole import (about 25 minutes for
     * 10,000 members), so all the threads are started before a job is queued: a ThreadPoolTaskExecutor starts
     * threads beyond its core pool size only once its queue is full. Idle threads end after a minute.
     *
     * When the threads are all busy and the queue is full, a job is rejected (TaskRejectedException, so the request
     * fails) rather than run on the request's own thread, which would hold it for the length of an import.
     */
    ThreadPoolTaskExecutor threadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threads);
        executor.setMaxPoolSize(threads);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setQueueCapacity(queueCapacity);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setThreadNamePrefix("async-thread-");
        executor.initialize();
        return executor;
    }
}
