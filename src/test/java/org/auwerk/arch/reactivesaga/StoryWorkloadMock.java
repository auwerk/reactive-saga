package org.auwerk.arch.reactivesaga;

import java.util.Optional;
import java.util.UUID;

import io.smallrye.mutiny.Uni;
import lombok.Getter;
import lombok.Setter;

public class StoryWorkloadMock {

    private final Optional<Throwable> shouldFailWith;

    @Getter
    private boolean executed = false;
    @Getter
    private boolean compensated = false;

    @Getter
    @Setter
    private UUID storyId;

    public StoryWorkloadMock() {
        this.shouldFailWith = Optional.empty();
    }

    public StoryWorkloadMock(Throwable shouldFailWith) {
        this.shouldFailWith = Optional.of(shouldFailWith);
    }

    public boolean shouldFail() {
        return shouldFailWith.isPresent();
    }

    public Uni<Void> execute(SagaContext context) {
        return Uni.createFrom().emitter(emitter -> {
            executed = true;
            shouldFailWith.ifPresentOrElse(
                    t -> emitter.fail(t),
                    () -> emitter.complete(null));
        }).replaceWithVoid();
    }

    public Uni<Void> compensate(SagaContext context) {
        return Uni.createFrom().emitter(emitter -> {
            compensated = true;
            emitter.complete(null);
        }).replaceWithVoid();
    }
}
