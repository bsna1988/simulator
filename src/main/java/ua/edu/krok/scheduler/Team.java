package ua.edu.krok.scheduler;

import java.util.List;

public interface Team {
    List<TeamMember> teamMembers();

    void addTeamMember(TeamMember teamMember);
}
