package ua.edu.krok.scheduler.impl;

import java.util.List;

import ua.edu.krok.scheduler.Project;
import ua.edu.krok.scheduler.Simulator;
import ua.edu.krok.scheduler.TaskBoard;
import ua.edu.krok.scheduler.Team;
import ua.edu.krok.scheduler.TeamMember;

public class DefaultSimulator implements Simulator<TimedTask> {

    @Override
    public TaskBoard simulate(Project<TimedTask> project, Team team) {
        int time = 0;
        TaskBoard taskBoard = new DefaultTaskBoard(team);

        while (project.hasRemainingTasks(taskBoard, time)) {
            List<TeamMember> freeTeamMembers =
                taskBoard.getFreeTeamMembers(time);

            for (TeamMember freeTeamMember : freeTeamMembers) {
                TimedTask task = project.nextAvailableTask(taskBoard, time);
                if (task == null) {
                    break;
                }
                taskBoard.addAssignment(
                    new DefaultAssignment(freeTeamMember, task, freeTeamMember.whenCanStartTask(time),
                        freeTeamMember.whenCanFinishTask(time, task.getEstimatedHours())));
            }
            time++;
        }

        return taskBoard;
    }
}
