package com.doodream.stateful.action;

import com.doodream.stateful.state.StateTransition;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActionLookupTable {

    private static final Logger Log = LoggerFactory.getLogger(ActionLookupTable.class);

    private Map<Action<?>, Method> actionMethodMap;
    private Map<Action<?>, StateTransition> stateTransitionMap;
    private Map<Action<?>, ActionHandler> handlerMap;

    private ActionLookupTable(Map<Action<?>, Method> subscriptionLookup, Map<Action<?>, StateTransition> transitionMap, ConcurrentHashMap<Action<?>, ActionHandler> handlerMap) {
        actionMethodMap = subscriptionLookup;
        stateTransitionMap = transitionMap;
        this.handlerMap = handlerMap;
    }


    public static ActionLookupTable create(Class<?> ...subscriptions) {
        ConcurrentHashMap<Action<?>, Method> subscriptionLookup = new ConcurrentHashMap<>();
        ConcurrentHashMap<Action<?>, StateTransition> trasitionMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<Action<?>, ActionHandler> handlerMap = new ConcurrentHashMap<>();

        for (Class<?> subscription : subscriptions) {
            Observable.fromArray(subscription.getMethods())
                    .filter(m -> isActionHandler(m))
                    .blockingSubscribe(m -> {
                        ActionHandler handler = m.getAnnotation(ActionHandler.class);
                        StateTransition transition = m.getAnnotation(StateTransition.class);

                        final Action action = Action.builder()
                                .name(handler.name())
                                .build();

                        subscriptionLookup.put(action, m);
                        handlerMap.put(action, handler);

                        if(transition != null) {
                            trasitionMap.put(action, transition);
                        }
                    });
        }

        return new ActionLookupTable(
                Collections.unmodifiableMap(subscriptionLookup),
                Collections.unmodifiableMap(trasitionMap),
                handlerMap);
    }

    private static boolean isActionHandler(Method m) {
        return m.isAnnotationPresent(ActionHandler.class);
    }

    public LookupResult lookup(final Action<?> action) {
        final Method method = actionMethodMap.get(action);
        final ActionHandler handler = handlerMap.get(action);
        if(method == null || handler == null) {
            return LookupResult.empty();
        }
        final StateTransition transition = stateTransitionMap.get(action);

        return LookupResult.builder()
                .method(method)
                .scope(method.getDeclaringClass().getName())
                .transition(transition)
                .param(handler.param())
                .build();
    }
}
