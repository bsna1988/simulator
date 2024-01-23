package ua.edu.krok.scheduler.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.edu.krok.scheduler.TeamMember;


@Getter
@RequiredArgsConstructor
public class DefaultTeamMember implements TeamMember {
    private final int id;

    public String toString() {
        return "Team Member " + id;
    }

    @Override
    public int whenCanFinishTask(int currentTime, int taskEstimateHours) {
        return currentTime + taskEstimateHours;
    }

    @Override
    public int whenCanStartTask(int currentTime) {
        return currentTime;
    }
}
