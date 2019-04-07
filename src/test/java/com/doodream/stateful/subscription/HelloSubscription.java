package com.doodream.stateful.subscription;

import com.doodream.stateful.TestAction;
import com.doodream.stateful.TestState;
import com.doodream.stateful.action.Action;
import com.doodream.stateful.action.ActionHandler;
import com.doodream.stateful.action.Subscription;
import com.doodream.stateful.state.StateTransition;

@Subscription
public interface HelloSubscription {

    @StateTransition(next=TestState.IDLE)
    @ActionHandler(name = TestAction.HELLO, param=String.class)
    void handleHelloAction(final Action<String> action);
}
