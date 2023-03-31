package org.auwerk.arch.reactivesaga.log;

import lombok.Getter;

public class ExecutionEvent {

    @Getter
    private final ExecutionEventType type;
    @Getter
    private Throwable throwable = null;

    private ExecutionEvent(ExecutionEventType type, Throwable throwable) {
        this.type = type;
        this.throwable = throwable;
    }

    public static ExecutionEvent started() {
        return new ExecutionEvent(ExecutionEventType.STARTED, null);
    }

    public static ExecutionEvent completed() {
        return new ExecutionEvent(ExecutionEventType.COMPLETED, null);
    }

    public static ExecutionEvent failed(Throwable throwable) {
        return new ExecutionEvent(ExecutionEventType.FAILED, throwable);
    }
}
