package com.doodream.stateful.action;

import com.doodream.stateful.state.StateContext;
import com.doodream.stateful.state.StateTransition;
import io.reactivex.Observable;
import io.reactivex.annotations.Nullable;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ActionRouter {

    private static final Logger Log = LoggerFactory.getLogger(ActionRouter.class);
    private final ActionScheduler actionScheduler = new ActionScheduler();

    private HashSet<ActionPublisher> publishers;
    private ActionLookupTable lookupTable;
    private Map<String, Set<ComponentContainer>> subscriptionMap;
    private Set<ComponentContainer> components;

    public static Builder builder() {
        return new Builder();
    }

    public void start(StateContext context) {
        Log.debug("start {}", context.getState());
        actionScheduler.start(action -> context.handle(action));
        for (ComponentContainer component : components) {
            component.getComponent().start(context);
        }

        for (ActionPublisher publisher : publishers) {
            publisher.startListen(context, actionScheduler);
        }
    }

    public void stop(StateContext context) {

        for (ActionPublisher publisher : publishers) {
            publisher.stopListen(context);
        }

        for (ComponentContainer component : components) {
            component.getComponent().stop(context);
        }
    }

    @Nullable public String routeAction(final Action<?> action) {
        final LookupResult result = lookupTable.lookup(action);
        if(result.isEmpty()) {
            Log.debug("fail to lookup : {}", action);
            return null;
        }
        Log.debug("scope lookup result {}", result);

        Set<ComponentContainer> containers = subscriptionMap.get(result.getScope());
        if(containers == null) {
            Log.debug("no subscription found : {}", action);
        }
        final Method method = result.getMethod();
        final StateTransition transition = result.getTransition();
        for (ComponentContainer container : containers) {
            try {
                method.invoke(container.component, action);
            } catch (IllegalAccessException | InvocationTargetException e) {
                Log.warn("fail to handle {} @ {} : {}",
                        action,
                        container.component.getClass().getSimpleName(),
                        e.getMessage());
            }
        }
        if(transition == null) {
            return null;
        }
        return transition.next();
    }

    public static class Builder {
        private static final Logger Log = LoggerFactory.getLogger(Builder.class);
        private final ActionRouter router = new ActionRouter();
        private Builder() { }

        public ActionRouter build() {
            if(router.publishers == null) {
                router.publishers = new HashSet<>();
            }
            return router;
        }

        public Builder from(Class<? extends ActionPublisher> ...pubs) {
            router.publishers = Observable.fromArray(pubs)
                    .filter(cls -> isValidPublisher(cls))
                    .map(cls -> cls.newInstance())
                    .collectInto(new HashSet<ActionPublisher>(), (set, pub) -> {
                        if(!set.add(pub)) {
                            Log.warn("fail to add publisher {} : already on the set", pub.getClass().getName());
                        }
                    })
                    .blockingGet();

            return this;
        }

        public Builder to(Class<? extends RouterComponent> ...comps) {

            final Set<Class> subscriptions = Collections.newSetFromMap(new ConcurrentHashMap<>());

            final TreeSet<ComponentContainer> componentContainers = new TreeSet<>();

            Map<String, Set<ComponentContainer>> initMap = new HashMap<>();
            router.subscriptionMap = Observable.fromArray(comps)
                    .filter(cls -> isValidSubscriber(cls))
                    .subscribeOn(Schedulers.computation())
                    .doOnNext(cls -> {
                        Class[] itfcs = cls.getInterfaces();
                        for (Class itfc : itfcs) {
                            if(isValidSubscription(itfc)) {
                                subscriptions.add(itfc);
                            }
                        }
                    })
                    .flatMap(cls -> buildSubscriptionMap(cls))
                    .collectInto(initMap, (map, localMap) -> {
                        for (Map.Entry<String, ComponentContainer> entry : localMap.entrySet()) {
                            componentContainers.add(entry.getValue());
                            if(map.containsKey(entry.getKey())) {
                                final Set<ComponentContainer> containers = map.get(entry.getKey());
                                containers.add(entry.getValue());
                            } else {
                                final Set<ComponentContainer> containers = new TreeSet<>();
                                containers.add(entry.getValue());
                                map.put(entry.getKey(), containers);
                            }
                        }
                    })
                    .blockingGet();



            router.components = componentContainers;
            router.lookupTable = ActionLookupTable.create(subscriptions.toArray(new Class[0]));
            return this;
        }

        private Observable<Map<String, ComponentContainer>> buildSubscriptionMap(Class<? extends RouterComponent> compCls) throws IllegalAccessException, InstantiationException {
            final RouterComponent component = compCls.newInstance();
            Map<String, ComponentContainer> into = new HashMap<>();
            Log.debug("build subscription map");

            return Observable.fromArray(compCls.getInterfaces())
                    .filter(cls -> isValidSubscription(cls))
                    .collectInto(into, (map, cls) -> {
                        ActionSubscriber subscriber = compCls.getAnnotation(ActionSubscriber.class);
                        map.put(cls.getName(), ComponentContainer.builder()
                                .component(component)
                                .priority(subscriber.priority())
                                .build());
                    })
                    .toObservable();
        }

        private Observable<HashSet<Class<?>>> getSubscription(Class<? extends RouterComponent> compCls) {
            return Observable.fromArray(compCls.getInterfaces())
                    .filter(cls -> isValidSubscription(cls))
                    .collectInto(new HashSet<Class<?>>(), (set, cls) -> {
                        set.add(cls);
                    })
                    .toObservable();
        }


        private boolean isValidSubscription(Class<?> cls) {
            return cls.isAnnotationPresent(Subscription.class);
        }


        private boolean isValidSubscriber(Class<?> cls) {
            return cls.isAnnotationPresent(ActionSubscriber.class);
        }

        private boolean isValidPublisher(Class<? extends ActionPublisher> cls) {
            for (Class<?> anInterface : cls.getInterfaces()) {
                if(anInterface.equals(ActionPublisher.class)) {
                    return true;
                }
            }
            return false;
        }

    }
}
