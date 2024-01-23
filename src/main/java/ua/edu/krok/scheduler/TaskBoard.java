package ua.edu.krok.scheduler;

import java.util.List;

public interface TaskBoard {

    List<Assignment> getAssignments();

    void addAssignment(Assignment assignment);

    List<Task> getFinishedTasksAt(int time);

    List<Task> getAssignedTasksAt(int time);

    List<TeamMember> getFreeTeamMembers(int time);

    int getFinishTime();
}
