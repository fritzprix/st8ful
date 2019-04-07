package com.doodream.stateful.component;

import com.doodream.stateful.action.Action;
import com.doodream.stateful.action.ActionPriority;
import com.doodream.stateful.action.ActionSubscriber;
import com.doodream.stateful.action.RouterComponent;
import com.doodream.stateful.state.StateContext;
import com.doodream.stateful.subscription.ComputeSubscription;
import com.doodream.stateful.subscription.FarewellSubscription;

@ActionSubscriber(priority = ActionPriority.PRESENTATION)
public class FarewellAcceptor implements FarewellSubscription, ComputeSubscription, RouterComponent {

    @Override
    public void start(StateContext context) {

    }

    @Override
    public void stop(StateContext context) {

    }

    @Override
    public void handleFarewellAction(Action<String> action) {

    }

    @Override
    public void onComputeStart(Action<String> action) {

    }

    @Override
    public void onCompute(Action<Integer> action) {

    }

    @Override
    public void onComputeFinish(Action<String> action) {

    }
}
