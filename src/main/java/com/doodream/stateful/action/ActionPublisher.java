package com.doodream.stateful.action;

import com.doodream.stateful.state.StateContext;

public interface ActionPublisher {
    void startListen(final StateContext context, ActionScheduler actionScheduler);
    void stopListen(final StateContext context);
}
