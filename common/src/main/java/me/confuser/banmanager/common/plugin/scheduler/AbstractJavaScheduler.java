/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.confuser.banmanager.common.plugin.scheduler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Abstract implementation of {@link SchedulerAdapter} using a {@link ScheduledExecutorService}.
 */
public abstract class AbstractJavaScheduler implements SchedulerAdapter {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("luckperms-scheduler")
            .build()
    );

    private final ErrorReportingExecutor workerPool = new ErrorReportingExecutor(Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("luckperms-worker-%d")
            .build()
    ));

    @Override
    public SchedulerTask syncLater(Runnable task, long delay, TimeUnit unit) {
        ScheduledFuture<?> future = this.scheduler.schedule(() -> task, delay, unit);
        return () -> future.cancel(false);
    }

    @Override
    public Executor async() {
        return this.workerPool;
    }

    @Override
    public SchedulerTask asyncLater(Runnable task, long delay, TimeUnit unit) {
        ScheduledFuture<?> future = this.scheduler.schedule(() -> this.workerPool.execute(task), delay, unit);
        return () -> future.cancel(false);
    }

    @Override
    public SchedulerTask asyncRepeating(Runnable task, long interval, TimeUnit unit) {
        ScheduledFuture<?> future = this.scheduler.scheduleAtFixedRate(() -> this.workerPool.execute(task), interval, interval, unit);
        return () -> future.cancel(false);
    }

    @Override
    public void shutdown() {
        this.scheduler.shutdown();
        this.workerPool.delegate.shutdown();
        try {
            this.workerPool.delegate.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static final class ErrorReportingExecutor implements Executor {
        private final ExecutorService delegate;

        private ErrorReportingExecutor(ExecutorService delegate) {
            this.delegate = delegate;
        }

        @Override
        public void execute(@NonNull Runnable command) {
            this.delegate.execute(new ErrorReportingRunnable(command));
        }
    }

    private static final class ErrorReportingRunnable implements Runnable {
        private final Runnable delegate;

        private ErrorReportingRunnable(Runnable delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            try {
                this.delegate.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}