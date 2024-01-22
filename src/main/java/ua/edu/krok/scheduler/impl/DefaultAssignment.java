package ua.edu.krok.scheduler.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ua.edu.krok.scheduler.Assignment;
import ua.edu.krok.scheduler.Task;
import ua.edu.krok.scheduler.TeamMember;

@Getter
@AllArgsConstructor
public class DefaultAssignment implements Assignment {
    private TeamMember teamMember;
    private Task task;
    private int startTime;
    private int finishTime;

    @Override
    public String toString() {
        return task + " is assigned to " + teamMember + " at " + startTime + ". Finish at " +
            finishTime;
    }
}
