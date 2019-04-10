package com.doodream.stateful.component;

import com.doodream.stateful.action.Action;
import com.doodream.stateful.action.ActionPriority;
import com.doodream.stateful.action.ActionSubscriber;
import com.doodream.stateful.action.RouterComponent;
import com.doodream.stateful.state.StateContext;
import com.doodream.stateful.subscription.HelloSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionSubscriber(priority=ActionPriority.UPDATE)
public class HelloAcceptor implements HelloSubscription, RouterComponent {
    private Logger Log = LoggerFactory.getLogger(HelloAcceptor.class);

    @Override
    public void handleHelloAction(Action<String> action) throws Exception {
        Log.debug("Hello");
        throw new Exception("fuck");
    }

    @Override
    public void start(StateContext context) {
        Log.debug("start");
    }

    @Override
    public void stop(StateContext context) {
        Log.debug("stop");
    }
}
