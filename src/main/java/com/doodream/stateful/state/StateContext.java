package com.doodream.stateful.state;

import com.doodream.stateful.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class StateContext {

    private static final Logger Log = LoggerFactory.getLogger(StateContext.class);
    private final Map<String, Object> params;
    private final Map<String, StateHandler> handlers;
    private String currentState;

    public StateContext(String initState, Map<String, StateHandler> handlerMap, Map<String, Object> parameters) {
        params = parameters;
        handlers = handlerMap;
        currentState = initState;
        if(!handlers.containsKey(currentState)) {
            throw new IllegalArgumentException(String.format(Locale.ENGLISH, "no currentState handler for %s", currentState));
        }
    }

    public synchronized String getState() {
        return currentState;
    }

    public Object getParameter(String key) {
        return params.get(key);
    }

    public synchronized boolean handle(Action<?> action) throws ExecutionException {
        Log.debug("handle {}", action);
        final StateHandler handler = handlers.get(currentState);
        if(handler == null) {
            throw new IllegalStateException("no valid currentState handler for " + currentState);
        }

        action.setStateContext(this);
        final String nextState = handler.handleAction(action);
        if(!isValidState(nextState)) {
            return false;
        }

        if(!nextState.equalsIgnoreCase(currentState)) {
            applyState(currentState, nextState);
        }
        return true;
    }

    void stop() {
        applyState(currentState, null);
    }

    void start() {
        applyState(null, currentState);
    }

    private void applyState(String currentState, String nextState) {
        Log.debug("currentState transition {} => {}", currentState, nextState);
        if(isValidState(currentState)) {
            doStageOut(currentState);
        }

        if(isValidState(nextState)) {
            this.currentState = nextState;
            doStageIn(nextState);
        }
    }

    private void doStageIn(String state) {
        final StateHandler handler = handlers.get(state);
        if(handler == null) {
            throw new IllegalStateException("no currentState handler!!");
        }
        handler.stageIn(this);
    }

    private void doStageOut(String state) {
        final StateHandler handler = handlers.get(state);
        handler.stageOut(this);
    }

    private boolean isValidState(String state) {
        return (state != null) &&
                (!state.isEmpty()) &&
                handlers.containsKey(state);
    }
}
