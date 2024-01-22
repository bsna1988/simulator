package ua.edu.krok.scheduler;

import java.util.Set;

public interface Project<T extends Task> {
    void addTask(T task, Set<T> blockedByTasks);

    T nextAvailableTask(TaskBoard taskBoard, int time);

    boolean hasRemainingTasks(TaskBoard taskBoard, int time);
}
