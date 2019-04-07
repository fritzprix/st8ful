package com.doodream.stateful.state;

import com.doodream.stateful.TestState;
import com.doodream.stateful.action.Action;
import com.doodream.stateful.action.ActionRouter;
import com.doodream.stateful.component.HelloAcceptor;
import com.doodream.stateful.publisher.FarewellPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

@StateHandle(state= TestState.INIT)
public class InitStateHandler implements StateHandler {


    private static final Logger Log = LoggerFactory.getLogger(InitStateHandler.class);
    private AtomicReference<ActionRouter> router = new AtomicReference<>(null);

    public void stageIn(StateContext context) {
        if(router.compareAndSet(null, ActionRouter.builder()
                .to(HelloAcceptor.class)
                .build())) {
            Log.debug("router initialized");
        } else {
            Log.debug("fail to initialized");
        }
        router.get().start(context);
    }

    public String handleAction(final Action<?> action) {
        Log.debug("handle action {}",action);
        return router.get().routeAction(action);
    }

    public void stageOut(StateContext context) {
        if(router.get() == null) {
            Log.debug("no router to stop");
            return;
        }
        router.get().stop(context);
    }

}
