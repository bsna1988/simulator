package ua.edu.krok.scheduler.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import ua.edu.krok.scheduler.Task;

@EqualsAndHashCode(of = "id")
@Getter
public class TimedTask implements Task {
    private final int estimatedHours;
    private final int id;

    public TimedTask(int id, int estimatedHors) {
        this.id = id;
        this.estimatedHours = estimatedHors;
    }

    public String toString() {
        return String.format("Task %d (%dh)", id, estimatedHours);
    }

    public int getEstimatedHours() {
        return estimatedHours;
    }

    @Override
    public int getId() {
        return id;
    }
}
