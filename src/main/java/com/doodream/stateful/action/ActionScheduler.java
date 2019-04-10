package com.doodream.stateful.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActionScheduler {

    private static final Logger Log = LoggerFactory.getLogger(ActionScheduler.class);

    private final ArrayBlockingQueue<Action> actionQueue = new ArrayBlockingQueue<>(10);
    private final ExecutorService stealingPool = Executors.newWorkStealingPool(4);
    private Future actionPoolConsumingTask;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);
    private final Map<Action, ScheduledFuture> scheduledTasks = new ConcurrentHashMap<>();
    private WeakReference<ScheduledActionListener> listener;
    private AtomicBoolean isStarted = new AtomicBoolean(false);

    interface ScheduledActionListener {
        void onHandleAction(Action action);
    }

    @Override
    protected synchronized void finalize() throws Throwable {
        super.finalize();
        cancelPoolConsumingTask();
        cancelScheduledTask();
        scheduledExecutorService.shutdown();
        stealingPool.shutdown();
    }

    private void cancelScheduledTask() {
        for (Map.Entry<Action, ScheduledFuture> scheduledTask : scheduledTasks.entrySet()) {
            final ScheduledFuture task = scheduledTask.getValue();
            if(task.isCancelled() || task.isDone()) {
                continue;
            }
            Log.trace("task {} canceled", task);
            task.cancel(true);
        }
    }

    synchronized void start(ScheduledActionListener actionListener) throws IllegalStateException {
        if(!isStarted.compareAndSet(false, true)) {
            return;
        }
        actionPoolConsumingTask = stealingPool.submit(() -> {
            while (isStarted.get()) {
                try {
                    final Action<?> action = actionQueue.take();
                    actionListener.onHandleAction(action);
                } catch (InterruptedException e) {

                }
            }
        });
        listener = new WeakReference<>(actionListener);
    }

    void stop() {
        if(!isStarted.compareAndSet(true, false)) {
            throw new IllegalStateException("not started!!");
        }
        synchronized (scheduledTasks) {
            try {
                while (!scheduledTasks.isEmpty()) {
                    scheduledTasks.wait();
                }
            } catch (InterruptedException ignored) { }
        }
        cancelPoolConsumingTask();
        cancelScheduledTask();
    }

    private void cancelPoolConsumingTask() {
        if(actionPoolConsumingTask == null) {
            return;
        }
        if(actionPoolConsumingTask.isDone() || actionPoolConsumingTask.isCancelled()) {
            return;
        }
        actionPoolConsumingTask.cancel(true);
    }

    public void submit(Action action) {
        try {
            actionQueue.add(action);
        } catch (IllegalStateException e) {
            Log.warn("fail to submit action {}", e.getMessage());
        }
    }

    public boolean scheduleAction(final Action action, long delay, TimeUnit timeUnit) {
        if(scheduledExecutorService.isShutdown() || scheduledExecutorService.isTerminated()) {
            Log.warn("scheduler is already stopped!!");
            return false;
        }

        if(!scheduledTasks.containsKey(action)) {
            scheduledTasks.put(action, scheduledExecutorService.schedule(() -> {
                final ScheduledActionListener nullableListener = listener.get();
                synchronized (scheduledTasks) {
                    scheduledTasks.remove(action);
                    scheduledTasks.notifyAll();
                }
                if (nullableListener == null) {
                    return false;
                }
                nullableListener.onHandleAction(action);
                return true;
            }, delay, timeUnit));
            return true;
        } else {
            Log.warn("fail to schedule action");
            return false;
        }
    }

}
