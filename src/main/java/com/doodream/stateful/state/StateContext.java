package com.doodream.stateful.state;

import com.doodream.stateful.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;

public class StateContext {

    private static final Logger Log = LoggerFactory.getLogger(StateContext.class);
    private final Map<String, Object> params;
    private final Map<String, StateHandler> handlers;
    private String state;

    public StateContext(String initState, Map<String, StateHandler> handlerMap, Map<String, Object> parameters) {
        params = parameters;
        handlers = handlerMap;
        state = initState;
        if(!handlers.containsKey(state)) {
            throw new IllegalArgumentException(String.format(Locale.ENGLISH, "no state handler for %s", state));
        }
    }

    public synchronized String getState() {
        return state;
    }

    public Object getParameter(String key) {
        return params.get(key);
    }

    public synchronized boolean handle(Action<?> action) {
        Log.debug("handle {}", action);
        final StateHandler handler = handlers.get(state);
        if(handler == null) {
            throw new IllegalStateException("no valid state handler for " + state);
        }

        action.setStateContext(this);
        final String nextState = handler.handleAction(action);
        if(nextState == null || nextState.isEmpty()) {
            return false;
        }

        if(!nextState.equalsIgnoreCase(state)) {
            applyState(state, nextState);
        }
        return true;
    }

    void stop() {
        applyState(state, null);
    }

    void start() {
        applyState(null, state);
    }

    private void applyState(String currentState, String nextState) {
        Log.debug("state transition {} => {}", currentState, nextState);
        if(isValidState(currentState)) {
            doStageOut(currentState);
        }

        if(isValidState(nextState)) {
            state = nextState;
            doStageIn(nextState);

        }
    }

    private void doStageIn(String state) {
        final StateHandler handler = handlers.get(state);
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
