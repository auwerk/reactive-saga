package org.auwerk.arch.reactivesaga.log;

import java.util.Optional;
import java.util.UUID;

public interface ExecutionLog {

    void logEvent(UUID storyId, ExecutionEvent event);

    boolean checkStoryCompletion(UUID storyId);

    Optional<Throwable> getStoryFailure(UUID storyId);
}
