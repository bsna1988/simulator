package ua.edu.krok.scheduler.impl;

import java.util.ArrayList;
import java.util.List;

import ua.edu.krok.scheduler.Team;
import ua.edu.krok.scheduler.TeamMember;

public class DefaultTeam implements Team {
    private final List<TeamMember> teamMembers = new ArrayList<>();

    @Override
    public List<TeamMember> teamMembers() {
        return teamMembers;
    }

    @Override
    public void addTeamMember(TeamMember teamMember) {
        teamMembers.add(teamMember);
    }
}
