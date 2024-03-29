package ua.edu.krok.scheduler.impl;

import static java.util.Comparator.naturalOrder;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.RequiredArgsConstructor;
import ua.edu.krok.scheduler.Assignment;
import ua.edu.krok.scheduler.Project;
import ua.edu.krok.scheduler.Scheduler;
import ua.edu.krok.scheduler.TaskBoard;
import ua.edu.krok.scheduler.Team;
import ua.edu.krok.scheduler.TeamMember;

@RequiredArgsConstructor
public class ESTWorkHoursUnawareScheduler implements Scheduler<TimedTask> {
    public static final int START_HOUR = 9;

    @Override
    public TaskBoard simulate(Project<TimedTask> project, Team team) {
        final AtomicInteger time = new AtomicInteger();

        DefaultTeam sameWorkHoursTeam = new DefaultTeam();
        for (TeamMember teamMember : team.teamMembers()) {
            sameWorkHoursTeam.addTeamMember(
                new ScheduleAwareTeamMember(teamMember.getId(), ZoneOffset.UTC, START_HOUR));
        }
        DAGProject<TimedTask> dagProject = (DAGProject<TimedTask>) project;
        TaskBoard taskBoard = scheduleSameWorkHoursTeam(project, time, sameWorkHoursTeam);

        TaskBoard adjustedTaskBoard = new DefaultTaskBoard(team);
        for (Assignment assignment : taskBoard.getAssignments()) {
            ScheduleAwareTeamMember teamMember =
                (ScheduleAwareTeamMember) team.teamMembers()
                    .stream().filter(member -> member.getId() == assignment.getTeamMember().getId())
                    .findFirst().orElseThrow();
            int workScheduleAdjustmentHours = teamMember.getStartHourInUTC() - START_HOUR;
            int adjustedStartTime = assignment.getStartTime() + workScheduleAdjustmentHours;
            int adjustedShift =
                calculateShiftDueToBlockedByTasks(dagProject, adjustedTaskBoard, assignment,
                    adjustedStartTime);

            adjustedStartTime = teamMember.whenCanStartTask(adjustedStartTime + adjustedShift);
            TimedTask task = (TimedTask) assignment.getTask();
            int adjustedFinishHour =
                teamMember.whenCanFinishTask(adjustedStartTime, task.getEstimatedHours());

            Assignment adjustedAssignment =
                new DefaultAssignment(teamMember, task,
                    adjustedStartTime,
                    adjustedFinishHour);

            adjustedTaskBoard.addAssignment(adjustedAssignment);
        }

        return adjustedTaskBoard;
    }

    private static int calculateShiftDueToBlockedByTasks(DAGProject<TimedTask> dagProject,
                                                         TaskBoard adjustedTaskBoard,
                                                         Assignment assignment,
                                                         int adjustedStartTime) {
        int earliestPossibleStartWithoutBlocks =
            adjustedTaskBoard.getAssignments().stream()
                .filter(ass -> ass.getTeamMember().getId() == assignment.getTeamMember().getId())
                .map(Assignment::getFinishTime)
                .max(naturalOrder())
                .orElse(adjustedStartTime);

        Set<TimedTask> blockedByTasks = dagProject.blockedBy(assignment.getTask());
        int latestBlockedTaskFinishTime = blockedByTasks.stream().flatMap(
                task -> adjustedTaskBoard.getAssignments()
                    .stream().filter(
                        adjustedAssignment -> adjustedAssignment.getTask().getId() == task.getId())
            ).map(Assignment::getFinishTime)
            .max(naturalOrder())
            .orElse(adjustedStartTime);

        return Math.max(earliestPossibleStartWithoutBlocks, latestBlockedTaskFinishTime) -
            adjustedStartTime;
    }

    private TaskBoard scheduleSameWorkHoursTeam(Project<TimedTask> project, AtomicInteger time,
                                                DefaultTeam sameWorkHoursTeam) {
        TaskBoard taskBoard = new DefaultTaskBoard(sameWorkHoursTeam);

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
