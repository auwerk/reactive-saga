package org.auwerk.arch.reactivesaga.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

public class InMemoryExecutionLog implements ExecutionLog {

    private final List<Entry> log = new ArrayList<>();

    @Override
    public void logEvent(UUID storyId, ExecutionEvent event) {
        log.add(new Entry(storyId, event));
    }

    @Override
    public boolean checkStoryCompletion(UUID storyId) {
        return log.stream()
                .anyMatch(entry -> storyId.equals(entry.storyId)
                        && ExecutionEventType.COMPLETED.equals(entry.event.getType()));
    }

    @Override
    public Optional<Throwable> getStoryFailure(UUID storyId) {
        return log.stream()
                .filter(entry -> ExecutionEventType.FAILED.equals(entry.event.getType())
                        && storyId.equals(entry.storyId))
                .map(entry -> entry.event.getThrowable())
                .findFirst();
    }

    @RequiredArgsConstructor
    private static class Entry {
        final UUID storyId;
        final ExecutionEvent event;
    }
}
