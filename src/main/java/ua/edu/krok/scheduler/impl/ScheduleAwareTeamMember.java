package ua.edu.krok.scheduler.impl;

import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import ua.edu.krok.scheduler.TeamMember;

@Getter
public class ScheduleAwareTeamMember implements TeamMember {
    public static final int INIT_DAYS = 365;
    private final WorkHoursAwareTeamMember delegate;
    private final int startHourInUTC;

    /**
     * Creates teamMember who is working in specified timezone, starts work day at specified hour,
     * has 1 hour of break and works 8 hours per day
     *
     * @param zoneOffset - Team member time zone
     * @param startHour  - working start hour
     */
    public ScheduleAwareTeamMember(int id, ZoneOffset zoneOffset, int startHour) {
        this.startHourInUTC = OffsetTime.of(LocalTime.of(startHour, 0), zoneOffset)
            .withOffsetSameInstant(ZoneOffset.UTC).getHour();
        int currentDay = startHourInUTC;
        List<Integer> timeBounds = new ArrayList<>();
        while (currentDay < TimeUnit.DAYS.toHours(INIT_DAYS)) {
            timeBounds.add(currentDay);
            timeBounds.add(currentDay + 4);
            timeBounds.add(currentDay + 5);
            timeBounds.add(currentDay + 9);
            currentDay += TimeUnit.DAYS.toHours(1);
        }
        delegate = new WorkHoursAwareTeamMember(id, timeBounds);
    }

    @Override
    public int getId() {
        return delegate.getId();
    }

    @Override
    public int whenCanFinishTask(int currentTime, int taskEstimateHours) {
        return delegate.whenCanFinishTask(currentTime, taskEstimateHours);
    }

    @Override
    public int whenCanStartTask(int currentTime) {
        return delegate.whenCanStartTask(currentTime);
    }

    @Override
    public String toString() {
        return String.format("Team Member %d [%d-%d]", delegate.getId(), startHourInUTC,
            startHourInUTC + 9);
    }
}
