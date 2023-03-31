package org.auwerk.arch.reactivesaga;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.auwerk.arch.reactivesaga.exception.SagaException;
import org.junit.jupiter.api.Test;

import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;

public class SagaTest {

    @Test
    void executeSaga_noStories() {
        // given
        final var saga = new Saga();

        // when
        final var subscriber = saga.execute().subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        subscriber.assertCompleted();
    }

    @Test
    void executeSaga_allStoriesComplete() {
        // given
        final var saga = new Saga();
        final var stories = List.of(new StoryWorkloadMock(), new StoryWorkloadMock(), new StoryWorkloadMock(),
                new StoryWorkloadMock());

        // when
        stories.forEach(story -> saga.addStory(context -> story.execute(context),
                context -> story.compensate(context)));
        final var subscriber = saga.execute().subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        subscriber.assertCompleted();

        assertAll(stories.stream().map(story -> () -> assertTrue(story.isExecuted())));
    }

    @Test
    void executeSaga_completeStoriesCompensatedOnFailure() {
        // given
        final var storyFailure = new RuntimeException();
        final var saga = new Saga();
        final var stories = List.of(new StoryWorkloadMock(), new StoryWorkloadMock(storyFailure),
                new StoryWorkloadMock(), new StoryWorkloadMock());

        // when
        stories.forEach(story -> story.setStoryId(saga.addStory(context -> story.execute(context),
                context -> story.compensate(context))));
        final var subscriber = saga.execute().subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        final var failure = (SagaException) subscriber
                .assertFailedWith(SagaException.class)
                .getFailure();

        final var failureMap = failure.getFailureMap();
        assertNotNull(failureMap);
        assertEquals(1, failureMap.size());
        assertSame(storyFailure, failureMap.get(stories.get(1).getStoryId()));

        assertAll(stories.stream().map(story -> () -> assertTrue(story.isExecuted())));
        assertAll(stories.stream()
                .filter(story -> !story.shouldFail())
                .map(story -> () -> assertTrue(story.isCompensated())));
        assertAll(stories.stream()
                .filter(story -> story.shouldFail())
                .map(story -> () -> assertFalse(story.isCompensated())));
    }
}
