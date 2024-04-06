package ua.edu.krok;

import java.time.LocalDateTime;
import java.time.ZoneId;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import ua.edu.krok.scheduler.Scheduler;
import ua.edu.krok.scheduler.TaskBoard;
import ua.edu.krok.scheduler.TaskSet;
import ua.edu.krok.scheduler.Team;
import ua.edu.krok.scheduler.TeamMember;
import ua.edu.krok.scheduler.impl.DAGProject;
import ua.edu.krok.scheduler.impl.DefaultTeam;
import ua.edu.krok.scheduler.impl.EFTScheduler;
import ua.edu.krok.scheduler.impl.ESTWorkHoursUnawareScheduler;
import ua.edu.krok.scheduler.impl.LocalTeamScheduler;
import ua.edu.krok.scheduler.impl.ScheduleAwareTeamMember;
import ua.edu.krok.scheduler.impl.TimedTask;
import ua.edu.krok.taskset.impl.RandomDAG;

public class Main {

    public static final String CALIFORNIA = "GMT-7";
    public static final String EASTERN_EUROPE = "GMT+1";

    public static void main(String[] args) {
        MetricRegistry registry = new MetricRegistry();
        Histogram histogram0 = registry.histogram("est (local)");
        Histogram histogram1 = registry.histogram("est (unaware)");
        Histogram histogram2 = registry.histogram("est (aware)");

        RandomDAG generator = new RandomDAG();

        Team distributedTeam = new DefaultTeam();
        LocalDateTime now = LocalDateTime.now();
        distributedTeam.addTeamMember(new ScheduleAwareTeamMember(1,
            ZoneId.of(EASTERN_EUROPE).getRules().getOffset(now), 9));
        distributedTeam.addTeamMember(new ScheduleAwareTeamMember(2,
            ZoneId.of(EASTERN_EUROPE).getRules().getOffset(now), 9));
        distributedTeam.addTeamMember(new ScheduleAwareTeamMember(3,
            ZoneId.of(EASTERN_EUROPE).getRules().getOffset(now), 9));

        distributedTeam.addTeamMember(
            new ScheduleAwareTeamMember(4,
                ZoneId.of(CALIFORNIA).getRules().getOffset(now), 9));
        distributedTeam.addTeamMember(
            new ScheduleAwareTeamMember(5,
                ZoneId.of(CALIFORNIA).getRules().getOffset(now), 9));
        distributedTeam.addTeamMember(
            new ScheduleAwareTeamMember(6,
                ZoneId.of(CALIFORNIA).getRules().getOffset(now), 9));

        DefaultTeam sameWorkHoursTeam = new DefaultTeam();
        for (TeamMember teamMember : distributedTeam.teamMembers()) {
            sameWorkHoursTeam.addTeamMember(
                new ScheduleAwareTeamMember(teamMember.getId(),
                    ZoneId.of(EASTERN_EUROPE).getRules().getOffset(now), 9));
        }

        for (int i = 0; i < 100; i++) {
            System.out.println("Project " + i);
            simulate(distributedTeam, sameWorkHoursTeam, histogram0, histogram1, histogram2,
                generator);
        }
        ConsoleReporter reporter = ConsoleReporter.forRegistry(registry).build();
        reporter.report();
        System.out.println(String.format("means = [%.2f, %.2f, %.2f]",
            histogram2.getSnapshot().getMean(),
            histogram1.getSnapshot().getMean(), histogram0.getSnapshot().getMean()));
        System.out.println(String.format("stddevs = [%.2f, %.2f, %.2f]",
            histogram2.getSnapshot().getStdDev(),
            histogram1.getSnapshot().getStdDev(), histogram0.getSnapshot().getStdDev()));
    }

    private static void simulate(Team distributedTeam,
                                 DefaultTeam sameWorkHoursTeam,
                                 Histogram histogram0,
                                 Histogram histogram1,
                                 Histogram histogram2,
                                 RandomDAG generator) {
        TaskSet<TimedTask> taskSet = generator.generate(15, 15, 8);


        System.out.println("---EST of Local team---");

        Scheduler<TimedTask> scheduler0 = new LocalTeamScheduler();
        TaskBoard taskBoard0 = scheduler0.simulate(new DAGProject<>(taskSet), sameWorkHoursTeam);
        histogram0.update(taskBoard0.getFinishTime());
        //   System.out.println(taskBoard0);

        System.out.println("---EST (timezone unaware) of Distributed distributedTeam---");

        Scheduler<TimedTask> scheduler1 = new ESTWorkHoursUnawareScheduler();
        TaskBoard taskBoard1 = scheduler1.simulate(new DAGProject<>(taskSet), distributedTeam);
        histogram1.update(taskBoard1.getFinishTime());
        //   System.out.println(taskBoard1);

        System.out.println("---EST (timezone aware) of Distributed distributedTeam---");

        Scheduler<TimedTask> scheduler2 = new EFTScheduler();
        TaskBoard taskBoard2 = scheduler2.simulate(new DAGProject<>(taskSet), distributedTeam);

        histogram2.update(taskBoard2.getFinishTime());
        //   System.out.println(taskBoard2);


        //printEmployeeAssignments(taskBoard0, 0, 1);
        //printEmployeeAssignments(taskBoard0, 0, 2);

        //printEmployeeAssignments(taskBoard1, 1, 1);
        //printEmployeeAssignments(taskBoard1, 1, 2);

        //printEmployeeAssignments(taskBoard2, 2, 1);
        //printEmployeeAssignments(taskBoard2, 2, 2);
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
                        assignment.getFinishTime() + "");
            });
        System.out.println("`;");
    }
}