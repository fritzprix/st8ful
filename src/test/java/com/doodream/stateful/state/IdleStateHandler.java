package com.doodream.stateful.state;

import com.doodream.stateful.TestState;
import com.doodream.stateful.action.Action;
import com.doodream.stateful.action.ActionRouter;
import com.doodream.stateful.component.FarewellAcceptor;
import com.doodream.stateful.publisher.FarewellPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

@StateHandle(state = TestState.IDLE)
public class IdleStateHandler implements StateHandler {
    private static final Logger Log = LoggerFactory.getLogger(IdleStateHandler.class);
    private final AtomicReference<ActionRouter> router = new AtomicReference<>(null);

    @Override
    public void stageIn(StateContext context) {
        if(router.compareAndSet(null, ActionRouter.builder()
                .from(FarewellPublisher.class)
                .to(FarewellAcceptor.class)
                .build())) {
            Log.debug("router built for IDLE");
        }
        router.get().start(context);
    }

    @Override
    public String handleAction(Action<?> action) {
        return router.get().routeAction(action);
    }

    @Override
    public void stageOut(StateContext context) {
        if(router.get() == null) {
            return;
        }

        router.get().stop(context);
        router.set(null);
    }
}
