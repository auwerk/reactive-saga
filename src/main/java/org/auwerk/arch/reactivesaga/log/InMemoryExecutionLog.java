package org.auwerk.arch.reactivesaga.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public Map<UUID, Throwable> mapFailures() {
        return log.stream().filter(entry -> ExecutionEventType.FAILED.equals(entry.event.getType()))
                .collect(Collectors.toMap(entry -> entry.storyId, entry -> entry.event.getThrowable()));
    }

    @RequiredArgsConstructor
    private static class Entry {
        final UUID storyId;
        final ExecutionEvent event;
    }
}
