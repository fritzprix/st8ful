package com.doodream.stateful.action;

import com.doodream.stateful.state.StateTransition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LookupResult {

    private static final LookupResult EMPTY = LookupResult.builder().build();
    private String scope;
    private Method method;
    private Class param;
    private StateTransition transition;

    public static LookupResult empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }
}
