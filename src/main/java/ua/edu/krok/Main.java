package ua.edu.krok;

import java.time.ZoneOffset;

import ua.edu.krok.scheduler.Simulator;
import ua.edu.krok.scheduler.TaskBoard;
import ua.edu.krok.scheduler.TaskSet;
import ua.edu.krok.scheduler.Team;
import ua.edu.krok.scheduler.impl.DAGProject;
import ua.edu.krok.scheduler.impl.DefaultTeam;
import ua.edu.krok.scheduler.impl.ESTSimulator;
import ua.edu.krok.scheduler.impl.ScheduleAwareTeamMember;
import ua.edu.krok.scheduler.impl.TimedTask;
import ua.edu.krok.taskset.impl.RandomDAG;

public class Main {
    public static void main(String[] args) {
        RandomDAG generator = new RandomDAG();
        TaskSet<TimedTask> taskSet = generator.generate(10, 10, 8);

        Team team = new DefaultTeam();
        team.addTeamMember(new ScheduleAwareTeamMember(1, ZoneOffset.of("CET"), 10));
        team.addTeamMember(new ScheduleAwareTeamMember(2, ZoneOffset.of("PST"), 9));

        Simulator<TimedTask> simulator = new ESTSimulator();
        TaskBoard taskBoard = simulator.simulate(new DAGProject<>(taskSet), team);

        System.out.println(taskBoard);
    }
}