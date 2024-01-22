package ua.edu.krok.taskset;

import ua.edu.krok.scheduler.TaskSet;

public interface TaskSetGenerator {
    TaskSet generate(int taskCount, int edgeCount, int maxEstimatedTime);
}
