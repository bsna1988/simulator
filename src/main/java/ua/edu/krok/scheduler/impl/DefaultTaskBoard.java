package ua.edu.krok.scheduler.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import ua.edu.krok.scheduler.Assignment;
import ua.edu.krok.scheduler.Task;
import ua.edu.krok.scheduler.TaskBoard;
import ua.edu.krok.scheduler.Team;
import ua.edu.krok.scheduler.TeamMember;


@RequiredArgsConstructor
public class DefaultTaskBoard implements TaskBoard {
    private final List<Assignment> assignments = new ArrayList<>();
    private final Team team;

    @Override
    public void addAssignment(Assignment assignment) {
        this.assignments.add(assignment);
    }

    @Override
    public List<Task> getFinishedTasksAt(int time) {
        return assignments.stream()
            .filter(assignment -> assignment.getFinishTime() <= time)
            .map(Assignment::getTask)
            .collect(Collectors.toList());
    }

    @Override
    public List<Task> getAssignedTasksAt(int time) {
        return assignments.stream()
            .map(Assignment::getTask)
            .collect(Collectors.toList());
    }

    @Override
    public List<TeamMember> getFreeTeamMembers(int time) {
        return team.teamMembers().stream().filter(
                teamMember -> assignments.stream()
                    .filter(assignment -> assignment.getFinishTime() > time)
                    .filter(assignment ->
                        assignment.getTeamMember().equals(teamMember)).findAny().isEmpty())
            .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return assignments.stream()
            .map(Object::toString)
            .collect(Collectors.joining("\n"));
    }
}
