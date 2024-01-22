package ua.edu.krok.scheduler;

public interface TeamMember {
    int whenCanFinishTask(int currentTime, int taskEstimateHours);

    int whenCanStartTask(int currentTime);
}
