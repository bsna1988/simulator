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
import ua.edu.krok.scheduler.impl.DAGProject;
import ua.edu.krok.scheduler.impl.DefaultTeam;
import ua.edu.krok.scheduler.impl.EFTScheduler;
import ua.edu.krok.scheduler.impl.ESTWorkHoursUnawareScheduler;
import ua.edu.krok.scheduler.impl.ScheduleAwareTeamMember;
import ua.edu.krok.scheduler.impl.TimedTask;
import ua.edu.krok.taskset.impl.RandomDAG;

public class Main {
    public static void main(String[] args) {
        MetricRegistry registry = new MetricRegistry();
        Histogram histogram1 = registry.histogram("est (unaware)");
        Histogram histogram2 = registry.histogram("est (aware)");

        RandomDAG generator = new RandomDAG();

        Team team = new DefaultTeam();
        LocalDateTime now = LocalDateTime.now();
        team.addTeamMember(new ScheduleAwareTeamMember(1,
            ZoneId.of("Europe/Kiev").getRules().getOffset(now), 11));
        team.addTeamMember(
            new ScheduleAwareTeamMember(2,
                ZoneId.of("America/Los_Angeles").getRules().getOffset(now), 8));

        for (int i = 0; i < 1; i++) {
            simulate(team, histogram1, histogram2, generator);
        }
        ConsoleReporter reporter = ConsoleReporter.forRegistry(registry).build();
        reporter.report();
    }

    private static void simulate(Team team, Histogram histogram1,
                                 Histogram histogram2, RandomDAG generator) {
        TaskSet<TimedTask> taskSet = generator.generate(5, 5, 8);

        System.out.println("---EST (timezone unaware) of Distributed team---");

        Scheduler<TimedTask> scheduler1 = new ESTWorkHoursUnawareScheduler();
        TaskBoard taskBoard1 = scheduler1.simulate(new DAGProject<>(taskSet), team);
        histogram1.update(taskBoard1.getFinishTime());
        System.out.println(taskBoard1);

        System.out.println("---EST (timezone aware) of Distributed team---");

        Scheduler<TimedTask> scheduler2 = new EFTScheduler();
        TaskBoard taskBoard2 = scheduler2.simulate(new DAGProject<>(taskSet), team);

        histogram2.update(taskBoard2.getFinishTime());
        System.out.println(taskBoard2);
    }
}