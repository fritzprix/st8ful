package com.doodream.stateful.state;

import com.doodream.stateful.TestState;
import com.doodream.stateful.action.ActionRoute;
import com.doodream.stateful.component.ComputeComponent;

@ActionRoute(to = ComputeComponent.class)
@StateHandle(state = TestState.COMPUTE)
public class ComputeStateHandler extends StateHandler {

}
