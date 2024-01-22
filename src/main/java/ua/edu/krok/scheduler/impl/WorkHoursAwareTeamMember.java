package ua.edu.krok.scheduler.impl;

import java.util.List;

import lombok.RequiredArgsConstructor;
import ua.edu.krok.scheduler.TeamMember;

@RequiredArgsConstructor
public class WorkHoursAwareTeamMember implements TeamMember {
    private static final int MAX_WORKING_HOUR = 1000;
    private final int id;
    private final boolean[] workHours = new boolean[MAX_WORKING_HOUR];

    public WorkHoursAwareTeamMember(int id, List<Integer> workingHoursBonds) {
        this.id = id;
        int i = 0;
        while (i < workingHoursBonds.size()) {
            int startHour = workingHoursBonds.get(i++);
            int endHour = workingHoursBonds.get(i++);
            for (int k = startHour; k < endHour; k++) {
                workHours[k] = true;
            }
        }
    }

    @Override

    public int whenCanFinishTask(int currentTime, int taskEstimateHours) {
        int finishTime = currentTime;
        int remainingHours = taskEstimateHours;
        while (remainingHours > 0) {
            if (workHours[finishTime++]) {
                remainingHours--;
            }
        }
        return finishTime;
    }

    @Override
    public int whenCanStartTask(int currentTime) {
        for (int i = currentTime; i < MAX_WORKING_HOUR; i++) {
            if (workHours[i]) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return "Team Member " + id;
    }
}
