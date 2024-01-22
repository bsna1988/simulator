package ua.edu.krok.scheduler;

public interface Assignment {
    Task getTask();

    TeamMember getTeamMember();

    int getStartTime();

    int getFinishTime();
}
