package org.auwerk.arch.reactivesaga;

import java.util.UUID;
import java.util.function.Supplier;

import org.auwerk.arch.reactivesaga.log.ExecutionEvent;
import org.auwerk.arch.reactivesaga.log.ExecutionLog;

import io.smallrye.mutiny.Uni;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Story {

    @Getter
    private final UUID id = UUID.randomUUID();

    private final ExecutionLog executionLog;
    private final Supplier<Uni<Void>> workload;
    private final Supplier<Uni<Void>> compensation;

    public Uni<Void> execute() {
        executionLog.logEvent(id, ExecutionEvent.started());
        return workload.get()
                .invoke(() -> executionLog.logEvent(id, ExecutionEvent.completed()))
                .onFailure()
                .invoke(ex -> {
                    executionLog.logEvent(id, ExecutionEvent.failed(ex));
                });
    }

    public Uni<Void> compensate() {
        return compensation.get();
    }
}
