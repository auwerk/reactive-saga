package org.auwerk.arch.reactivesaga;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public class SagaContext {

    @Getter
    private final Map<String, Object> values = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        return (T) values.get(key);
    }
}
