package pl.betoncraft.betonquest.config;

import lombok.Getter;
import pl.betoncraft.betonquest.core.Journal;

import java.util.List;

@Getter
public class JournalProfile {

    private final String id;
    private final String title;
    private final int priority;
    private final Journal.Status status;
    private final String waypoint;
    private final List<String> contents;

    public JournalProfile(String id, String title, int priority, Journal.Status status, String waypoint, List<String> contents) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.status = status;
        this.waypoint = waypoint;
        this.contents = contents;
    }
}
