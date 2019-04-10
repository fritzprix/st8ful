package com.doodream.stateful.state;

import com.doodream.stateful.action.Action;
import com.doodream.stateful.action.ActionRoute;
import com.doodream.stateful.action.ActionRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class StateMachine {

    private static final Logger Log = LoggerFactory.getLogger(StateMachine.class);
    private StateContext context;
    private final ExecutorService executorService = Executors.newWorkStealingPool(4);
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
            actionQueue.put(action);
        } catch (InterruptedException e) {
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
                    Log.warn("StateHandler for {} has been already added!!", stateHandle.state());
                    continue;
                }

                StateHandler instance = constructor.newInstance();



                if(isActionRouteAnnotated(handler)) {
                    ActionRouter router = ActionRouter.from(handler.getAnnotation(ActionRoute.class));
                    try {
                        Field routerField = StateHandler.class.getDeclaredField("router");
                        routerField.setAccessible(true);
                        routerField.set(instance, router);
                    } catch (NoSuchFieldException e) {
                        Log.warn("the field named router is not found");
                    }
                }

                stateHandlerMap.put(stateHandle.state(), instance);

            }
            if(stateHandlerMap.isEmpty()) {
                throw new IllegalArgumentException("no valid state mapping");
            }

            return this;
        }

        private boolean isActionRouteAnnotated(Class<? extends StateHandler> handler) {
            return handler.getAnnotation(ActionRoute.class) != null;
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
