package com.doodream.stateful.state;

import com.doodream.stateful.action.Action;
import com.doodream.stateful.action.ActionRouter;

public class StateHandler {

    ActionRouter router;

    protected void stageIn(StateContext context) {
        if(router != null) {
            router.start(context);
        }
    }

    protected String handleAction(Action<?> action) {
        if(router != null) {
            return router.routeAction(action);
        } else {
            return null;
        }
    }

    protected void stageOut(StateContext context) {
        if(router != null) {
            router.stop(context);
        }
    }
}
