package ua.edu.krok.scheduler;

public interface Scheduler<T extends Task> {

    TaskBoard simulate(Project<T> project, Team team);
}
