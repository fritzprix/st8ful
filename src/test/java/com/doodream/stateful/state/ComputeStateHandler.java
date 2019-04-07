package com.doodream.stateful.state;

import com.doodream.stateful.TestState;
import com.doodream.stateful.action.Action;
import com.doodream.stateful.action.ActionRouter;
import com.doodream.stateful.component.ComputeComponent;

import java.util.concurrent.atomic.AtomicReference;

@StateHandle(state = TestState.COMPUTE)
public class ComputeStateHandler implements StateHandler {

    private final AtomicReference<ActionRouter> router = new AtomicReference<>();


    @Override
    public void stageIn(StateContext context) {
        if(router.compareAndSet(null, ActionRouter.builder()
                .to(ComputeComponent.class)
                .build())) {

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
    }
}
