package com.doodream.stateful.state;

import com.doodream.stateful.action.Action;

public interface StateHandler {

    void stageIn(final StateContext context);

    String handleAction(final Action<?> action);

    void stageOut(final StateContext context);

}
