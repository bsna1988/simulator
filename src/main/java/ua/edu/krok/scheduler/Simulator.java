package ua.edu.krok.scheduler;

public interface Simulator<T extends Task> {

    TaskBoard simulate(Project<T> project, Team team);
}
