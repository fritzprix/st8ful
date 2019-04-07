package com.doodream.stateful.action;

import com.doodream.stateful.state.StateContext;

public interface RouterComponent {
    void start(final StateContext context);
    void stop(final StateContext context);
}
