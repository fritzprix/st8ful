package com.doodream.stateful.action;

import com.doodream.stateful.state.StateContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = {"param", "scope", "stateContext"})
public class Action<T> {
    private String name;
    private T param;

    private transient String scope;
    private transient StateContext stateContext;

    public static final Action<?> EMPTY = new Action<>();

    public static boolean isEmpty(Action<?> action) {
        return action.equals(EMPTY);
    }

    public static <T> Action<T> empty() {
        return new Action<>();
    }

    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    private Action() {
        name = "";
        param = null;
    }

    public static class Builder<T> {

        private final Action<T> action = Action.empty();

        private Builder() {
            action.name = "";
            action.scope = "";
            action.param = null;
        }

        public Builder<T> name(String name) {
            action.name = name;
            return this;
        }

        public Builder<T> param(T param) {
            action.param = param;
            return this;
        }

        public Builder<T> scope(String scope) {
            action.scope = scope;
            return this;
        }

        public Action<T> build() {
            return action;
        }
    }

}
