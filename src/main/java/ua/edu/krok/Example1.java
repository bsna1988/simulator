package ua.edu.krok;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import ua.edu.krok.scheduler.Scheduler;
import ua.edu.krok.scheduler.TaskBoard;
import ua.edu.krok.scheduler.Team;
import ua.edu.krok.scheduler.TeamMember;
import ua.edu.krok.scheduler.impl.DAGProject;
import ua.edu.krok.scheduler.impl.DefaultTeam;
import ua.edu.krok.scheduler.impl.EFTScheduler;
import ua.edu.krok.scheduler.impl.ESTWorkHoursUnawareScheduler;
import ua.edu.krok.scheduler.impl.LocalTeamScheduler;
import ua.edu.krok.scheduler.impl.ScheduleAwareTeamMember;
import ua.edu.krok.scheduler.impl.TimedTask;

public class Example1 {

    public static final String CALIFORNIA = "GMT-7";
    public static final String EASTERN_EUROPE = "GMT+1";

    public static void main(String[] args) {

        Team distributedTeam = new DefaultTeam();
        LocalDateTime now = LocalDateTime.now();
        distributedTeam.addTeamMember(new ScheduleAwareTeamMember(1,
            ZoneId.of(EASTERN_EUROPE).getRules().getOffset(now), 9));

        distributedTeam.addTeamMember(new ScheduleAwareTeamMember(2,
            ZoneId.of(CALIFORNIA).getRules().getOffset(now), 9));

        DefaultTeam sameWorkHoursTeam = new DefaultTeam();
        for (TeamMember teamMember : distributedTeam.teamMembers()) {
            sameWorkHoursTeam.addTeamMember(
                new ScheduleAwareTeamMember(teamMember.getId(),
                    ZoneId.of(EASTERN_EUROPE).getRules().getOffset(now), 9));
        }
        DAGProject<TimedTask> project = new DAGProject<>();
        TimedTask task1 = new TimedTask(1, 4);
        TimedTask task2 = new TimedTask(2, 12);
        TimedTask task3 = new TimedTask(3, 4);
        TimedTask task4 = new TimedTask(4, 2);

        project.addTask(task1, Collections.emptySet());
        project.addTask(task2, Collections.singleton(task1));
        project.addTask(task3, Collections.emptySet());
        project.addTask(task4, new HashSet<>(Arrays.asList(task2, task3)));

        simulate(distributedTeam, sameWorkHoursTeam, project);

    }

    private static void simulate(Team distributedTeam,
                                 DefaultTeam sameWorkHoursTeam, DAGProject<TimedTask> project) {


        System.out.println("---EST of Local team---");

        Scheduler<TimedTask> scheduler0 = new LocalTeamScheduler();
        TaskBoard taskBoard0 = scheduler0.simulate(project, sameWorkHoursTeam);
        System.out.println(taskBoard0);

        System.out.println("---EST (timezone unaware) of Distributed distributedTeam---");

        Scheduler<TimedTask> scheduler1 = new ESTWorkHoursUnawareScheduler();
        TaskBoard taskBoard1 = scheduler1.simulate(project, distributedTeam);
        System.out.println(taskBoard1);

        System.out.println("---EST (timezone aware) of Distributed distributedTeam---");

        Scheduler<TimedTask> scheduler2 = new EFTScheduler();
        TaskBoard taskBoard2 = scheduler2.simulate(project, distributedTeam);

        System.out.println(taskBoard2);

        printEmployeeAssignments(taskBoard0, 0, 1);
        printEmployeeAssignments(taskBoard0, 0, 2);

        printEmployeeAssignments(taskBoard1, 1, 1);
        printEmployeeAssignments(taskBoard1, 1, 2);

        printEmployeeAssignments(taskBoard2, 2, 1);
        printEmployeeAssignments(taskBoard2, 2, 2);

    }

    private static void printEmployeeAssignments(TaskBoard taskBoard1, int boardId,
                                                 int employeeId) {
        System.out.println("const e_" + boardId + "_" + employeeId + "=`");
        taskBoard1.getAssignments().stream()
            .filter(assignment -> assignment.getTeamMember().getId() == employeeId)
            .forEach(assignment -> {
                TimedTask task = (TimedTask) assignment.getTask();
                System.out.println(
                    assignment.getTask().getId() + "," +
                        task.getEstimatedHours() + "," +
                        assignment.getStartTime() + "," +
                        assignment.getFinishTime());
            });
        System.out.println("`;");
    }
}