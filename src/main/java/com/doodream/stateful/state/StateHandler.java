package com.doodream.stateful.state;

import com.doodream.stateful.action.Action;
import com.doodream.stateful.action.ActionRouter;

public class StateHandler {

    ActionRouter router;

    public void stageIn(StateContext context) {
        if(router != null) {
            router.start(context);
        }
    }

    public String handleAction(Action<?> action) {
        if(router != null) {
            return router.routeAction(action);
        } else {
            return null;
        }
    }

    public void stageOut(StateContext context) {
        if(router != null) {
            router.stop(context);
        }
    }
}
