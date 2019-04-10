package com.doodream.stateful.state;

import com.doodream.stateful.TestState;
import com.doodream.stateful.action.ActionRoute;
import com.doodream.stateful.component.HelloAcceptor;

@StateHandle(state= TestState.INIT)
// inject router
@ActionRoute(to = {
        HelloAcceptor.class
})
public class InitStateHandler extends StateHandler {

}
