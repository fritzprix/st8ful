package com.doodream.stateful.subscription;

import com.doodream.stateful.TestAction;
import com.doodream.stateful.TestState;
import com.doodream.stateful.action.Action;
import com.doodream.stateful.action.ActionHandler;
import com.doodream.stateful.action.Subscription;
import com.doodream.stateful.state.StateTransition;

@Subscription
public interface ComputeSubscription {

    @StateTransition(next = TestState.COMPUTE)
    @ActionHandler(name = TestAction.COMPUTE_START, param = String.class)
    void onComputeStart(Action<String> action);

    @ActionHandler(name = TestAction.COMPUTE, param = Integer.class)
    void onCompute(Action<Integer> action);


    @StateTransition(next = TestState.IDLE)
    @ActionHandler(name = TestAction.COMPUTE_FINISH, param = String.class)
    void onComputeFinish(Action<String> action);
}
