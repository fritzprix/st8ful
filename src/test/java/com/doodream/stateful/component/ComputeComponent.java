package com.doodream.stateful.component;

import com.doodream.stateful.action.Action;
import com.doodream.stateful.action.ActionPriority;
import com.doodream.stateful.action.ActionSubscriber;
import com.doodream.stateful.action.RouterComponent;
import com.doodream.stateful.state.StateContext;
import com.doodream.stateful.subscription.ComputeSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionSubscriber(priority = ActionPriority.PRESENTATION)
public class ComputeComponent implements ComputeSubscription, RouterComponent {

    public static final int INIT_COUNT = 0;
    private static final Logger Log = LoggerFactory.getLogger(ComputeComponent.class);
    private volatile static int count = 0;

    public static int getCount() {
        return count;
    }

    @Override
    public void start(StateContext context) {

    }

    @Override
    public void stop(StateContext context) {
    }

    @Override
    public void onComputeStart(Action<String> action) {

    }

    @Override
    public void onCompute(Action<Integer> action) {
        int value = action.getParam();
        count += value;
        Log.debug("count : {}", count);
    }

    @Override
    public void onComputeFinish(Action<String> action) {

    }
}
