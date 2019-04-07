package com.doodream.stateful.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.*;

public class ActionScheduler {

    private static final Logger Log = LoggerFactory.getLogger(ActionScheduler.class);

    private final ArrayBlockingQueue<Action> actionQueue = new ArrayBlockingQueue<>(10);
    private final ExecutorService stealingPool = Executors.newWorkStealingPool(4);
    private Future actionPoolConsumingTask;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);
    private final Map<Action, ScheduledFuture> scheduledTasks = new ConcurrentHashMap<>();
    private WeakReference<ScheduledActionListener> listener;
    private volatile boolean isStarted = false;

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
        if(isStarted) {
            throw new IllegalStateException("already started!!");
        }
        isStarted = true;
        actionPoolConsumingTask = stealingPool.submit(() -> {
        });
        listener = new WeakReference<>(actionListener);
    }

    synchronized void stop() {
        if(!isStarted) {
            throw new IllegalStateException("not started!!");
        }
        isStarted = false;
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
                scheduledTasks.remove(action);
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
