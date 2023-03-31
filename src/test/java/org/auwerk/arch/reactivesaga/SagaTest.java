package org.auwerk.arch.reactivesaga;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.auwerk.arch.reactivesaga.exception.SagaException;
import org.auwerk.arch.reactivesaga.log.ExecutionLog;
import org.auwerk.arch.reactivesaga.log.InMemoryExecutionLog;
import org.junit.jupiter.api.Test;

import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;

public class SagaTest {

    private final ExecutionLog executionLog = new InMemoryExecutionLog();

    @Test
    void executeSaga_noStories() {
        // given
        final var saga = new Saga(executionLog);

        // when
        final var subscriber = saga.execute().subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        subscriber.assertCompleted();
    }

    @Test
    void executeSaga_allStoriesComplete() {
        // given
        final var saga = new Saga(executionLog);
        final var workloads = List.of(new StoryWorkloadMock(), new StoryWorkloadMock(), new StoryWorkloadMock(),
                new StoryWorkloadMock());

        // when
        workloads.forEach(workload -> saga.addStory(context -> workload.execute(context),
                context -> workload.compensate(context)));
        final var subscriber = saga.execute().subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        subscriber.assertCompleted();

        assertAll(workloads.stream().map(workload -> () -> assertTrue(workload.isExecuted())));
    }

    @Test
    void executeSaga_completeStoriesCompensatedOnFailure() {
        // given
        final var storyFailure = new RuntimeException();
        final var saga = new Saga(executionLog);
        final var workloads = List.of(new StoryWorkloadMock(), new StoryWorkloadMock(storyFailure),
                new StoryWorkloadMock(), new StoryWorkloadMock());

        // when
        workloads.forEach(workload -> workload.setStoryId(saga.addStory(context -> workload.execute(context),
                context -> workload.compensate(context))));
        final var subscriber = saga.execute().subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        subscriber.assertFailedWith(SagaException.class);

        assertAll(workloads.stream()
                .filter(workload -> workload.shouldFail())
                .map(workload -> () -> {
                    final var loggedFailure = executionLog.getStoryFailure(workload.getStoryId());
                    assertTrue(loggedFailure.isPresent());
                    assertSame(workload.getThrowable(), loggedFailure.get());
                }));

        assertAll(workloads.stream().map(workload -> () -> assertTrue(workload.isExecuted())));
        assertAll(workloads.stream()
                .filter(workload -> !workload.shouldFail())
                .map(workload -> () -> assertTrue(workload.isCompensated())));
        assertAll(workloads.stream()
                .filter(workload -> workload.shouldFail())
                .map(workload -> () -> assertFalse(workload.isCompensated())));
    }
}
