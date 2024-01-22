package ua.edu.krok;

import java.time.LocalDateTime;
import java.time.ZoneId;

import ua.edu.krok.scheduler.Simulator;
import ua.edu.krok.scheduler.TaskBoard;
import ua.edu.krok.scheduler.TaskSet;
import ua.edu.krok.scheduler.Team;
import ua.edu.krok.scheduler.impl.DAGProject;
import ua.edu.krok.scheduler.impl.DefaultSimulator;
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


        System.out.println("---Default Simulator of Distributed team---");
        Team team = new DefaultTeam();
        LocalDateTime now = LocalDateTime.now();
        team.addTeamMember(new ScheduleAwareTeamMember(1,
            ZoneId.of("Europe/Kiev").getRules().getOffset(now), 10));
        team.addTeamMember(
            new ScheduleAwareTeamMember(2,
                ZoneId.of("America/Los_Angeles").getRules().getOffset(now), 9));

        Simulator<TimedTask> simulator1 = new DefaultSimulator();
        TaskBoard taskBoard1 = simulator1.simulate(new DAGProject<>(taskSet), team);

        System.out.println(taskBoard1);

        System.out.println("---EST (timezone aware) of Distributed team---");


        Simulator<TimedTask> simulator2 = new ESTSimulator();
        TaskBoard taskBoard2 = simulator2.simulate(new DAGProject<>(taskSet), team);

        System.out.println(taskBoard2);

        System.out.println("---EFT (timezone aware) of Distributed team---");

        Simulator<TimedTask> simulator3 = new ESTSimulator();
        TaskBoard taskBoard3 = simulator3.simulate(new DAGProject<>(taskSet), team);

        System.out.println(taskBoard3);
    }
}