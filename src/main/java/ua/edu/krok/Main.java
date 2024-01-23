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
import ua.edu.krok.scheduler.impl.DefaultScheduler;
import ua.edu.krok.scheduler.impl.DefaultTeam;
import ua.edu.krok.scheduler.impl.EFTScheduler;
import ua.edu.krok.scheduler.impl.ESTScheduler;
import ua.edu.krok.scheduler.impl.ScheduleAwareTeamMember;
import ua.edu.krok.scheduler.impl.TimedTask;
import ua.edu.krok.taskset.impl.RandomDAG;

public class Main {
    public static void main(String[] args) {
        MetricRegistry registry = new MetricRegistry();
        Histogram defaultHistogram = registry.histogram("default");
        Histogram estHistogram = registry.histogram("est");
        Histogram eftHistogram = registry.histogram("eft");

        RandomDAG generator = new RandomDAG();

        Team team = new DefaultTeam();
        LocalDateTime now = LocalDateTime.now();
        team.addTeamMember(new ScheduleAwareTeamMember(1,
            ZoneId.of("Europe/Kiev").getRules().getOffset(now), 11));
        team.addTeamMember(
            new ScheduleAwareTeamMember(2,
                ZoneId.of("America/Los_Angeles").getRules().getOffset(now), 8));

        for (int i = 0; i < 1000; i++) {
            simulate(team, defaultHistogram, estHistogram, eftHistogram, generator);
        }
        ConsoleReporter reporter = ConsoleReporter.forRegistry(registry).build();
        reporter.report();
    }

    private static void simulate(Team team, Histogram defaultHistogram, Histogram estHistogram,
                                 Histogram eftHistogram, RandomDAG generator) {
        TaskSet<TimedTask> taskSet = generator.generate(10, 15, 40);

        //System.out.println("---Default Simulator of Distributed team---");

        Scheduler<TimedTask> scheduler1 = new DefaultScheduler();
        TaskBoard taskBoard1 = scheduler1.simulate(new DAGProject<>(taskSet), team);
        defaultHistogram.update(taskBoard1.getFinishTime());

        //System.out.println(taskBoard1);

        //System.out.println("---EST (timezone aware) of Distributed team---");


        Scheduler<TimedTask> scheduler2 = new ESTScheduler();
        TaskBoard taskBoard2 = scheduler2.simulate(new DAGProject<>(taskSet), team);
        estHistogram.update(taskBoard2.getFinishTime());
        //System.out.println(taskBoard2);

        //System.out.println("---EFT (timezone aware) of Distributed team---");

        Scheduler<TimedTask> scheduler3 = new EFTScheduler();
        TaskBoard taskBoard3 = scheduler3.simulate(new DAGProject<>(taskSet), team);

        eftHistogram.update(taskBoard3.getFinishTime());
        //System.out.println(taskBoard3);
    }
}