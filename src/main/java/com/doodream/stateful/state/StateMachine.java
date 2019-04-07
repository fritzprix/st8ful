package com.doodream.stateful.state;

import com.doodream.stateful.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class StateMachine {

    private static final Logger Log = LoggerFactory.getLogger(StateMachine.class);
    private StateContext context;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final AtomicReference<Future> stateMachineTask = new AtomicReference<>();
    private final AtomicBoolean isStarted = new AtomicBoolean(false);
    private final ArrayBlockingQueue<Action<?>> actionQueue;

    public StateMachine(String initState, Map<String, StateHandler> stateHandlerMap, Map<String, Object> parameters) {
        context = new StateContext(initState, stateHandlerMap, parameters);
        actionQueue = new ArrayBlockingQueue<>(10);
    }

    public boolean handle(Action<?> action) {
        if(action == null) {
            return false;
        }
        try {
            Log.debug("queue action {}", action);
            actionQueue.add(action);
        } catch (IllegalStateException e) {
            Log.warn("action queue is full!!, all the threads in the pool are blocked");
            return false;
        }

        return true;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        cancelTask(stateMachineTask.get());
        executorService.shutdown();
    }

    public void start() throws IllegalStateException {
        if(!isStarted.compareAndSet(false, true)) {
            throw new IllegalStateException("state machine has already started");
        }
        stateMachineTask.set(executorService.submit(() -> {
            startHandleAction();
        }));
    }

    public void stop() {
        if(!isStarted.compareAndSet(true, false)) {
            throw new IllegalStateException("not started yet");
        }

        cancelTask(stateMachineTask.get());
        executorService.shutdown();
        actionQueue.clear();
    }

    private void cancelTask(Future task) {
        if(task == null) {
            return;
        }
        if(task.isCancelled() || task.isDone()) {
            return;
        }

        task.cancel(true);
    }

    private void startHandleAction() {
        try {
            context.start();
            Log.debug("started!! {}", isStarted.get());
            while(isStarted.get()) {
                Action<?> action = actionQueue.take();
                try {
                    if (context.handle(action)) {
                        Log.debug("action : {} handled", action);
                    }
                } catch (Exception e) {
                    Log.warn("fail to handle {} : ", action, e);
                }
            }

        } catch (InterruptedException e) {
            Log.warn("quit action consuming loop");
        }
        context.stop();
    }



    public static Builder builder() {
        return new Builder();
    }

    public String getState() {
        return context.getState();
    }

    public static class Builder {

        private Builder() { }
        private final Map<String, StateHandler> stateHandlerMap = new HashMap<String, StateHandler>();
        private final Map<String, Object> parameters = new HashMap<String, Object>();
        private StateHandle initState;



        public Builder setHandlers(Class<? extends StateHandler> ...handlers) throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
            for (Class<? extends StateHandler> handler : handlers) {
                if(!isStateAnnotated(handler)) {
                    continue;
                }
                StateHandle stateHandle = handler.getAnnotation(StateHandle.class);
                Constructor<? extends StateHandler> constructor = handler.getConstructor();
                if(initState == null) {
                    initState = stateHandle;
                }
                if(stateHandlerMap.containsKey(stateHandle.state())) {
                    break;
                }

                stateHandlerMap.put(stateHandle.state(), constructor.newInstance());

            }
            if(stateHandlerMap.isEmpty()) {
                throw new IllegalArgumentException("no valid state mapping");
            }

            return this;
        }

        public Builder addParameter(String key, Object value) {
            parameters.put(key, value);
            return this;
        }

        public StateMachine build() {
            return new StateMachine(initState.state(), stateHandlerMap, parameters);
        }

        private boolean isStateAnnotated(Class<?> handler) {
            return handler.getAnnotation(StateHandle.class) != null;
        }
    }


}
