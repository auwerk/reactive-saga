package org.auwerk.arch.reactivesaga;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.auwerk.arch.reactivesaga.exception.SagaException;
import org.auwerk.arch.reactivesaga.log.ExecutionLog;
import org.auwerk.arch.reactivesaga.log.InMemoryExecutionLog;

import io.smallrye.mutiny.Uni;

public class Saga {

    private final ExecutionLog log = new InMemoryExecutionLog();
    private final List<Story> stories = new ArrayList<>();
    private final SagaContext context = new SagaContext();

    public UUID addStory(Function<SagaContext, Uni<Void>> workload, Function<SagaContext, Uni<Void>> compensation) {
        final var story = new Story(log, () -> workload.apply(context), () -> compensation.apply(context));
        stories.add(story);
        return story.getId();
    }

    public Uni<SagaContext> execute() {
        return executeAll()
                .replaceWith(context)
                .onFailure()
                .recoverWithUni(compensateAllComplete().invoke(() -> {
                    throw new SagaException(log.mapFailures());
                }).replaceWith(context));
    }

    private Uni<Void> executeAll() {
        return Uni.createFrom().voidItem().call(() -> {
            final var unis = stories.stream()
                    .map(story -> story.execute())
                    .toList();
            if (unis.isEmpty()) {
                return Uni.createFrom().voidItem();
            }
            return Uni.combine().all().unis(unis).collectFailures().discardItems();
        }).replaceWithVoid();
    }

    private Uni<Void> compensateAllComplete() {
        return Uni.createFrom().voidItem().call(() -> {
            final var unis = stories.stream()
                    .filter(story -> log.checkStoryCompletion(story.getId()))
                    .map(story -> story.compensate())
                    .toList();
            if (unis.isEmpty()) {
                return Uni.createFrom().nothing();
            }
            return Uni.combine().all().unis(unis)
                    .discardItems();
        }).replaceWithVoid();
    }
}
