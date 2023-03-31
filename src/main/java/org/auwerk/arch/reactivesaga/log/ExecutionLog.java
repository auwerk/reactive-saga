package org.auwerk.arch.reactivesaga.log;

import java.util.Map;
import java.util.UUID;

public interface ExecutionLog {

    void logEvent(UUID storyId, ExecutionEvent event);

    boolean checkStoryCompletion(UUID storyId);

    Map<UUID, Throwable> mapFailures();
}
