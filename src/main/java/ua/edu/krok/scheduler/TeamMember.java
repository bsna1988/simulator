package ua.edu.krok.scheduler;

public interface TeamMember {

    int getId();

    int whenCanFinishTask(int currentTime, int taskEstimateHours);

    int whenCanStartTask(int currentTime);
}
