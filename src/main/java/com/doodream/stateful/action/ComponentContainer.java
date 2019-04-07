package com.doodream.stateful.action;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComponentContainer implements Comparable<ComponentContainer> {
    ActionPriority priority;
    RouterComponent component;

    @Override
    public int compareTo(ComponentContainer o) {
        return priority.ordinal() - o.priority.ordinal();
    }
}
