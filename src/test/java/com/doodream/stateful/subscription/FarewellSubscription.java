package com.doodream.stateful.subscription;

import com.doodream.stateful.TestAction;
import com.doodream.stateful.TestState;
import com.doodream.stateful.action.Action;
import com.doodream.stateful.action.ActionHandler;
import com.doodream.stateful.action.Subscription;
import com.doodream.stateful.state.StateTransition;

@Subscription
public interface FarewellSubscription {

    @StateTransition(next = TestState.INIT)
    @ActionHandler(name = TestAction.FAREWELL, param = String.class)
    void handleFarewellAction(final Action<String> action);
}
