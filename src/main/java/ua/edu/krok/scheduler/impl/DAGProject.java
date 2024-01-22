package ua.edu.krok.scheduler.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import ua.edu.krok.scheduler.Project;
import ua.edu.krok.scheduler.Task;
import ua.edu.krok.scheduler.TaskBoard;
import ua.edu.krok.scheduler.TaskSet;

public class DAGProject<T extends Task> implements Project<T>, TaskSet<T> {
    private final Queue<T> tasks = new LinkedList<>();
    private final Map<T, Set<T>> blockedByMap = new HashMap<>();

    public DAGProject(TaskSet<T> taskSet) {
        taskSet.getTasks().forEach(task -> {
            addTask(task, taskSet.blockedBy(task));
        });
    }

    public DAGProject() {

    }

    @Override
    public void addTask(T task, Set<T> blockedByList) {
        tasks.add(task);
        blockedByMap.put(task, blockedByList);
    }

    @Override
    public Set<T> blockedBy(Task task) {
        return blockedByMap.get(task);
    }

    @Override
    public T nextAvailableTask(TaskBoard taskBoard, int time) {
        Optional<T> nextTask =
            tasks.stream().filter(task -> !taskBoard.getAssignedTasksAt(time).contains(task))
                .filter(task -> {
                    HashSet<Task> blockedBy = new HashSet<>(blockedBy(task));
                    taskBoard.getFinishedTasksAt(time).forEach(blockedBy::remove);
                    return blockedBy.isEmpty();
                }).findFirst();
        return nextTask.orElse(null);
    }

    @Override
    public boolean hasRemainingTasks(TaskBoard taskBoard, int time) {
        return !new HashSet<>(taskBoard.getFinishedTasksAt(time)).containsAll(tasks);
    }

    @Override
    public Queue<T> getTasks() {
        return tasks;
    }
}
