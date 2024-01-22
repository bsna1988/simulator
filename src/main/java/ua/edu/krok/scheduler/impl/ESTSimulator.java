package ua.edu.krok.scheduler.impl;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import ua.edu.krok.scheduler.Project;
import ua.edu.krok.scheduler.Simulator;
import ua.edu.krok.scheduler.TaskBoard;
import ua.edu.krok.scheduler.Team;
import ua.edu.krok.scheduler.TeamMember;

public class ESTSimulator implements Simulator<TimedTask> {

    @Override
    public TaskBoard simulate(Project<TimedTask> project, Team team) {
        final AtomicInteger time = new AtomicInteger();

        TaskBoard taskBoard = new DefaultTaskBoard(team);

        while (project.hasRemainingTasks(taskBoard, time.get())) {

            TimedTask task = project.nextAvailableTask(taskBoard, time.get());

            if (task != null) {
                Optional<TeamMember> teamMemberWhoCanStartNow =
                    taskBoard.getFreeTeamMembers(time.get())
                        .stream()
                        .sorted(Comparator.comparingInt(
                            teamMember -> teamMember.whenCanStartTask(time.get())))
                        .filter(teamMember -> teamMember.whenCanStartTask(time.get()) == time.get())
                        .findFirst();
                if (teamMemberWhoCanStartNow.isPresent()) {
                    taskBoard.addAssignment(
                        new DefaultAssignment(teamMemberWhoCanStartNow.get(), task,
                            teamMemberWhoCanStartNow.get().whenCanStartTask(time.get()),
                            teamMemberWhoCanStartNow.get()
                                .whenCanFinishTask(time.get(), task.getEstimatedHours())));
                    continue;
                }
            }
            time.incrementAndGet();
        }

        return taskBoard;
    }
}
