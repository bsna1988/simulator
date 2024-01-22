package ua.edu.krok.scheduler.impl;

import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import ua.edu.krok.scheduler.TeamMember;

public class ScheduleAwareTeamMember implements TeamMember {
    public static final int INIT_DAYS = 30;
    private final WorkHoursAwareTeamMember delegate;

    /**
     * Creates teamMember who is working in specified timezone, starts work day at specified hour,
     * has 1 hour of break and works 8 hours per day
     *
     * @param zoneOffset - Team member time zone
     * @param startHour  - working start hour
     */
    public ScheduleAwareTeamMember(int id, ZoneOffset zoneOffset, int startHour) {
        List<Integer> timeBounds = new ArrayList<>();
        int days = INIT_DAYS;
        while (days > 0) {
            int startHourInUTC = OffsetTime.of(LocalTime.of(startHour, 0), zoneOffset)
                .withOffsetSameInstant(ZoneOffset.UTC).getHour();
            timeBounds.add(startHourInUTC);
            timeBounds.add(startHourInUTC + 4);
            timeBounds.add(startHourInUTC + 5);
            timeBounds.add(startHourInUTC + 9);
            days--;
        }
        delegate = new WorkHoursAwareTeamMember(id, timeBounds);
    }

    @Override
    public int whenCanFinishTask(int currentTime, int taskEstimateHours) {
        return delegate.whenCanFinishTask(currentTime, taskEstimateHours);
    }

    @Override
    public int whenCanStartTask(int currentTime) {
        return delegate.whenCanStartTask(currentTime);
    }
}
