package org.auwerk.arch.reactivesaga.exception;

import java.util.Map;
import java.util.UUID;

import lombok.Getter;

public class SagaException extends RuntimeException {

    @Getter
    private final Map<UUID, Throwable> failureMap;

    public SagaException(Map<UUID, Throwable> failureMap) {
        this.failureMap = failureMap;
    }

    public int storiesFailed() {
        return failureMap.size();
    }
}
