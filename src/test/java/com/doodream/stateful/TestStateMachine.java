package com.doodream.stateful;

import com.doodream.stateful.action.Action;
import com.doodream.stateful.component.ComputeComponent;
import com.doodream.stateful.state.ComputeStateHandler;
import com.doodream.stateful.state.IdleStateHandler;
import com.doodream.stateful.state.InitStateHandler;
import com.doodream.stateful.state.StateMachine;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestStateMachine {
    private static StateMachine stateMachine;

    @BeforeClass
    public static void init() throws Exception {
        stateMachine = StateMachine.builder()
                .setHandlers(
                        InitStateHandler.class,
                        IdleStateHandler.class,
                        ComputeStateHandler.class
                )
                .addParameter("message", "hello")
                .build();

        stateMachine.start();

        stateMachine.handle(Action.<String>builder()
                .name(TestAction.HELLO)
                .param("hello")
                .build());

        Assert.assertEquals(stateMachine.getState(), TestState.INIT);

        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) { }
        Assert.assertEquals(stateMachine.getState(), TestState.IDLE);
    }

    @AfterClass
    public static void exit() {
        stateMachine.stop();
    }



    @Test
    public void C_checkStateUpgrade() {
        stateMachine.handle(Action.builder()
                .name(TestAction.COMPUTE_START)
                .build());

        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}
        Assert.assertEquals(stateMachine.getState(), TestState.COMPUTE);
    }

    @Test
    public void D_checkConcurrentAction() {
        ExecutorService service = Executors.newWorkStealingPool(20);
        for (int i = 0; i < 20; i++) {
            if(i % 2 == 0) {
                service.submit(() -> {
                    stateMachine.handle(Action.builder()
                            .name(TestAction.COMPUTE)
                            .param(Integer.valueOf(1))
                            .build());
                });
            } else {
                service.submit(() -> {
                    stateMachine.handle(Action.builder()
                            .name(TestAction.COMPUTE)
                            .param(Integer.valueOf(-1))
                            .build());
                });
            }
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}
        Assert.assertEquals(ComputeComponent.getCount(), ComputeComponent.INIT_COUNT);
    }

    @Test
    public void E_checkReturnToIdle() {
        stateMachine.handle(Action.builder()
                .name(TestAction.COMPUTE_FINISH)
                .build());
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}
        Assert.assertEquals(stateMachine.getState(), TestState.IDLE);
    }

}
