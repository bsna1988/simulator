package ua.edu.krok.scheduler;

import java.util.Queue;
import java.util.Set;

public interface TaskSet<T extends Task> {
    Queue<T> getTasks();

    Set<T> blockedBy(T task);
}
