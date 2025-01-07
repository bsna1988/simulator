package ua.edu.krok;

import static java.util.stream.Collectors.joining;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

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
    public static final String ASIA = "GMT+6";
    public static final int MAX_EDGE_FACTOR = 3;

    public static void main(String[] args) {
        MetricRegistry registry = new MetricRegistry();
        Histogram localHistogram = registry.histogram("est (local)");
        Histogram twoPhaseHistogram = registry.histogram("est (unaware)");
        Histogram proposedApproachhistogram = registry.histogram("est (aware)");

        RandomDAG generator = new RandomDAG();

        Team distributedTeam = new DefaultTeam();
        LocalDateTime now = LocalDateTime.now();
        distributedTeam.addTeamMember(new ScheduleAwareTeamMember(1,
            ZoneId.of(ASIA).getRules().getOffset(now), 9));
        distributedTeam.addTeamMember(new ScheduleAwareTeamMember(2,
            ZoneId.of(ASIA).getRules().getOffset(now), 9));
        distributedTeam.addTeamMember(new ScheduleAwareTeamMember(3,
            ZoneId.of(ASIA).getRules().getOffset(now), 9));

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
                    ZoneId.of(ASIA).getRules().getOffset(now), 9));
        }


        double[] proposedApproachMeanDurations = new double[MAX_EDGE_FACTOR];
        double[] twoPhaseMeanDurations = new double[MAX_EDGE_FACTOR];
        double[] localMeanDurations = new double[MAX_EDGE_FACTOR];

        for (int edgeFactor = 1; edgeFactor <= MAX_EDGE_FACTOR; edgeFactor++) {
            for (int i = 0; i < 100; i++) {
                System.out.println("Project " + i);
                simulate(distributedTeam, sameWorkHoursTeam, localHistogram, twoPhaseHistogram,
                    proposedApproachhistogram,
                    generator, edgeFactor);
            }
            proposedApproachMeanDurations[edgeFactor - 1] =
                proposedApproachhistogram.getSnapshot().getMean();
            twoPhaseMeanDurations[edgeFactor - 1] = twoPhaseHistogram.getSnapshot().getMean();
            localMeanDurations[edgeFactor - 1] = localHistogram.getSnapshot().getMean();
        }

        System.out.println(
            "proposedApproachMeanDurations = [" + asString(proposedApproachMeanDurations) + "]");
        System.out.println("twoPhaseMeanDurations = [" + asString(twoPhaseMeanDurations) + "]");
        System.out.println("localMeanDurations = [" + asString(localMeanDurations) + "]");

    }

    private static String asString(double[] mean_durations1) {
        return Arrays.stream(mean_durations1).mapToObj(mean -> String.format("%.2f", mean))
            .collect(joining(","));
    }

    private static void simulate(Team distributedTeam,
                                 DefaultTeam sameWorkHoursTeam,
                                 Histogram histogram0,
                                 Histogram histogram1,
                                 Histogram histogram2,
                                 RandomDAG generator, int edgeFactor) {
        TaskSet<TimedTask> taskSet = generator.generate(15, 15 * edgeFactor, 24);


        System.out.println("---EST of Local team---");

        Scheduler<TimedTask> scheduler0 = new LocalTeamScheduler();
        TaskBoard taskBoard0 = scheduler0.simulate(new DAGProject<>(taskSet), sameWorkHoursTeam);
        histogram0.update(taskBoard0.getFinishTime());
        System.out.println(taskBoard0);

        System.out.println("---EST (timezone unaware) of Distributed distributedTeam---");

        Scheduler<TimedTask> scheduler1 = new ESTWorkHoursUnawareScheduler();
        TaskBoard taskBoard1 = scheduler1.simulate(new DAGProject<>(taskSet), distributedTeam);
        histogram1.update(taskBoard1.getFinishTime());
        System.out.println(taskBoard1);

        System.out.println("---EST (timezone aware) of Distributed distributedTeam---");

        Scheduler<TimedTask> scheduler2 = new EFTScheduler();
        TaskBoard taskBoard2 = scheduler2.simulate(new DAGProject<>(taskSet), distributedTeam);

        histogram2.update(taskBoard2.getFinishTime());
        System.out.println(taskBoard2);
    }

}