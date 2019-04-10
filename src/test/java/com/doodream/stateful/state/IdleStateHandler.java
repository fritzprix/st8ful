package com.doodream.stateful.state;

import com.doodream.stateful.TestState;
import com.doodream.stateful.action.ActionRoute;
import com.doodream.stateful.component.FarewellAcceptor;
import com.doodream.stateful.publisher.FarewellPublisher;

@ActionRoute(
        from = {
                FarewellPublisher.class
        },to = {
                FarewellAcceptor.class
        })
@StateHandle(state = TestState.IDLE)
public class IdleStateHandler extends StateHandler {
}
