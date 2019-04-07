package com.doodream.stateful.publisher;

import com.doodream.stateful.TestAction;
import com.doodream.stateful.action.Action;
import com.doodream.stateful.action.ActionPublisher;
import com.doodream.stateful.action.ActionScheduler;
import com.doodream.stateful.state.StateContext;

import java.util.concurrent.TimeUnit;

public class FarewellPublisher implements ActionPublisher {


    @Override
    public void startListen(StateContext context, ActionScheduler actionScheduler) {
        actionScheduler.scheduleAction(Action.builder()
                .name(TestAction.FAREWELL)
                .param("see you again")
                .build(), 1L, TimeUnit.SECONDS);
    }

    @Override
    public void stopListen(StateContext context) {

    }
}
