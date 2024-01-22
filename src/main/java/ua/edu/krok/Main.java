package ua.edu.krok;

import java.time.LocalDateTime;
import java.time.ZoneId;

import ua.edu.krok.scheduler.Simulator;
import ua.edu.krok.scheduler.TaskBoard;
import ua.edu.krok.scheduler.TaskSet;
import ua.edu.krok.scheduler.Team;
import ua.edu.krok.scheduler.impl.DAGProject;
import ua.edu.krok.scheduler.impl.DefaultTeam;
import ua.edu.krok.scheduler.impl.DefaultTeamMember;
import ua.edu.krok.scheduler.impl.ESTSimulator;
import ua.edu.krok.scheduler.impl.ScheduleAwareTeamMember;
import ua.edu.krok.scheduler.impl.TimedTask;
import ua.edu.krok.taskset.impl.RandomDAG;

public class Main {
    public static void main(String[] args) {
        RandomDAG generator = new RandomDAG();
        TaskSet<TimedTask> taskSet = generator.generate(10, 10, 8);


        System.out.println("---EST of Distributed team---");
        Team team = new DefaultTeam();
        team.addTeamMember(new DefaultTeamMember(1));
        team.addTeamMember(new DefaultTeamMember(2));

        Simulator<TimedTask> simulator1 = new ESTSimulator();
        TaskBoard taskBoard1 = simulator1.simulate(new DAGProject<>(taskSet), team);

        System.out.println(taskBoard1);

        System.out.println("---EST (timezone aware) of Distributed team---");

        Team timeZoneAwareTeam = new DefaultTeam();
        LocalDateTime now = LocalDateTime.now();
        timeZoneAwareTeam.addTeamMember(new ScheduleAwareTeamMember(1,
            ZoneId.of("Europe/Kiev").getRules().getOffset(now), 10));
        timeZoneAwareTeam.addTeamMember(
            new ScheduleAwareTeamMember(2,
                ZoneId.of("America/Los_Angeles").getRules().getOffset(now), 9));

        Simulator<TimedTask> simulator2 = new ESTSimulator();
        TaskBoard taskBoard2 = simulator2.simulate(new DAGProject<>(taskSet), timeZoneAwareTeam);

        System.out.println(taskBoard2);
    }
}